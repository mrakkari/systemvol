package com.flight.reservation.exception;

import com.flight.reservation.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(VolNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVolNotFound(VolNotFoundException ex, WebRequest request) {
        logger.error("Vol non trouvé: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "VOL_NOT_FOUND",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PlacesInsuffisantesException.class)
    public ResponseEntity<ErrorResponse> handlePlacesInsuffisantes(PlacesInsuffisantesException ex, WebRequest request) {
        logger.error("Places insuffisantes: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "INSUFFICIENT_SEATS",
                ex.getMessage(),
                String.format("Places disponibles: %d, Places demandées: %d", ex.getPlacesDisponibles(), ex.getPlacesDemandees())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<ErrorResponse> handleReservationConflict(ReservationConflictException ex, WebRequest request) {
        logger.error("Conflit lors de la réservation: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "RESERVATION_CONFLICT",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        String details = bindingResult.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "Erreurs de validation dans la requête",
                details
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse errorResponse = new ErrorResponse(
                "CONSTRAINT_VIOLATION",
                "Violation de contraintes",
                details
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex, WebRequest request) {
        logger.warn("Conflit de concurrence détecté: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "CONCURRENCY_CONFLICT",
                "La réservation a été modifiée par une autre transaction. Veuillez réessayer."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest request) {
        logger.error("Erreur inattendue: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_ERROR",
                "Une erreur inattendue s'est produite"
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}