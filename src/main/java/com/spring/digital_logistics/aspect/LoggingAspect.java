package com.spring.digital_logistics.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.spring.digital_logistics.service.UserService.*(..))")
    public void userServiceMethodes() {}

    @Before("userServiceMethodes()")
    public void LogBefore(JoinPoint joinPoint){
        System.out.println("[AOP] Appel de la méthode : " + joinPoint.getSignature().getName());
    }


    @AfterReturning(pointcut = "userServiceMethodes()" , returning = "result")
    public void LogAfter(JoinPoint jointPoint , Object result){
        System.out.println("[AOP] Méthode " + jointPoint.getSignature().getName() + "terminée, resultat : " + result);
    }

}
