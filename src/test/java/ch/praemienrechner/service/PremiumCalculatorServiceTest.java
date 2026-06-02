package ch.praemienrechner.service;

import ch.praemienrechner.domain.AgeGroup;
import ch.praemienrechner.domain.Franchise;
import ch.praemienrechner.dto.CalculationRequest;
import ch.praemienrechner.dto.CalculationResult;
import ch.praemienrechner.dto.CompareRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link PremiumCalculatorService}.
 *
 * <p>Because the service holds only pure calculation logic (no persistence or
 * framework dependencies) it can be instantiated and tested directly, without
 * starting Quarkus — keeping the suite fast.</p>
 */
class PremiumCalculatorServiceTest {

    private final PremiumCalculatorService service = new PremiumCalculatorService();

    private CalculationResult calc(String canton, int age, int franchise, boolean unfall) {
        return service.calculate(new CalculationRequest(canton, age, franchise, unfall));
    }

    // ---------------------------------------------------------------------
    //  Age group boundaries
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Age 18 maps to KIND (upper boundary of the child group)")
    void age18IsKind() {
        assertEquals(AgeGroup.KIND.name(), calc("ZH", 18, 300, false).ageGroup());
    }

    @Test
    @DisplayName("Age 19 maps to JUNGER_ERWACHSENER (lower boundary)")
    void age19IsJungerErwachsener() {
        assertEquals(AgeGroup.JUNGER_ERWACHSENER.name(), calc("ZH", 19, 300, false).ageGroup());
    }

    @Test
    @DisplayName("Age 25 still maps to JUNGER_ERWACHSENER (upper boundary)")
    void age25IsJungerErwachsener() {
        assertEquals(AgeGroup.JUNGER_ERWACHSENER.name(), calc("ZH", 25, 300, false).ageGroup());
    }

    @Test
    @DisplayName("Age 26 maps to ERWACHSENER (lower boundary of adult group)")
    void age26IsErwachsener() {
        assertEquals(AgeGroup.ERWACHSENER.name(), calc("ZH", 26, 300, false).ageGroup());
    }

    @Test
    @DisplayName("Age 0 maps to KIND")
    void age0IsKind() {
        assertEquals(AgeGroup.KIND.name(), calc("ZH", 0, 300, false).ageGroup());
    }

    // ---------------------------------------------------------------------
    //  Franchise discount for all six levels (adult, factor-1.0 canton check)
    // ---------------------------------------------------------------------

    @ParameterizedTest(name = "Franchise {0} CHF -> discount {1}%")
    @CsvSource({
            "300, 0",
            "500, -5",
            "1000, -14",
            "1500, -21",
            "2000, -27",
            "2500, -32"
    })
    @DisplayName("Each franchise level reports the correct discount percentage")
    void franchiseDiscountPercentages(int franchise, int expectedPercent) {
        CalculationResult result = calc("LU", 40, franchise, false);
        assertEquals(expectedPercent, result.breakdown().franchiseDiscount());
    }

    @Test
    @DisplayName("A higher franchise always yields a lower premium")
    void higherFranchiseLowersPremium() {
        BigDecimal f300 = calc("BE", 40, 300, false).monthlyPremium();
        BigDecimal f1000 = calc("BE", 40, 1000, false).monthlyPremium();
        BigDecimal f2500 = calc("BE", 40, 2500, false).monthlyPremium();
        assertTrue(f300.compareTo(f1000) > 0, "300 should be pricier than 1000");
        assertTrue(f1000.compareTo(f2500) > 0, "1000 should be pricier than 2500");
    }

    // ---------------------------------------------------------------------
    //  Unfall (accident coverage) surcharge
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Accident coverage excluded -> 0% surcharge")
    void unfallExcludedHasNoSurcharge() {
        assertEquals(0, calc("LU", 32, 1000, false).breakdown().unfallSurcharge());
    }

    @Test
    @DisplayName("Accident coverage included -> 8% surcharge")
    void unfallIncludedHasEightPercentSurcharge() {
        assertEquals(8, calc("LU", 32, 1000, true).breakdown().unfallSurcharge());
    }

