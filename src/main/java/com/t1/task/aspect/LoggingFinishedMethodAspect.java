package com.t1.task.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingFinishedMethodAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFinishedMethodAspect.class);

    @AfterReturning(pointcut = "@annotation(com.t1.task.aspect.annotation.CustomLoggingFinishedMethod)",
            returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("Method {} has successfully completed. Result: {}",
                joinPoint.getSignature().getName(),
                result);
    }
}
