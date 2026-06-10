package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.mantera.hostel.dto.billing.PaymentCreateRequest;
import ru.mantera.hostel.dto.billing.PaymentResponse;
import ru.mantera.hostel.dto.billing.ReservationInvoiceResponse;
import ru.mantera.hostel.dto.billing.ReservationServiceCreateRequest;
import ru.mantera.hostel.dto.billing.ReservationServiceItemResponse;
import ru.mantera.hostel.service.BillingService;

import java.util.List;

@RestController
@RequestMapping("/api/reservations/{reservationId}")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/services")
    public List<ReservationServiceItemResponse> getReservationServices(
            @PathVariable Long reservationId
    ) {
        return billingService.getReservationServices(reservationId);
    }

    @PostMapping("/services")
    public ReservationServiceItemResponse addService(
            @PathVariable Long reservationId,
            @RequestBody @Valid ReservationServiceCreateRequest request
    ) {
        return billingService.addService(reservationId, request);
    }

    @DeleteMapping("/services/{itemId}")
    public void deleteService(
            @PathVariable Long reservationId,
            @PathVariable Long itemId
    ) {
        billingService.deleteService(reservationId, itemId);
    }

    @GetMapping("/payments")
    public List<PaymentResponse> getPayments(
            @PathVariable Long reservationId
    ) {
        return billingService.getPayments(reservationId);
    }

    @PostMapping("/payments")
    public PaymentResponse addPayment(
            @PathVariable Long reservationId,
            @RequestBody @Valid PaymentCreateRequest request
    ) {
        return billingService.addPayment(reservationId, request);
    }

    @GetMapping("/invoice")
    public ReservationInvoiceResponse getInvoice(
            @PathVariable Long reservationId
    ) {
        return billingService.getInvoice(reservationId);
    }
}