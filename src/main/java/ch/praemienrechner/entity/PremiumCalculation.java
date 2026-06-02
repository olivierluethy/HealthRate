package ch.praemienrechner.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Persistent record of a single premium calculation, stored so that the most
 * recent calculations can be served as a history.
 *
 * <p>Extends {@link PanacheEntity}, which provides an auto-generated {@code id}
 * and the active-record style persistence methods used by the service layer.</p>
 */
@Entity
@Table(name = "premium_calculation")
public class PremiumCalculation extends PanacheEntity {

    /** Two-letter canton code, e.g. {@code "LU"}. */
    @Column(nullable = false, length = 2)
    public String canton;

    /** Age of the insured person at the time of calculation. */
    @Column(nullable = false)
    public int age;

    /** Chosen franchise (annual deductible) in CHF. */
    @Column(nullable = false)
    public int franchise;

    /** Whether accident coverage (Unfalleinschluss) was included. */
    @Column(nullable = false)
    public boolean unfalleinschluss;

    /** The resulting monthly premium in CHF. */
    @Column(name = "monthly_premium", nullable = false, precision = 10, scale = 2)
    public BigDecimal monthlyPremium;

    /** Timestamp at which the calculation was performed. */
    @Column(name = "calculated_at", nullable = false)
    public LocalDateTime calculatedAt;

    public PremiumCalculation() {
        // Required by JPA.
    }

    public PremiumCalculation(String canton, int age, int franchise, boolean unfalleinschluss,
                              BigDecimal monthlyPremium, LocalDateTime calculatedAt) {
        this.canton = canton;
        this.age = age;
        this.franchise = franchise;
        this.unfalleinschluss = unfalleinschluss;
        this.monthlyPremium = monthlyPremium;
        this.calculatedAt = calculatedAt;
    }
}
