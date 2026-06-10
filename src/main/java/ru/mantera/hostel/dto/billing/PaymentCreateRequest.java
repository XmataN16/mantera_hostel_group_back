package ru.mantera.hostel.dto.billing;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.mantera.hostel.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentCreateRequest(

        @NotNull(message = "amount обязателен")
        @Positive(message = "amount должен быть больше 0")
        BigDecimal amount,

        @NotNull(message = "method обязателен")
        PaymentMethod method,

        String transactionReference,

        String comment
) {
}