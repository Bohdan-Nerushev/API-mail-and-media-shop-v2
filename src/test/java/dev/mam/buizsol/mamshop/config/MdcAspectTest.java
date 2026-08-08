package dev.mam.buizsol.mamshop.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("MdcAspect Tests")
class MdcAspectTest {

    private MdcAspect mdcAspect;
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        mdcAspect = new MdcAspect();
        joinPoint = mock(ProceedingJoinPoint.class);
        MDC.clear();
    }

    @Test
    @DisplayName("Should put and remove correlationId when none exists")
    void shouldManageMdcWhenNoneExists() throws Throwable {
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertNotNull(MDC.get("correlationId"));
            return "Result";
        });

        Object result = mdcAspect.manageMdcContext(joinPoint);

        assertEquals("Result", result);
        assertNull(MDC.get("correlationId"));
    }

    @Test
    @DisplayName("Should not remove correlationId when it already existed")
    void shouldNotRemoveMdcWhenAlreadyExists() throws Throwable {
        String existingId = "existing-id";
        MDC.put("correlationId", existingId);

        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertEquals(existingId, MDC.get("correlationId"));
            return "Result";
        });

        Object result = mdcAspect.manageMdcContext(joinPoint);

        assertEquals("Result", result);
        assertEquals(existingId, MDC.get("correlationId"));
    }

    @Test
    @DisplayName("Should remove correlationId even if Exception occurs")
    void shouldRemoveMdcOnException() throws Throwable {
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Error"));

        assertThrows(RuntimeException.class, () -> mdcAspect.manageMdcContext(joinPoint));

        assertNull(MDC.get("correlationId"));
    }
}
