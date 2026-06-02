package ch.praemienrechner.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read-only view of a stored {@code PremiumCalculation} for the history endpoint.
 *
 * @param id               database identifier of the stored calculation
 * @param canton           two-letter canton code
 * @param age              age used in the calculation
 * @param franchise        franchise level in CHF
 * @param unfalleinschluss whether accident coverage was included
 * @param monthlyPremium   the resulting monthly premium in CHF
 * @param calculatedAt     timestamp at which the calculation was performed
 */
public record HistoryEntry(
        Long id,
        String canton,
        int age,
        int franchise,
        boolean unfalleinschluss,
        BigDecimal monthlyPremium,
        LocalDateTime calculatedAt
) {
}
