package com.example.config;

import cn.rylan.springboot.bean.LogTrackInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Pointcut("execution(* com.example.controller..*(..))")
    public void controller() {
    }

    @Before("controller()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("===============方法执行开始：" + joinPoint.getSignature().getName() + "================");
        log.info("链路ID: " + LogTrackInterceptor.getTraceId());
    }

    @AfterReturning(pointcut = "controller()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("===============方法执行结束：" + joinPoint.getSignature().getName() + "================\n");
    }

    @AfterThrowing(pointcut = "controller()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        log.info("方法抛出异常：" + exception.getMessage());
    }
}
