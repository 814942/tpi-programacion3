package com.foodstore.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void constructor_ShouldSetValues() {
        var response = new ErrorResponse("error", "msg", 400, "2026-01-01T00:00:00", "/path", null);
        assertEquals("error", response.error());
        assertEquals("msg", response.message());
        assertEquals(400, response.status());
        assertEquals("2026-01-01T00:00:00", response.timestamp());
        assertEquals("/path", response.path());
        assertNull(response.fields());
    }

    @Test
    void staticFactory_WithoutFields_ShouldGenerateTimestamp() {
        var response = ErrorResponse.of("err", "msg", 404, "/path");
        assertEquals("err", response.error());
        assertEquals("msg", response.message());
        assertEquals(404, response.status());
        assertEquals("/path", response.path());
        assertNotNull(response.timestamp());
        assertNull(response.fields());
    }

    @Test
    void staticFactory_WithFields_ShouldIncludeFields() {
        var fields = Map.of("name", "El nombre es obligatorio");
        var response = ErrorResponse.of("err", "msg", 400, "/path", fields);
        assertEquals(fields, response.fields());
        assertNotNull(response.timestamp());
    }
}
