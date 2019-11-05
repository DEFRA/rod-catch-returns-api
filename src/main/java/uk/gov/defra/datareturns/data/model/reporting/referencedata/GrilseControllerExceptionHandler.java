package uk.gov.defra.datareturns.data.model.reporting.referencedata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GrilseControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(GrilseProbabilityController.GrilseCsvException.class)
    public ResponseEntity<GrilseCsvErrorResponse> grilseCsvErrorHandler(final GrilseProbabilityController.GrilseCsvException ex) {
        return new ResponseEntity<>(GrilseCsvErrorResponse.of(ex.getStatus().value(), ex.getMessage(), ex.getErrors()),
                ex.getStatus());
    }

    @AllArgsConstructor(staticName = "of")
    @Getter
    private static class GrilseCsvErrorResponse {
        private final LocalDateTime timestamp = LocalDateTime.now();
        private final int status;
        private final String message;
        private final List<GrilseProbabilityController.GrilseCsvError> errors;
    }
}
