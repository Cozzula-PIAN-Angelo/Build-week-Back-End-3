package com.epicode.buildweekbackend3.payloads;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClientFilterDTO(String name, BigDecimal revenueMin, BigDecimal revenueMax, LocalDate insertedFrom, LocalDate insertedTo, LocalDate contactedFrom, LocalDate contactedTo) {
}
