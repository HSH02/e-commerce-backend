package com.ecommerce.global.aop.log;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LogTraceAspect {

    private final LogTracer logTracer;

    @Around("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Controller) || " +
            "@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.stereotype.Repository)")
    public Object logTrace(ProceedingJoinPoint joinPoint) throws Throwable {
        TraceStatus status = null;
        try {
            String message = joinPoint.getSignature().toShortString();
            status = logTracer.begin(message);

            Object result = joinPoint.proceed();

            logTracer.end(status);
            return result;
        } catch (Exception e) {
            if (status != null) {
                logTracer.exception(status, e);
            }
            throw e;
        }
    }

}
