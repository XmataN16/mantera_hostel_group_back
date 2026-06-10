package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.billing.PaymentCreateRequest;
import ru.mantera.hostel.dto.billing.PaymentResponse;
import ru.mantera.hostel.dto.billing.ReservationInvoiceResponse;
import ru.mantera.hostel.dto.billing.ReservationServiceCreateRequest;
import ru.mantera.hostel.dto.billing.ReservationServiceItemResponse;
import ru.mantera.hostel.entity.AdditionalService;
import ru.mantera.hostel.entity.Payment;
import ru.mantera.hostel.entity.Reservation;
import ru.mantera.hostel.entity.ReservationRoom;
import ru.mantera.hostel.entity.ReservationServiceItem;
import ru.mantera.hostel.enums.PaymentStatus;
import ru.mantera.hostel.enums.ReservationRoomStatus;
import ru.mantera.hostel.enums.ReservationStatus;
import ru.mantera.hostel.enums.ServiceStatus;
import ru.mantera.hostel.exception.BadRequestException;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.AdditionalServiceRepository;
import ru.mantera.hostel.repository.PaymentRepository;
import ru.mantera.hostel.repository.ReservationRepository;
import ru.mantera.hostel.repository.ReservationServiceItemRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private final ReservationRepository reservationRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final ReservationServiceItemRepository reservationServiceItemRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public List<ReservationServiceItemResponse> getReservationServices(Long reservationId) {
        getReservationOrThrow(reservationId);

        return reservationServiceItemRepository.findByReservation_IdOrderByIdAsc(reservationId)
                .stream()
                .map(this::toServiceItemResponse)
                .toList();
    }

    public ReservationServiceItemResponse addService(
            Long reservationId,
            ReservationServiceCreateRequest request
    ) {
        Reservation reservation = getReservationOrThrow(reservationId);
        validateReservationCanChangeCharges(reservation);

        AdditionalService service = additionalServiceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Дополнительная услуга с id=" + request.serviceId() + " не найдена"
                ));

        if (!service.getHotel().getId().equals(reservation.getHotelId())) {
            throw new BadRequestException("Услуга не относится к отелю выбранного бронирования");
        }

        if (service.getStatus() != ServiceStatus.ACTIVE) {
            throw new BadRequestException("Нельзя добавить неактивную дополнительную услугу");
        }

        BigDecimal price = service.getPrice();
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(request.quantity()));

        ReservationServiceItem item = ReservationServiceItem.builder()
                .reservation(reservation)
                .service(service)
                .quantity(request.quantity())
                .price(price)
                .totalPrice(totalPrice)
                .build();

        ReservationServiceItem saved = reservationServiceItemRepository.save(item);

        recalculateReservationTotal(reservation);

        return toServiceItemResponse(saved);
    }

    public void deleteService(Long reservationId, Long itemId) {
        Reservation reservation = getReservationOrThrow(reservationId);
        validateReservationCanChangeCharges(reservation);

        ReservationServiceItem item = reservationServiceItemRepository
                .findByIdAndReservation_Id(itemId, reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Услуга в бронировании с id=" + itemId + " не найдена"
                ));

        reservationServiceItemRepository.delete(item);

        recalculateReservationTotal(reservation);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPayments(Long reservationId) {
        getReservationOrThrow(reservationId);

        return paymentRepository.findByReservation_IdOrderByCreatedAtAsc(reservationId)
                .stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    public PaymentResponse addPayment(Long reservationId, PaymentCreateRequest request) {
        Reservation reservation = getReservationOrThrow(reservationId);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Нельзя зарегистрировать оплату по отменённому бронированию");
        }

        ReservationInvoiceResponse invoice = getInvoice(reservationId);

        if (invoice.debtAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("По бронированию нет задолженности");
        }

        if (request.amount().compareTo(invoice.debtAmount()) > 0) {
            throw new BadRequestException("Сумма оплаты превышает задолженность по бронированию");
        }

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(request.amount())
                .method(request.method())
                .status(PaymentStatus.PAID)
                .transactionReference(request.transactionReference())
                .paidAt(OffsetDateTime.now())
                .comment(request.comment())
                .build();

        return toPaymentResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public ReservationInvoiceResponse getInvoice(Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);

        List<ReservationServiceItem> serviceItems =
                reservationServiceItemRepository.findByReservation_IdOrderByIdAsc(reservationId);

        List<Payment> payments =
                paymentRepository.findByReservation_IdOrderByCreatedAtAsc(reservationId);

        BigDecimal accommodationAmount = calculateAccommodationAmount(reservation);
        BigDecimal servicesAmount = calculateServicesAmount(serviceItems);
        BigDecimal totalAmount = accommodationAmount.add(servicesAmount);
        BigDecimal paidAmount = calculatePaidAmount(payments);
        BigDecimal debtAmount = totalAmount.subtract(paidAmount);

        if (debtAmount.compareTo(BigDecimal.ZERO) < 0) {
            debtAmount = BigDecimal.ZERO;
        }

        return new ReservationInvoiceResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                reservation.getHotelId(),
                reservation.getGuestId(),
                accommodationAmount,
                servicesAmount,
                totalAmount,
                paidAmount,
                debtAmount,
                serviceItems.stream()
                        .map(this::toServiceItemResponse)
                        .toList(),
                payments.stream()
                        .map(this::toPaymentResponse)
                        .toList()
        );
    }

    private Reservation getReservationOrThrow(Long reservationId) {
        return reservationRepository.findWithReservationRoomsById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Бронирование с id=" + reservationId + " не найдено"
                ));
    }

    private void validateReservationCanChangeCharges(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Нельзя изменять начисления по отменённому бронированию");
        }

        if (reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new BadRequestException("Нельзя изменять начисления по завершённому проживанию");
        }
    }

    private void recalculateReservationTotal(Reservation reservation) {
        List<ReservationServiceItem> serviceItems =
                reservationServiceItemRepository.findByReservation_IdOrderByIdAsc(reservation.getId());

        BigDecimal accommodationAmount = calculateAccommodationAmount(reservation);
        BigDecimal servicesAmount = calculateServicesAmount(serviceItems);

        reservation.setTotalAmount(accommodationAmount.add(servicesAmount));
        reservationRepository.save(reservation);
    }

    private BigDecimal calculateAccommodationAmount(Reservation reservation) {
        long nights = ChronoUnit.DAYS.between(
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        if (nights <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (ReservationRoom room : reservation.getReservationRooms()) {
            if (room.getStatus() == ReservationRoomStatus.CANCELLED) {
                continue;
            }

            total = total.add(
                    room.getPricePerNight().multiply(BigDecimal.valueOf(nights))
            );
        }

        return total;
    }

    private BigDecimal calculateServicesAmount(List<ReservationServiceItem> serviceItems) {
        return serviceItems.stream()
                .map(ReservationServiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculatePaidAmount(List<Payment> payments) {
        return payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ReservationServiceItemResponse toServiceItemResponse(ReservationServiceItem item) {
        return new ReservationServiceItemResponse(
                item.getId(),
                item.getReservation().getId(),
                item.getService().getId(),
                item.getService().getName(),
                item.getQuantity(),
                item.getPrice(),
                item.getTotalPrice(),
                item.getCreatedAt()
        );
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getReservation().getId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getTransactionReference(),
                payment.getPaidAt(),
                payment.getComment(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}