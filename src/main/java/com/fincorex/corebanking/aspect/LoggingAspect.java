
package com.fincorex.corebanking.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.bankfusion.corebanking.service.*.*(..))")
    public void logBefore(final JoinPoint joinPoint) {
        logger.info("Method execution started: {}", joinPoint.getSignature());
        logger.info("Parameters passed: {}", Arrays.toString(joinPoint.getArgs()));
    }

    @After("execution(* com.bankfusion.corebanking.service.*.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        logger.info("Method execution completed: {}", joinPoint.getSignature());
    }

    @AfterThrowing(pointcut = "execution(* com.bankfusion.corebanking.service.*.*(..))", throwing = "ex")
    public void logAfterException(JoinPoint joinPoint, Throwable ex) {
        logger.error("Exception occurred in method: {} | Message: {}", joinPoint.getSignature(), ex.getMessage(), ex);
    }
}
