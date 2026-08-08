package dev.mam.buizsol.mamshop.config;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class MdcAspect {

    private static final String CORRELATION_ID_KEY = "correlationId";

    @Around(value = "execution(public * dev.mam.buizsol.mamshop..service.*.*(..))")
    public Object manageMdcContext(final ProceedingJoinPoint joinPoint) throws Throwable {
        final boolean isNewContext = MDC.get(CORRELATION_ID_KEY) == null;
        try {
            if (isNewContext) {
                MDC.put(CORRELATION_ID_KEY, UUID.randomUUID().toString());
            }

            return joinPoint.proceed();
        } finally {
            if (isNewContext) {
                MDC.remove(CORRELATION_ID_KEY);
            }
        }
    }
}
