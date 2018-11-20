package com.sogou.nativecrashcollector;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Created by duwei on 18-3-8.
 */

@Aspect
public class BackTraceAspect {

    private static final String POINTCUT_CONSTRUCTOR =
            "execution(@com.sogou.nativecrashcollector.BackTrace * *.*(..))";

    @Pointcut(POINTCUT_CONSTRUCTOR)
    public void getBackTracePointcut() {}

    @Around("getBackTracePointcut()")
    public Object backtracePoint(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        Log.d("xx","backtracePoint=========className="+className+" methodName="+methodName);
        BackTraceService trace = BacKTraceFactory.getService();
        trace.register();
        Object result = joinPoint.proceed();
        trace.withdraw();
        return result;
    }

}
