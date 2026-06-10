package ru.mantera.hostel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "guests",
        indexes = {
                @Index(name = "idx_guests_phone", columnList = "phone"),
                @Index(name = "idx_guests_email", columnList = "email"),
                @Index(name = "idx_guests_document_number", columnList = "document_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 20)
    private String gender;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "citizenship", length = 100)
    private String citizenship;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "document_number", nullable = false, length = 50)
    private String documentNumber;

    @Column(name = "document_issue_date")
    private LocalDate documentIssueDate;

    @Column(name = "document_issued_by", length = 255)
    private String documentIssuedBy;

    @Column(length = 500)
    private String address;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}