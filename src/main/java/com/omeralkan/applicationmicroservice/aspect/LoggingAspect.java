package com.omeralkan.applicationmicroservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(com.omeralkan.applicationmicroservice.controller..*)")
    public void controllerMethods() {}

    @Pointcut("within(com.omeralkan.applicationmicroservice.service.impl..*)")
    public void serviceMethods() {}

    @Pointcut("controllerMethods() || serviceMethods()")
    public void applicationMethods() {}

    @Around("applicationMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("[ENTER] {}.{}() | Args: {}", className, methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;

        log.info("[EXIT] {}.{}() | Duration: {}ms", className, methodName, duration);

        return result;
    }

    @AfterThrowing(pointcut = "applicationMethods()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        log.error("[ERROR] {}.{}() | Exception: {} | Message: {}",
                className, methodName,
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }
}