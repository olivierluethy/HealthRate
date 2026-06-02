package ch.praemienrechner.dto;

import java.math.BigDecimal;

/**
 * Lightweight representation of a canton for the {@code GET /api/cantons} endpoint.
 *
 * @param code           the two-letter canton code, e.g. {@code "ZH"}
 * @param name           the full canton name, e.g. {@code "Zürich"}
 * @param regionalFactor the regional cost multiplier, e.g. {@code 1.18}
 * @param basePremium    the canton-specific adult base premium in CHF
 */
public record CantonDto(
        String code,
        String name,
        double regionalFactor,
        BigDecimal basePremium
) {
}
