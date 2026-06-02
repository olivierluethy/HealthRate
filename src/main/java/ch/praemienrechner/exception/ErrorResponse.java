package ch.praemienrechner.exception;

/**
 * Uniform JSON error payload: {@code { "error": "..." }}.
 *
 * @param error a human-readable description of what went wrong.
 */
public record ErrorResponse(String error) {
}
