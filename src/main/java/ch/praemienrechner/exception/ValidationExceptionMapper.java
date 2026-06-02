package ch.praemienrechner.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

/**
 * Maps Bean Validation failures ({@link ConstraintViolationException}) to a
 * {@code 400 Bad Request} with the uniform {@code { "error": "..." }} body,
 * overriding Quarkus' default validation response shape.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // Join all violation messages so the client sees every problem at once.
        String message = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Invalid request";
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(message))
                .build();
    }
}
