package com.epicode.buildweekbackend3.payloads;

import jakarta.validation.constraints.NotBlank;

public record AssignRoleDTO(
        @NotBlank(message = "Il ruolo e obbligatorio")
        String roles
        ) {}
