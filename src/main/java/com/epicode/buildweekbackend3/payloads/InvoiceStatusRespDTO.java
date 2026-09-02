package com.epicode.buildweekbackend3.payloads;

import com.epicode.buildweekbackend3.entities.InvoiceStatus;
import com.epicode.buildweekbackend3.entities.Roles;

import java.util.Comparator;
import java.util.List;

public record InvoiceStatusRespDTO(
        long id,
        String name,
        String description,
        Roles requiredRole,
        List<TransitionDTO> allowedTransitions
) {
    public record TransitionDTO(long id, String name, Roles requiredRole) {
    }

    public static InvoiceStatusRespDTO from(InvoiceStatus status) {
        List<TransitionDTO> transitions = status.getAllowedTransitions().stream()
                .map(t -> new TransitionDTO(t.getId(), t.getName(), t.getRequiredRole()))
                .sorted(Comparator.comparing(TransitionDTO::name))
                .toList();

        return new InvoiceStatusRespDTO(
                status.getId(),
                status.getName(),
                status.getDescription(),
                status.getRequiredRole(),
                transitions
        );
    }
}