package ch.praemienrechner.dto;

import java.math.BigDecimal;

/**
 * Result of a premium calculation, returned by the calculate and compare endpoints.
 *
 * @param monthlyPremium the monthly premium in CHF
 * @param yearlyPremium  the yearly premium in CHF ({@code monthlyPremium × 12})
 * @param ageGroup       the resolved age group, e.g. {@code "ERWACHSENER"}
 * @param canton         the full canton name, e.g. {@code "Luzern"}
 * @param franchise      the franchise level in CHF
 * @param breakdown      the factor-by-factor breakdown of the premium
 */
public record CalculationResult(
        BigDecimal monthlyPremium,
        BigDecimal yearlyPremium,
        String ageGroup,
        String canton,
        int franchise,
        Breakdown breakdown
) {
}
