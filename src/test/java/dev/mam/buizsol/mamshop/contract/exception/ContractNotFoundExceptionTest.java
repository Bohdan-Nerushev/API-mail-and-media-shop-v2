package dev.mam.buizsol.mamshop.contract.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("ContractNotFoundException Tests")
class ContractNotFoundExceptionTest {

    @Test
    @DisplayName("Constructor with message should set message correctly")
    void shouldSetMessageWhenConstructorWithMessageIsCalled() {
        final String message = "Contract not found";

        final ContractNotFoundException exception = new ContractNotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Constructor with message and cause should set both correctly")
    void shouldSetMessageAndCauseWhenConstructorIsCalled() {
        final String message = "Contract not found";
        final Throwable cause = new RuntimeException("Root cause");

        final ContractNotFoundException exception = new ContractNotFoundException(message, cause);

        assertEquals(message, exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    @DisplayName("Constructor with message and null cause should set message and null cause")
    void shouldSetMessageAndNullCauseWhenConstructorIsCalled() {
        final String message = "Contract not found";

        final ContractNotFoundException exception = new ContractNotFoundException(message, null);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
}
