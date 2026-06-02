package ch.praemienrechner.resource;

import ch.praemienrechner.dto.CalculationRequest;
import ch.praemienrechner.dto.CalculationResult;
import ch.praemienrechner.dto.CompareRequest;
import ch.praemienrechner.dto.HistoryEntry;
import ch.praemienrechner.service.CalculationHistoryService;
import ch.praemienrechner.service.PremiumCalculatorService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * REST endpoints for premium calculation, franchise comparison and history.
 *
 * <p>This resource contains no business logic: it only validates input (via
 * Bean Validation), delegates to the service layer and returns the result.</p>
 */
@Path("/api/premium")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Premium", description = "Calculate and compare Swiss health-insurance premiums")
public class PremiumResource {

    @Inject
    PremiumCalculatorService calculatorService;

    @Inject
    CalculationHistoryService historyService;

    /**
     * Calculates the premium for a single franchise level and stores the result.
     *
     * @param request the validated calculation request.
     * @return the calculated premium with a breakdown.
     */
    @POST
    @Path("/calculate")
    @Operation(summary = "Calculate the monthly and yearly premium for a given input")
    public CalculationResult calculate(@Valid CalculationRequest request) {
        CalculationResult result = calculatorService.calculate(request);
        historyService.record(request, result);
        return result;
    }

    /**
     * Calculates premiums for all six franchise levels, cheapest first.
     *
     * @param request the validated compare request.
     * @return one result per franchise level, sorted by monthly premium ascending.
     */
    @POST
    @Path("/compare")
    @Operation(summary = "Compare all six franchise levels for the same person")
    public List<CalculationResult> compare(@Valid CompareRequest request) {
        return calculatorService.compare(request);
    }

    /**
     * Returns the 20 most recent calculations, newest first.
     *
     * @return the recent calculation history.
     */
    @GET
    @Path("/history")
    @Operation(summary = "Return the 20 most recent calculations")
    public List<HistoryEntry> history() {
        return historyService.recentHistory();
    }
}
