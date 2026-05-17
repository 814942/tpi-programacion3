package com.foodstore.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_ShouldSetCorrectMessage() {
        var ex = new ResourceNotFoundException("Usuario", "email", "foo@bar.com");
        assertEquals("Usuario con email 'foo@bar.com' no encontrado", ex.getMessage());
    }

    @Test
    void getters_ShouldReturnCorrectValues() {
        var ex = new ResourceNotFoundException("Categoria", "id", "5");
        assertEquals("Categoria", ex.getResourceName());
        assertEquals("id", ex.getFieldName());
        assertEquals("5", ex.getFieldValue());
    }
}
