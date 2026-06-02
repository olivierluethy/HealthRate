package ch.praemienrechner.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration tests for the REST layer using RestAssured against a running
 * Quarkus instance (backed by an in-memory H2 database in the test profile).
 */
@QuarkusTest
class PremiumResourceIT {

    @Test
    @DisplayName("POST /api/premium/calculate returns 200 with the expected structure")
    void calculateReturnsCorrectStructure() {
        given()
                .contentType("application/json")
                .body("""
                        { "canton": "LU", "age": 32, "franchise": 1000, "unfalleinschluss": false }
                        """)
                .when()
                .post("/api/premium/calculate")
                .then()
                .statusCode(200)
                .body("monthlyPremium", is(324.56f))
                .body("yearlyPremium", is(3894.72f))
                .body("ageGroup", equalTo("ERWACHSENER"))
                .body("canton", equalTo("Luzern"))
                .body("franchise", equalTo(1000))
                .body("breakdown.kantonFactor", is(1.02f))
                .body("breakdown.franchiseDiscount", is(-14))
                .body("breakdown.unfallSurcharge", is(0));
    }

    @Test
    @DisplayName("Including accident coverage applies the 8% surcharge")
    void calculateWithAccidentCoverage() {
        given()
                .contentType("application/json")
                .body("""
                        { "canton": "LU", "age": 32, "franchise": 1000, "unfalleinschluss": true }
                        """)
                .when()
                .post("/api/premium/calculate")
                .then()
                .statusCode(200)
                .body("breakdown.unfallSurcharge", is(8));
    }

    @Test
    @DisplayName("Negative age returns 400 with a JSON error message")
    void negativeAgeReturns400() {
        given()
                .contentType("application/json")
                .body("""
                        { "canton": "LU", "age": -5, "franchise": 1000, "unfalleinschluss": false }
                        """)
                .when()
                .post("/api/premium/calculate")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    @DisplayName("Unknown canton returns 400 with a JSON error message")
    void unknownCantonReturns400() {
        given()
                .contentType("application/json")
                .body("""
                        { "canton": "XX", "age": 32, "franchise": 1000, "unfalleinschluss": false }
                        """)
                .when()
                .post("/api/premium/calculate")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    @DisplayName("Invalid franchise value returns 400")
    void invalidFranchiseReturns400() {
        given()
                .contentType("application/json")
                .body("""
                        { "canton": "ZH", "age": 32, "franchise": 750, "unfalleinschluss": false }
                        """)
                .when()
                .post("/api/premium/calculate")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    @DisplayName("POST /api/premium/compare returns all six franchise levels sorted ascending")
    void compareReturnsSixLevels() {
        given()
                .contentType("application/json")
                .body("""
                        { "canton": "LU", "age": 32, "unfalleinschluss": false }
                        """)
                .when()
                .post("/api/premium/compare")
                .then()
                .statusCode(200)
                .body("$", hasSize(6))
                .body("[0].franchise", equalTo(2500))
                .body("[5].franchise", equalTo(300));
    }

    @Test
    @DisplayName("GET /api/premium/history returns the recent calculations")
    void historyReturnsRecentCalculations() {
        // Perform a calculation so there is at least one history entry.
        given()
                .contentType("application/json")
                .body("""
                        { "canton": "BE", "age": 40, "franchise": 500, "unfalleinschluss": false }
                        """)
                .when()
                .post("/api/premium/calculate")
                .then()
                .statusCode(200);

        given()
                .when()
                .get("/api/premium/history")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("GET /api/cantons returns all 26 cantons")
    void cantonsReturnsAll26() {
        given()
                .when()
                .get("/api/cantons")
                .then()
                .statusCode(200)
                .body("$", hasSize(26))
                .body("[0].code", notNullValue())
                .body("[0].regionalFactor", notNullValue());
    }
}
