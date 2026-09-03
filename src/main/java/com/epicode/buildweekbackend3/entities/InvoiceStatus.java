package com.epicode.buildweekbackend3.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "invoice_statuses")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class InvoiceStatus extends BaseEntity {

    @NotBlank(message = "Il nome dello stato è obbligatorio.")
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "required_role", nullable = false)
    private Roles requiredRole = Roles.CONTABILE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "invoice_status_transitions",
            joinColumns = @JoinColumn(name = "from_status_id"),
            inverseJoinColumns = @JoinColumn(name = "to_status_id")
    )
    @ToString.Exclude // evita ricorsione infinita nel toString
    private Set<InvoiceStatus> allowedTransitions = new HashSet<>();

    // createdAt/updatedAt e i callback @PrePersist/@PreUpdate arrivano da
    // BaseEntity: ridichiararli qui mapperebbe due campi sulla stessa colonna.

    public InvoiceStatus(String name, Roles requiredRole) {
        this.name = name;
        this.requiredRole = requiredRole;
    }

    public boolean canTransitionTo(InvoiceStatus target) {
        return target != null && this.allowedTransitions.contains(target);
    }
}
