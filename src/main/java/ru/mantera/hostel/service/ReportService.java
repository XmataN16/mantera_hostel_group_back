package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.report.*;
import ru.mantera.hostel.entity.*;
import ru.mantera.hostel.enums.PaymentStatus;
import ru.mantera.hostel.enums.ReservationStatus;
import ru.mantera.hostel.enums.RoomStatus;
import ru.mantera.hostel.exception.BadRequestException;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationServiceItemRepository reservationServiceItemRepository;
    private final PaymentRepository paymentRepository;
    private final RoomTypeRepository roomTypeRepository;

    public OccupancyReportResponse getOccupancy(Long hotelId, LocalDate date) {
        validateHotel(hotelId);

        List<Room> rooms = roomRepository.findByHotel_Id(hotelId);
        List<Reservation> reservations = reservationRepository
                .findByHotelIdAndCheckInDateLessThanAndCheckOutDateGreaterThanOrderByCheckInDateAsc(
                        hotelId,
                        date.plusDays(1),
                        date
                );

        long totalRooms = rooms.size();

        long maintenanceRooms = rooms.stream()
                .filter(room -> room.getStatus() == RoomStatus.MAINTENANCE
                        || room.getStatus() == RoomStatus.OUT_OF_SERVICE)
                .count();

        long occupiedRooms = reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.CHECKED_IN)
                .flatMap(reservation -> reservation.getReservationRooms().stream())
                .filter(room -> room.getRoomId() != null)
                .map(ReservationRoom::getRoomId)
                .distinct()
                .count();

        long bookedRooms = reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.CREATED
                        || reservation.getStatus() == ReservationStatus.CONFIRMED)
                .flatMap(reservation -> reservation.getReservationRooms().stream())
                .filter(room -> room.getRoomId() != null)
                .map(ReservationRoom::getRoomId)
                .distinct()
                .count();

        long unavailableRooms = occupiedRooms + bookedRooms + maintenanceRooms;
        long availableRooms = Math.max(totalRooms - unavailableRooms, 0);

        double occupancyPercent = totalRooms == 0
                ? 0
                : Math.round((occupiedRooms * 10000.0) / totalRooms) / 100.0;

        return new OccupancyReportResponse(
                hotelId,
                date,
                totalRooms,
                occupiedRooms,
                bookedRooms,
                availableRooms,
                maintenanceRooms,
                occupancyPercent
        );
    }

    public CheckInOutReportResponse getCheckInsCheckOuts(Long hotelId, LocalDate date) {
        validateHotel(hotelId);

        List<Reservation> checkIns = reservationRepository
                .findByHotelIdAndCheckInDateLessThanAndCheckOutDateGreaterThanOrderByCheckInDateAsc(
                        hotelId,
                        date.plusDays(1),
                        date.minusDays(1)
                )
                .stream()
                .filter(reservation -> reservation.getCheckInDate().equals(date))
                .toList();

        List<Reservation> checkOuts = reservationRepository
                .findByHotelIdAndCheckInDateLessThanAndCheckOutDateGreaterThanOrderByCheckInDateAsc(
                        hotelId,
                        date.plusDays(1),
                        date.minusDays(1)
                )
                .stream()
                .filter(reservation -> reservation.getCheckOutDate().equals(date))
                .toList();

        return new CheckInOutReportResponse(
                hotelId,
                date,
                checkIns.size(),
                checkOuts.size()
        );
    }

    public RevenueReportResponse getRevenue(Long hotelId, LocalDate fromDate, LocalDate toDate) {
        validateHotel(hotelId);
        validatePeriod(fromDate, toDate);

        List<Reservation> reservations = reservationRepository
                .findByHotelIdAndCheckInDateLessThanAndCheckOutDateGreaterThanOrderByCheckInDateAsc(
                        hotelId,
                        toDate.plusDays(1),
                        fromDate
                );

        List<ReservationServiceItem> serviceItems =
                reservationServiceItemRepository.findByReservation_HotelId(hotelId);

        BigDecimal servicesRevenue = serviceItems.stream()
                .filter(item -> isReservationInPeriod(item.getReservation(), fromDate, toDate))
                .map(ReservationServiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = reservations.stream()
                .filter(reservation -> reservation.getStatus() != ReservationStatus.CANCELLED)
                .map(Reservation::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal accommodationRevenue = totalAmount.subtract(servicesRevenue);

        if (accommodationRevenue.compareTo(BigDecimal.ZERO) < 0) {
            accommodationRevenue = BigDecimal.ZERO;
        }

        return new RevenueReportResponse(
                hotelId,
                fromDate,
                toDate,
                accommodationRevenue,
                servicesRevenue,
                accommodationRevenue.add(servicesRevenue)
        );
    }

    public DebtReportResponse getDebts(Long hotelId) {
        validateHotel(hotelId);

        List<Reservation> reservations = reservationRepository.findAll()
                .stream()
                .filter(reservation -> reservation.getHotelId().equals(hotelId))
                .filter(reservation -> reservation.getStatus() != ReservationStatus.CANCELLED)
                .filter(reservation -> reservation.getStatus() != ReservationStatus.CHECKED_OUT)
                .toList();

        List<Payment> payments = paymentRepository.findByReservation_HotelId(hotelId);

        Map<Long, BigDecimal> paidByReservation = payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.PAID)
                .collect(Collectors.groupingBy(
                        payment -> payment.getReservation().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        List<DebtReportItemResponse> items = new ArrayList<>();

        for (Reservation reservation : reservations) {
            BigDecimal paidAmount = paidByReservation.getOrDefault(reservation.getId(), BigDecimal.ZERO);
            BigDecimal debtAmount = reservation.getTotalAmount().subtract(paidAmount);

            if (debtAmount.compareTo(BigDecimal.ZERO) > 0) {
                items.add(new DebtReportItemResponse(
                        reservation.getId(),
                        reservation.getReservationNumber(),
                        reservation.getGuestId(),
                        reservation.getCheckInDate(),
                        reservation.getCheckOutDate(),
                        reservation.getTotalAmount(),
                        paidAmount,
                        debtAmount
                ));
            }
        }

        BigDecimal totalDebt = items.stream()
                .map(DebtReportItemResponse::debtAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DebtReportResponse(hotelId, totalDebt, items);
    }

    public List<RoomTypeDistributionItemResponse> getRoomTypeDistribution(
            Long hotelId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        validateHotel(hotelId);
        validatePeriod(fromDate, toDate);

        Map<Long, String> roomTypeNames = roomTypeRepository.findByHotel_Id(hotelId)
                .stream()
                .collect(Collectors.toMap(RoomType::getId, RoomType::getName));

        List<Reservation> reservations = reservationRepository
                .findByHotelIdAndCheckInDateLessThanAndCheckOutDateGreaterThanOrderByCheckInDateAsc(
                        hotelId,
                        toDate.plusDays(1),
                        fromDate
                );

        Map<Long, Long> countByRoomType = reservations.stream()
                .filter(reservation -> reservation.getStatus() != ReservationStatus.CANCELLED)
                .flatMap(reservation -> reservation.getReservationRooms().stream())
                .collect(Collectors.groupingBy(
                        ReservationRoom::getRoomTypeId,
                        Collectors.counting()
                ));

        return countByRoomType.entrySet()
                .stream()
                .map(entry -> new RoomTypeDistributionItemResponse(
                        entry.getKey(),
                        roomTypeNames.getOrDefault(entry.getKey(), "Категория #" + entry.getKey()),
                        entry.getValue()
                ))
                .toList();
    }

    private void validateHotel(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Отель с id=" + hotelId + " не найден");
        }
    }

    private void validatePeriod(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new BadRequestException("Период отчёта обязателен");
        }

        if (toDate.isBefore(fromDate)) {
            throw new BadRequestException("Дата окончания периода не может быть раньше даты начала");
        }
    }

    private boolean isReservationInPeriod(
            Reservation reservation,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return reservation.getCheckInDate().isBefore(toDate.plusDays(1))
                && reservation.getCheckOutDate().isAfter(fromDate);
    }
}