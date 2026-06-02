package ch.praemienrechner.resource;

import ch.praemienrechner.domain.Canton;
import ch.praemienrechner.dto.CantonDto;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Read-only endpoint exposing the list of Swiss cantons and their pricing data.
 *
 * <p>Primarily used by the frontend to populate the canton dropdown.</p>
 */
@Path("/api/cantons")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Cantons", description = "Reference data for the 26 Swiss cantons")
public class CantonResource {

    /**
     * Returns all 26 cantons with their code, full name, regional factor and base premium,
     * sorted alphabetically by full name.
     *
     * @return the list of cantons.
     */
    @GET
    @Operation(summary = "List all 26 Swiss cantons with their pricing factors")
    public List<CantonDto> listCantons() {
        return Arrays.stream(Canton.values())
                .map(c -> new CantonDto(c.getCode(), c.getFullName(), c.getRegionalFactor(), c.getBasePremium()))
                .sorted(Comparator.comparing(CantonDto::name))
                .toList();
    }
}
