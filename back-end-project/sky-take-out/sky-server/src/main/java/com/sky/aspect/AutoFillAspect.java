package com.sky.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.service.*.*(..))")
    public void servicePointCut() {}

    @Pointcut("execution(* com.sky.mapper.*.*(..))")
    public void mapperPointCut() {}

    @Pointcut("servicePointCut() && mapperPointCut() && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillAspect() {}
    
    @Before("autoFillAspect()")
    public void autoFill(JoinPoint joinPoint) {
        // 日志记录
        log.info("进行公共字段自动填充");
    }

}
