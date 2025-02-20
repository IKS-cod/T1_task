package com.t1.task.aspect;

import com.t1.task.exception.ExecutionTimeAspectException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    @Around("@annotation(com.t1.task.aspect.annotation.CustomExecutionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) {
        long startTime = System.currentTimeMillis();

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            throw new ExecutionTimeAspectException();
        }

        long endTime = System.currentTimeMillis();
        logger.info("Method {} executed in {} ms",
                joinPoint.getSignature().getName(),
                (endTime - startTime));
        return result;
    }
}
