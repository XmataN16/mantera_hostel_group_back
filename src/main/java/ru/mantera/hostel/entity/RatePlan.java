package ru.mantera.hostel.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.mantera.hostel.enums.HotelStatus;
import ru.mantera.hostel.enums.MealPlan;

import java.time.OffsetDateTime;

@Entity
@Table(name = "rate_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_plan", nullable = false, length = 50)
    private MealPlan mealPlan;

    @Column(nullable = false)
    private Boolean refundable;

    @Column(name = "cancellation_policy", columnDefinition = "text")
    private String cancellationPolicy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HotelStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}