    @Test
    @DisplayName("Including accident coverage increases the premium by ~8%")
    void unfallIncreasesPremiumByEightPercent() {
        BigDecimal without = calc("LU", 32, 1000, false).monthlyPremium();
        BigDecimal with = calc("LU", 32, 1000, true).monthlyPremium();
        BigDecimal expectedWith = without.multiply(new BigDecimal("1.08"))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        // Allow a one-cent tolerance: the service rounds the full-precision product
        // once at the end, whereas this reconstruction rounds the base premium first,
        // so the two can legitimately differ by a single rounding step.
        BigDecimal diff = with.subtract(expectedWith).abs();
        assertTrue(diff.compareTo(new BigDecimal("0.01")) <= 0,
                "expected ~" + expectedWith + " but was " + with);
    }

    // ---------------------------------------------------------------------
    //  Age multiplier effect
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("A child pays less than a young adult, who pays less than an adult")
    void ageMultiplierOrdersPremiums() {
        BigDecimal kind = calc("ZH", 10, 300, false).monthlyPremium();
        BigDecimal junger = calc("ZH", 22, 300, false).monthlyPremium();
        BigDecimal adult = calc("ZH", 40, 300, false).monthlyPremium();
        assertTrue(kind.compareTo(junger) < 0);
        assertTrue(junger.compareTo(adult) < 0);
    }

    // ---------------------------------------------------------------------
    //  Full end-to-end calculation with an exact expected amount
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("LU, age 32, franchise 1000, no accident -> exactly CHF 324.56 / month")
    void exactEndToEndCalculation() {
        // 370.00 (national base) × 1.02 (LU) × 0.86 (franchise 1000)
        //        × 1.00 (no accident) × 1.00 (adult) = 324.564 -> 324.56
        CalculationResult result = calc("LU", 32, 1000, false);

        assertEquals(0, new BigDecimal("324.56").compareTo(result.monthlyPremium()),
                "monthly premium mismatch: " + result.monthlyPremium());
        assertEquals(0, new BigDecimal("3894.72").compareTo(result.yearlyPremium()),
                "yearly premium mismatch: " + result.yearlyPremium());
        assertEquals("Luzern", result.canton());
        assertEquals(AgeGroup.ERWACHSENER.name(), result.ageGroup());
        assertEquals(1000, result.franchise());
        assertEquals(0, Canton_NATIONAL_REFERENCE().compareTo(result.breakdown().basePremium()));
        assertEquals(-14, result.breakdown().franchiseDiscount());
    }

    private static BigDecimal Canton_NATIONAL_REFERENCE() {
        return ch.praemienrechner.domain.Canton.NATIONAL_REFERENCE_PREMIUM;
    }

    @Test
    @DisplayName("Yearly premium is always 12× the monthly premium")
    void yearlyIsTwelveTimesMonthly() {
        CalculationResult result = calc("GE", 45, 500, true);
        BigDecimal expectedYearly = result.monthlyPremium().multiply(BigDecimal.valueOf(12))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        assertEquals(0, expectedYearly.compareTo(result.yearlyPremium()));
    }

    // ---------------------------------------------------------------------
    //  Compare endpoint logic
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Compare returns all six franchise levels sorted by premium ascending")
    void compareReturnsAllFranchisesSortedAscending() {
        List<CalculationResult> results = service.compare(new CompareRequest("LU", 32, false));

        assertEquals(Franchise.values().length, results.size());
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).monthlyPremium()
                            .compareTo(results.get(i).monthlyPremium()) <= 0,
                    "results are not sorted ascending by premium");
        }
        // Cheapest must be the highest franchise (2500), priciest the lowest (300).
        assertEquals(2500, results.get(0).franchise());
        assertEquals(300, results.get(results.size() - 1).franchise());
    }

    // ---------------------------------------------------------------------
    //  Invalid input handling
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Unknown canton code throws IllegalArgumentException")
    void unknownCantonThrows() {
        assertThrows(IllegalArgumentException.class, () -> calc("XX", 30, 300, false));
    }

    @Test
    @DisplayName("Invalid franchise value throws IllegalArgumentException")
    void invalidFranchiseThrows() {
        assertThrows(IllegalArgumentException.class, () -> calc("ZH", 30, 750, false));
    }
}
