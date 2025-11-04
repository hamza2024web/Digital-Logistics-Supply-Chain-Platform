package com.spring.digital_logistics.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // Ciblé tous les methodes de service
    @Pointcut("within(com.spring.digital_logistics.service..*)")
    public void allServiceMethods() {};

    // On logue aussi les argumetns pour plus de conexte
    @Before("allServiceMethods()")
    public void logBeforeMethodCall(JoinPoint joinPoint){
        String methodName = joinPoint.getSignature().toShortString(); //donne un nom plus précis
        String args = Arrays.toString(joinPoint.getArgs());
        log.info("[AOP-BEFORE] ==> Appel de {} avec les arguments: {}", methodName , args);
    }

    @AfterReturning(pointcut = "allServiceMethods()", returning = "result")
    public void logAfterMethodReturn(JoinPoint joinPoint , Object result){
        String methodName = joinPoint.getSignature().toShortString();
        log.info("[AOP-AFTER] <== Retour de {} avec le résultat: {}", methodName ,result);
    }

    // Ce bloc ne s'exécute que si une méthode ciblée lève une exception
    @AfterThrowing(pointcut = "allServiceMethods()", throwing = "exception")
    public void logAfterMethodException(JoinPoint joinPoint, Throwable exception){
        String methodName = joinPoint.getSignature().toShortString();
        log.error("[AOP-ERROR] XXX Exception dans {} - Cause: '{}'", methodName , exception.getMessage());
    }
}
