package com.example.auth.securityalerts.dto;

import com.example.common.exception.BadRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** A From / To date filter turned into a half-open time range [from, toExclusive). Both ends are optional. */
public record DateRange(LocalDateTime from, LocalDateTime toExclusive) {

    public static DateRange of(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("\"From\" date cannot be later than the \"To\" date");
        }
        return new DateRange(
                from != null ? from.atStartOfDay() : null,
                to != null ? to.plusDays(1).atStartOfDay() : null);
    }
}
