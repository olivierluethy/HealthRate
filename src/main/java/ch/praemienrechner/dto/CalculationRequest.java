package ch.praemienrechner.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Incoming request body for {@code POST /api/premium/calculate}.
 *
 * <p>All fields are validated with Bean Validation annotations; violations are
 * translated into a {@code 400 Bad Request} with a JSON error message.</p>
 */
public class CalculationRequest {

    /** Two-letter canton code, e.g. {@code "LU"}. */
    @NotBlank(message = "Canton must not be empty")
    public String canton;

    /** Age of the insured person; must be between 0 and 120. */
    @Min(value = 0, message = "Age must be between 0 and 120")
    @Max(value = 120, message = "Age must be between 0 and 120")
    public int age;

    /** Chosen franchise in CHF (300, 500, 1000, 1500, 2000 or 2500). */
    @NotNull(message = "Franchise must be provided")
    public Integer franchise;

    /** Whether accident coverage (Unfalleinschluss) should be included. */
    public boolean unfalleinschluss;

    public CalculationRequest() {
    }

    public CalculationRequest(String canton, int age, Integer franchise, boolean unfalleinschluss) {
        this.canton = canton;
        this.age = age;
        this.franchise = franchise;
        this.unfalleinschluss = unfalleinschluss;
    }
}
