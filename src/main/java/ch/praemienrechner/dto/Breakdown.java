package ch.praemienrechner.dto;

import java.math.BigDecimal;

/**
 * Transparent breakdown of the factors that produced a monthly premium.
 *
 * <p>Returned as part of {@link CalculationResult} so that clients can show the
 * user exactly how the premium was assembled.</p>
 *
 * @param basePremium      the national reference base premium in CHF (before any factor)
 * @param kantonFactor     the regional multiplier applied for the canton, e.g. {@code 1.02}
 * @param franchiseDiscount the franchise discount as a percentage, e.g. {@code -14}
 * @param unfallSurcharge  the accident-coverage surcharge as a percentage, {@code 0} or {@code 8}
 * @param ageMultiplier    the age-group multiplier, e.g. {@code 0.55} for a child
 */
public record Breakdown(
        BigDecimal basePremium,
        double kantonFactor,
        int franchiseDiscount,
        int unfallSurcharge,
        double ageMultiplier
) {
}
