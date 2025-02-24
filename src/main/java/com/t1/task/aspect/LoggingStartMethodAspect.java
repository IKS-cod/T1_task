package com.t1.task.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingStartMethodAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingStartMethodAspect.class);

    @Before("@annotation(com.t1.task.aspect.annotation.CustomLoggingFinishedMethod)")
    public void logBeforeMethod(JoinPoint joinPoint) {
        logger.info("Method {} is about to be called.",
                joinPoint.getSignature().getName());
    }
}
