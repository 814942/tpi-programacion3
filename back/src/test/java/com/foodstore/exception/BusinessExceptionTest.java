package com.foodstore.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructor_ShouldStoreMessage() {
        var ex = new BusinessException("test message");
        assertEquals("test message", ex.getMessage());
    }
}
