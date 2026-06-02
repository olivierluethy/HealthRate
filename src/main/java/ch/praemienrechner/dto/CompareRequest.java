package ch.praemienrechner.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Incoming request body for {@code POST /api/premium/compare}.
 *
 * <p>Identical to {@link CalculationRequest} but without a franchise, because
 * the compare endpoint evaluates all six franchise levels at once.</p>
 */
public class CompareRequest {

    /** Two-letter canton code, e.g. {@code "LU"}. */
    @NotBlank(message = "Canton must not be empty")
    public String canton;

    /** Age of the insured person; must be between 0 and 120. */
    @Min(value = 0, message = "Age must be between 0 and 120")
    @Max(value = 120, message = "Age must be between 0 and 120")
    public int age;

    /** Whether accident coverage (Unfalleinschluss) should be included. */
    public boolean unfalleinschluss;

    public CompareRequest() {
    }

    public CompareRequest(String canton, int age, boolean unfalleinschluss) {
        this.canton = canton;
        this.age = age;
        this.unfalleinschluss = unfalleinschluss;
    }
}
