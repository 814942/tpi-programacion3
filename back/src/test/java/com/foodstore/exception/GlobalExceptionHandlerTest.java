package com.foodstore.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {

        @GetMapping("/test/resource-not-found")
        public void throwResourceNotFound() {
            throw new ResourceNotFoundException("Test", "id", "1");
        }

        @GetMapping("/test/business-error")
        public void throwBusinessException() {
            throw new BusinessException("Business error");
        }

        @PostMapping("/test/validation-error")
        public void throwValidationError(@Valid @RequestBody TestRequest request) {
        }

        @PostMapping("/test/enum-invalid")
        public void throwEnumValidation(@RequestBody EnumRequest request) {
        }

        @PostMapping("/test/type-mismatch")
        public void throwTypeMismatch(@RequestBody TypeMismatchRequest request) {
        }

        @GetMapping("/test/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Access denied");
        }

        @GetMapping("/test/generic-error")
        public void throwGeneric() {
            throw new RuntimeException("Unexpected error");
        }
    }

    record TestRequest(@NotBlank String name) {
    }

    enum TestRol {
        ADMIN,
        USUARIO
    }

    record EnumRequest(TestRol rol) {
    }

    record TypeMismatchRequest(Integer age) {
    }

    @Test
    void handleResourceNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("resource_not_found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void handleBusinessException_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/test/business-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("business_error"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void handleValidation_ShouldReturn400WithFields() throws Exception {
        mockMvc.perform(post("/test/validation-error")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.name").value("must not be blank"));
    }

    @Test
    void handleAccessDenied_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("access_denied"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void handleGeneric_ShouldReturn500WithoutStackTrace() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("internal_error"))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void handleEnumInvalidValue_ShouldReturnEnumSpecificMessage() throws Exception {
        mockMvc.perform(post("/test/enum-invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rol\":\"INVALID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"))
                .andExpect(jsonPath("$.message").value("Valor inválido para un campo enumerado. Valores aceptados: [ADMIN, USUARIO]"));
    }

    @Test
    void handleNonEnumInvalidValue_ShouldReturnGenericMessage() throws Exception {
        mockMvc.perform(post("/test/type-mismatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"age\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"))
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud es inválido. Verificá que el JSON esté bien formado y los valores sean correctos."));
    }
}
