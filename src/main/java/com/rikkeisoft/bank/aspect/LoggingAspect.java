package com.rikkeisoft.bank.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.rikkeisoft.bank.service..*(..))")
    public void beforeService(JoinPoint joinPoint) {
        log.info("Start {}", joinPoint.getSignature().toShortString());
    }

    @AfterReturning("execution(* com.rikkeisoft.bank.service..*(..))")
    public void afterService(JoinPoint joinPoint) {
        log.info("Success {}", joinPoint.getSignature().toShortString());
    }

    @AfterThrowing(pointcut = "execution(* com.rikkeisoft.bank.service..*(..))", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.error("Error {}: {}", joinPoint.getSignature().toShortString(), ex.getMessage());
    }
}
