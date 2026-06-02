package ch.praemienrechner.domain;

import java.util.Arrays;

/**
 * The selectable annual deductible (Franchise) levels in CHF.
 *
 * <p>By choosing a higher franchise the insured person accepts to pay more
 * out-of-pocket before the insurer contributes, and in return receives a
 * premium discount. The discounts modelled here follow the typical Swiss
 * structure: the CHF&nbsp;300 base level has no discount, and each higher level
 * grants a progressively larger reduction.</p>
 *
 * <p>{@code discountPercent} is the human-facing percentage (e.g. {@code -14}),
 * while {@code discountFactor} is the multiplier used in the calculation
 * (e.g. {@code 0.86} for a 14&nbsp;% reduction).</p>
 */
public enum Franchise {

    CHF_300(300, 0),
    CHF_500(500, -5),
    CHF_1000(1000, -14),
    CHF_1500(1500, -21),
    CHF_2000(2000, -27),
    CHF_2500(2500, -32);

    private final int value;
    private final int discountPercent;
    private final double discountFactor;

    Franchise(int value, int discountPercent) {
        this.value = value;
        this.discountPercent = discountPercent;
        // discountPercent is negative (or zero); a -14 % discount becomes a 0.86 factor.
        this.discountFactor = 1.0 + (discountPercent / 100.0);
    }

    /** @return the franchise amount in CHF, e.g. {@code 1000}. */
    public int getValue() {
        return value;
    }

    /** @return the discount as a (negative) percentage, e.g. {@code -14}. */
    public int getDiscountPercent() {
        return discountPercent;
    }

    /** @return the multiplier applied to the premium, e.g. {@code 0.86} for a 14&nbsp;% discount. */
    public double getDiscountFactor() {
        return discountFactor;
    }

    /**
     * Resolves a franchise level from its CHF value.
     *
     * @param value the franchise amount in CHF, e.g. {@code 1000}.
     * @return the matching {@link Franchise}.
     * @throws IllegalArgumentException if the value is not one of the allowed levels.
     */
    public static Franchise fromValue(int value) {
        return Arrays.stream(values())
                .filter(f -> f.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid franchise: " + value + ". Allowed values are 300, 500, 1000, 1500, 2000, 2500."));
    }
}
