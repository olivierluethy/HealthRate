package ch.praemienrechner.domain;

/**
 * Swiss health-insurance age groups (Altersgruppen).
 *
 * <p>Premiums depend on the insured person's age band. Children pay a strongly
 * reduced premium, young adults a moderately reduced one, and adults the full
 * premium. Each group therefore carries a {@code premiumMultiplier} applied on
 * top of the canton base premium.</p>
 *
 * <ul>
 *   <li>{@code KIND} – 0–18 years (≈ 55&nbsp;% of the adult premium)</li>
 *   <li>{@code JUNGER_ERWACHSENER} – 19–25 years (≈ 85&nbsp;% of the adult premium)</li>
 *   <li>{@code ERWACHSENER} – 26+ years (full premium)</li>
 * </ul>
 */
public enum AgeGroup {

    KIND("Kind", 0, 18, 0.55),
    JUNGER_ERWACHSENER("Junger Erwachsener", 19, 25, 0.85),
    ERWACHSENER("Erwachsener", 26, Integer.MAX_VALUE, 1.00);

    private final String label;
    private final int minAge;
    private final int maxAge;
    private final double premiumMultiplier;

    AgeGroup(String label, int minAge, int maxAge, double premiumMultiplier) {
        this.label = label;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.premiumMultiplier = premiumMultiplier;
    }

    /** @return the human-readable German label, e.g. {@code "Junger Erwachsener"}. */
    public String getLabel() {
        return label;
    }

    /** @return inclusive lower age bound of this group. */
    public int getMinAge() {
        return minAge;
    }

    /** @return inclusive upper age bound of this group ({@link Integer#MAX_VALUE} for adults). */
    public int getMaxAge() {
        return maxAge;
    }

    /** @return the multiplier applied to the canton base premium for this age group. */
    public double getPremiumMultiplier() {
        return premiumMultiplier;
    }

    /**
     * Maps an age in years to its {@link AgeGroup}.
     *
     * @param age the insured person's age in completed years; must not be negative.
     * @return the matching age group.
     * @throws IllegalArgumentException if {@code age} is negative.
     */
    public static AgeGroup fromAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age must not be negative: " + age);
        }
        for (AgeGroup group : values()) {
            if (age >= group.minAge && age <= group.maxAge) {
                return group;
            }
        }
        // Unreachable: ERWACHSENER covers everything up to Integer.MAX_VALUE.
        return ERWACHSENER;
    }
}
