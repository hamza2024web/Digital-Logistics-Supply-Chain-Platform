package com.spring.digital_logistics.service;

import com.spring.digital_logistics.exception.*;
import com.spring.digital_logistics.exception.GlobalExceptionHandler.ErrorResponse;
import com.spring.digital_logistics.exception.GlobalExceptionHandler.ValidationErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    // ========== TESTS ResourceNotFoundException ==========

    @Test
    void handleResourceNotFound_shouldReturnNotFoundStatus() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Ressource non trouvée");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFound(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Ressource non trouvée", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleResourceNotFound_withDifferentMessage_shouldReturnCorrectMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Produit avec ID 123 non trouvé");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFound(ex);

        assertEquals("Produit avec ID 123 non trouvé", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
    }

    // ========== TESTS EmailAlreadyUsedException ==========

    @Test
    void handleEmailAlreadyUsed_shouldReturnConflictStatus() {
        EmailAlreadyUsedException ex = new EmailAlreadyUsedException("Email déjà utilisé");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEmailAlreadyUsed(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Email déjà utilisé", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleEmailAlreadyUsed_shouldIncludeTimestamp() {
        EmailAlreadyUsedException ex = new EmailAlreadyUsedException("test@test.com existe déjà");
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEmailAlreadyUsed(ex);

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertNotNull(response.getBody().getTimestamp());
        assertTrue(response.getBody().getTimestamp().isAfter(before));
        assertTrue(response.getBody().getTimestamp().isBefore(after));
    }

    // ========== TESTS WarehouseCodeAlreadyUsedException ==========

    @Test
    void handleCodeAlreadyUsed_shouldReturnConflictStatus() {
        WarehouseCodeAlreadyUsedException ex = new WarehouseCodeAlreadyUsedException("Code entrepôt déjà utilisé");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleCodeAlreadyUsed(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Code entrepôt déjà utilisé", response.getBody().getMessage());
    }

    @Test
    void handleCodeAlreadyUsed_withSpecificCode_shouldReturnCorrectMessage() {
        WarehouseCodeAlreadyUsedException ex = new WarehouseCodeAlreadyUsedException("Le code 'W-001' est déjà utilisé");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleCodeAlreadyUsed(ex);

        assertEquals("Le code 'W-001' est déjà utilisé", response.getBody().getMessage());
    }

    // ========== TESTS StockUnavailableException ==========

    @Test
    void handleStockUnavailable_shouldReturnBadRequestStatus() {
        StockUnavailableException ex = new StockUnavailableException("Stock insuffisant");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleStockUnavailable(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Stock insuffisant", response.getBody().getMessage());
    }

    @Test
    void handleStockUnavailable_withDetailedMessage_shouldReturnCorrectMessage() {
        StockUnavailableException ex = new StockUnavailableException("Stock disponible insuffisant pour le produit SKU-001. Disponible: 5, Demandé: 10");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleStockUnavailable(ex);

        assertTrue(response.getBody().getMessage().contains("SKU-001"));
        assertTrue(response.getBody().getMessage().contains("Disponible: 5"));
    }

    // ========== TESTS PurchaseOrderStatusException ==========

    @Test
    void handlePurchaseOrderStatusException_shouldReturnBadRequestStatus() {
        PurchaseOrderStatusException ex = new PurchaseOrderStatusException("Statut de commande invalide");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePurchaseOrderStatusException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Statut de commande invalide", response.getBody().getMessage());
    }

    @Test
    void handlePurchaseOrderStatusException_withSpecificStatus_shouldReturnCorrectMessage() {
        PurchaseOrderStatusException ex = new PurchaseOrderStatusException("Impossible de passer du statut PENDING à CANCELLED");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePurchaseOrderStatusException(ex);

        assertTrue(response.getBody().getMessage().contains("PENDING"));
        assertTrue(response.getBody().getMessage().contains("CANCELLED"));
    }

    // ========== TESTS SecurityException ==========

    @Test
    void handleSecurityException_shouldReturnForbiddenStatus() {
        SecurityException ex = new SecurityException("Accès non autorisé");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleSecurityException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Accès non autorisé", response.getBody().getMessage());
    }

    @Test
    void handleSecurityException_withUserContext_shouldReturnCorrectMessage() {
        SecurityException ex = new SecurityException("Vous n'êtes pas autorisé à modifier cette commande.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleSecurityException(ex);

        assertEquals("Vous n'êtes pas autorisé à modifier cette commande.", response.getBody().getMessage());
        assertEquals(403, response.getBody().getStatus());
    }

    // ========== TESTS IllegalStateException ==========

    @Test
    void handleIllegalStateException_shouldReturnBadRequestStatus() {
        IllegalStateException ex = new IllegalStateException("État invalide");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalStateException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("État invalide", response.getBody().getMessage());
    }

    @Test
    void handleIllegalStateException_withOrderStatus_shouldReturnCorrectMessage() {
        IllegalStateException ex = new IllegalStateException("Seule une commande avec le statut CREATED peut être réservée");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalStateException(ex);

        assertTrue(response.getBody().getMessage().contains("CREATED"));
        assertTrue(response.getBody().getMessage().contains("réservée"));
    }

    // ========== TESTS AccessDeniedException ==========

    @Test
    void handleAccessDeniedException_shouldReturnForbiddenStatus() {
        AccessDeniedException ex = new AccessDeniedException("Accès refusé");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Accès refusé. Vous n'avez pas les droits nécessaires.", response.getBody().getMessage());
    }

    @Test
    void handleAccessDeniedException_shouldReturnStandardMessage() {
        AccessDeniedException ex = new AccessDeniedException("Any message");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(ex);

        // Le message est toujours le même, peu importe le message d'origine
        assertEquals("Accès refusé. Vous n'avez pas les droits nécessaires.", response.getBody().getMessage());
    }

    // ========== TESTS MethodArgumentNotValidException ==========

    @Test
    void handleValidationExceptions_shouldReturnBadRequestWithErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("user", "email", "L'email est obligatoire");
        FieldError fieldError2 = new FieldError("user", "password", "Le mot de passe doit contenir au moins 8 caractères");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ValidationErrorResponse> response = globalExceptionHandler.handleValidationExceptions(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Erreur de validation", response.getBody().getMessage());
        assertNotNull(response.getBody().getErrors());
        assertEquals(2, response.getBody().getErrors().size());
        assertTrue(response.getBody().getErrors().containsKey("email"));
        assertTrue(response.getBody().getErrors().containsKey("password"));
        assertEquals("L'email est obligatoire", response.getBody().getErrors().get("email"));
        assertEquals("Le mot de passe doit contenir au moins 8 caractères", response.getBody().getErrors().get("password"));
    }

    @Test
    void handleValidationExceptions_withSingleError_shouldReturnOneError() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("product", "sku", "Le SKU est obligatoire");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ValidationErrorResponse> response = globalExceptionHandler.handleValidationExceptions(ex);

        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("Le SKU est obligatoire", response.getBody().getErrors().get("sku"));
    }

    @Test
    void handleValidationExceptions_shouldIncludeTimestamp() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of());

        ResponseEntity<ValidationErrorResponse> response = globalExceptionHandler.handleValidationExceptions(ex);

        assertNotNull(response.getBody().getTimestamp());
    }

    // ========== TESTS Exception générique ==========

    @Test
    void handleAll_shouldReturnInternalServerError() {
        Exception ex = new Exception("Erreur inattendue");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAll(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Erreur interne"));
        assertTrue(response.getBody().getMessage().contains("Erreur inattendue"));
    }

    @Test
    void handleAll_withNullPointerException_shouldReturnInternalServerError() {
        NullPointerException ex = new NullPointerException("Objet null");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAll(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Objet null"));
    }

    @Test
    void handleAll_withRuntimeException_shouldReturnInternalServerError() {
        RuntimeException ex = new RuntimeException("Erreur d'exécution");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAll(ex);

        assertEquals(500, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Erreur d'exécution"));
    }

    @Test
    void handleAll_shouldIncludeTimestamp() {
        Exception ex = new Exception("Test");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAll(ex);

        assertNotNull(response.getBody().getTimestamp());
    }

    // ========== TESTS ErrorResponse ==========

    @Test
    void errorResponse_shouldHaveCorrectFields() {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse errorResponse = new ErrorResponse(404, "Not Found", now);

        assertEquals(404, errorResponse.getStatus());
        assertEquals("Not Found", errorResponse.getMessage());
        assertEquals(now, errorResponse.getTimestamp());
    }

    @Test
    void errorResponse_settersAndGetters_shouldWork() {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse errorResponse = new ErrorResponse(200, "OK", now);

        errorResponse.setStatus(500);
        errorResponse.setMessage("Internal Error");
        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        errorResponse.setTimestamp(newTime);

        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal Error", errorResponse.getMessage());
        assertEquals(newTime, errorResponse.getTimestamp());
    }

    @Test
    void errorResponse_withNullMessage_shouldAcceptNull() {
        ErrorResponse errorResponse = new ErrorResponse(404, null, LocalDateTime.now());

        assertNull(errorResponse.getMessage());
        assertEquals(404, errorResponse.getStatus());
    }

    // ========== TESTS ValidationErrorResponse ==========

    @Test
    void validationErrorResponse_shouldHaveErrorsMap() {
        Map<String, String> errors = Map.of(
                "field1", "Error 1",
                "field2", "Error 2"
        );
        LocalDateTime now = LocalDateTime.now();

        ValidationErrorResponse response = new ValidationErrorResponse(
                400,
                "Validation Error",
                now,
                errors
        );

        assertEquals(400, response.getStatus());
        assertEquals("Validation Error", response.getMessage());
        assertEquals(now, response.getTimestamp());
        assertNotNull(response.getErrors());
        assertEquals(2, response.getErrors().size());
        assertEquals("Error 1", response.getErrors().get("field1"));
        assertEquals("Error 2", response.getErrors().get("field2"));
    }

    @Test
    void validationErrorResponse_settersAndGetters_shouldWork() {
        Map<String, String> errors = Map.of("field", "error");
        ValidationErrorResponse response = new ValidationErrorResponse(
                400,
                "Validation Error",
                LocalDateTime.now(),
                errors
        );

        Map<String, String> newErrors = Map.of("newField", "newError");
        response.setErrors(newErrors);

        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().containsKey("newField"));
        assertEquals("newError", response.getErrors().get("newField"));
    }

    @Test
    void validationErrorResponse_withEmptyErrors_shouldWork() {
        Map<String, String> errors = Map.of();
        ValidationErrorResponse response = new ValidationErrorResponse(
                400,
                "Validation Error",
                LocalDateTime.now(),
                errors
        );

        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void validationErrorResponse_extendsErrorResponse_shouldInheritMethods() {
        ValidationErrorResponse response = new ValidationErrorResponse(
                400,
                "Test",
                LocalDateTime.now(),
                Map.of()
        );

        response.setStatus(422);
        response.setMessage("Unprocessable Entity");

        assertEquals(422, response.getStatus());
        assertEquals("Unprocessable Entity", response.getMessage());
    }
}