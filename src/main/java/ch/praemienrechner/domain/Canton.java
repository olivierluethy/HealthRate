package ch.praemienrechner.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * The 26 Swiss cantons (Kantone) together with the data required to price a
 * health-insurance premium for a resident of that canton.
 *
 * <p>In the Swiss system premiums vary strongly by region because health-care
 * costs differ between cantons. We model this with a {@code regionalFactor}: a
 * multiplier applied to a single national reference premium
 * ({@link #NATIONAL_REFERENCE_PREMIUM}). A factor of {@code 1.00} represents the
 * national average, {@code 1.25} a 25&nbsp;% more expensive canton (e.g. Genf)
 * and {@code 0.95} a cheaper canton (e.g. Appenzell Innerrhoden).</p>
 *
 * <p>The factors below are illustrative values inspired by the 2024 Swiss
 * premium landscape and are intended for demonstration purposes only.</p>
 */
public enum Canton {

    // --- Cantons with explicitly specified factors ---
    ZH("Zürich", 1.18),
    BE("Bern", 1.05),
    LU("Luzern", 1.02),
    BS("Basel-Stadt", 1.22),
    BL("Basel-Landschaft", 1.15),
    GE("Genf", 1.25),
    VD("Waadt", 1.20),
    TI("Tessin", 1.12),
    SG("St. Gallen", 1.08),
    AG("Aargau", 1.06),

    // --- Remaining 16 cantons with realistic factors between 0.95 and 1.15 ---
    UR("Uri", 0.96),
    SZ("Schwyz", 1.00),
    OW("Obwalden", 0.97),
    NW("Nidwalden", 0.96),
    GL("Glarus", 1.01),
    ZG("Zug", 1.00),
    FR("Freiburg", 1.07),
    SO("Solothurn", 1.04),
    SH("Schaffhausen", 1.03),
    AR("Appenzell Ausserrhoden", 0.98),
    AI("Appenzell Innerrhoden", 0.95),
    GR("Graubünden", 0.99),
    TG("Thurgau", 1.01),
    VS("Wallis", 1.09),
    NE("Neuenburg", 1.14),
    JU("Jura", 1.10);

    /**
     * National reference premium in CHF for an adult ({@link AgeGroup#ERWACHSENER}),
     * lowest franchise (CHF&nbsp;300) and no accident coverage, at a regional factor
     * of {@code 1.00}. All canton-specific base premiums are derived from this value.
     */
    public static final BigDecimal NATIONAL_REFERENCE_PREMIUM = new BigDecimal("370.00");

    private final String fullName;
    private final double regionalFactor;

    Canton(String fullName, double regionalFactor) {
        this.fullName = fullName;
        this.regionalFactor = regionalFactor;
    }

    /** @return the official two-letter canton code, e.g. {@code "ZH"}. */
    public String getCode() {
        return name();
    }

    /** @return the full canton name, e.g. {@code "Zürich"}. */
    public String getFullName() {
        return fullName;
    }

    /** @return the regional cost multiplier applied to the national reference premium. */
    public double getRegionalFactor() {
        return regionalFactor;
    }

    /**
     * The canton-specific base monthly premium in CHF for an adult, lowest
     * franchise and no accident coverage. Computed as
     * {@code NATIONAL_REFERENCE_PREMIUM × regionalFactor}, rounded to two decimals.
     *
     * @return realistic base premium, typically between CHF&nbsp;350 and CHF&nbsp;500.
     */
    public BigDecimal getBasePremium() {
        return NATIONAL_REFERENCE_PREMIUM
                .multiply(BigDecimal.valueOf(regionalFactor))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Resolves a canton from its two-letter code (case-insensitive).
     *
     * @param code the canton code, e.g. {@code "lu"} or {@code "LU"}.
     * @return the matching {@link Canton}.
     * @throws IllegalArgumentException if the code does not match any Swiss canton.
     */
    public static Canton fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Canton code must not be empty");
        }
        String normalised = code.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(c -> c.name().equals(normalised))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown canton code: '" + code + "'. Expected one of the 26 Swiss canton codes."));
    }
}
