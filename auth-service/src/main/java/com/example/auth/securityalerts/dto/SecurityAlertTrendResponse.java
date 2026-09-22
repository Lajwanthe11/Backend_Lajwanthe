package com.example.auth.securityalerts.dto;

import java.time.LocalDate;

/** Alerts raised on one day, by severity. Days without alerts are included with zeros. */
public record SecurityAlertTrendResponse(
        LocalDate date,
        long total,
        long low,
        long medium,
        long high,
        long critical
) {
}
