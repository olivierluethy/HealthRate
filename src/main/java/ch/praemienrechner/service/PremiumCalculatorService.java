package ch.praemienrechner.service;

import ch.praemienrechner.domain.AgeGroup;
import ch.praemienrechner.domain.Canton;
import ch.praemienrechner.domain.Franchise;
import ch.praemienrechner.dto.Breakdown;
import ch.praemienrechner.dto.CalculationRequest;
import ch.praemienrechner.dto.CalculationResult;
import ch.praemienrechner.dto.CompareRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * Core business logic for Swiss health-insurance premium calculation.
 *
 * <p>This service is deliberately free of any framework, persistence or
 * transport concerns: it takes a request, applies the domain rules and returns
 * a result. That keeps it trivially unit-testable and fully decoupled from the
 * REST layer.</p>
 *
 * <p>The monthly premium is assembled from a single national reference premium
 * and four multiplicative factors:</p>
 * <pre>
 *   monthlyPremium = basePremium
 *                  × kantonFactor       (regional cost level)
 *                  × franchiseDiscount  (deductible-based discount)
 *                  × unfallSurcharge    (accident coverage surcharge)
 *                  × ageMultiplier      (age-group reduction)
 * </pre>
 */
@ApplicationScoped
public class PremiumCalculatorService {

    /** Surcharge applied when accident coverage (Unfalleinschluss) is included: +8&nbsp;%. */
    private static final double UNFALL_SURCHARGE_PERCENT = 8;
    private static final double UNFALL_SURCHARGE_FACTOR = 1.0 + (UNFALL_SURCHARGE_PERCENT / 100.0);
    private static final int MONEY_SCALE = 2;
    private static final int MONTHS_PER_YEAR = 12;

    /**
     * Calculates the premium for a single, fully specified request.
     *
     * @param request the calculation request (canton, age, franchise, accident coverage).
     * @return the calculated premium with a transparent breakdown.
     * @throws IllegalArgumentException if the canton or franchise value is invalid.
     */
    public CalculationResult calculate(CalculationRequest request) {
        // 1. Resolve and validate the domain inputs. Invalid codes/values throw
        //    IllegalArgumentException, which the REST layer maps to a 400 response.
        Canton canton = Canton.fromCode(request.canton);
        Franchise franchise = Franchise.fromValue(request.franchise);
        AgeGroup ageGroup = AgeGroup.fromAge(request.age);

        return compute(canton, ageGroup, franchise, request.unfalleinschluss);
    }

    /**
     * Calculates premiums for all six franchise levels for the same person,
     * sorted by monthly premium ascending (cheapest first).
     *
     * @param request the compare request (canton, age, accident coverage).
     * @return one {@link CalculationResult} per franchise level, cheapest first.
     * @throws IllegalArgumentException if the canton is invalid.
     */
    public List<CalculationResult> compare(CompareRequest request) {
        Canton canton = Canton.fromCode(request.canton);
        AgeGroup ageGroup = AgeGroup.fromAge(request.age);

        return java.util.Arrays.stream(Franchise.values())
                .map(franchise -> compute(canton, ageGroup, franchise, request.unfalleinschluss))
                .sorted(Comparator.comparing(CalculationResult::monthlyPremium))
                .toList();
    }

    /**
     * Pure calculation shared by {@link #calculate} and {@link #compare}.
     */
    private CalculationResult compute(Canton canton, AgeGroup ageGroup, Franchise franchise,
                                      boolean unfalleinschluss) {
        // The national reference premium is the starting point for every calculation.
        BigDecimal basePremium = Canton.NATIONAL_REFERENCE_PREMIUM;

        // Determine the accident-coverage surcharge factor (1.08 if included, else 1.00).
        double unfallFactor = unfalleinschluss ? UNFALL_SURCHARGE_FACTOR : 1.0;
        int unfallPercent = unfalleinschluss ? (int) UNFALL_SURCHARGE_PERCENT : 0;

        // Apply all factors. We keep full precision until the final rounding step
        // to avoid accumulating rounding errors across the multiplications.
        BigDecimal monthly = basePremium
                .multiply(BigDecimal.valueOf(canton.getRegionalFactor()))
                .multiply(BigDecimal.valueOf(franchise.getDiscountFactor()))
                .multiply(BigDecimal.valueOf(unfallFactor))
                .multiply(BigDecimal.valueOf(ageGroup.getPremiumMultiplier()))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal yearly = monthly
                .multiply(BigDecimal.valueOf(MONTHS_PER_YEAR))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        Breakdown breakdown = new Breakdown(
                basePremium,
                canton.getRegionalFactor(),
                franchise.getDiscountPercent(),
                unfallPercent,
                ageGroup.getPremiumMultiplier());

        return new CalculationResult(
                monthly,
                yearly,
                ageGroup.name(),
                canton.getFullName(),
                franchise.getValue(),
                breakdown);
    }
}
