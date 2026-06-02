package ch.praemienrechner.service;

import ch.praemienrechner.dto.CalculationRequest;
import ch.praemienrechner.dto.CalculationResult;
import ch.praemienrechner.dto.HistoryEntry;
import ch.praemienrechner.entity.PremiumCalculation;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Persists calculations and serves the calculation history.
 *
 * <p>Separated from {@link PremiumCalculatorService} so that the pure
 * calculation logic stays free of persistence concerns. This service owns all
 * database interaction for premium calculations.</p>
 */
@ApplicationScoped
public class CalculationHistoryService {

    private static final int HISTORY_LIMIT = 20;

    /**
     * Stores a completed calculation for later retrieval via the history endpoint.
     *
     * @param request the original request.
     * @param result  the calculated result whose monthly premium is recorded.
     */
    @Transactional
    public void record(CalculationRequest request, CalculationResult result) {
        PremiumCalculation entry = new PremiumCalculation(
                request.canton.trim().toUpperCase(),
                request.age,
                request.franchise,
                request.unfalleinschluss,
                result.monthlyPremium(),
                LocalDateTime.now());
        entry.persist();
    }

    /**
     * Returns the {@value #HISTORY_LIMIT} most recent calculations, newest first.
     *
     * @return a list of history entries ordered by calculation time descending.
     */
    public List<HistoryEntry> recentHistory() {
        return PremiumCalculation
                .findAll(Sort.by("calculatedAt").descending())
                .page(Page.ofSize(HISTORY_LIMIT))
                .<PremiumCalculation>list()
                .stream()
                .map(c -> new HistoryEntry(
                        c.id, c.canton, c.age, c.franchise,
                        c.unfalleinschluss, c.monthlyPremium, c.calculatedAt))
                .toList();
    }
}
