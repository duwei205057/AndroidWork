package com.aop;

import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.HashMap;

/**
 * Created by duwei on 18-3-8.
 */

@Aspect
public class TraceAspect {

    private static final String POINTCUT_METHOD =
            "call(* com.dw.MainActivity.execShellCmd(String))";

    private static final String POINTCUT_CONSTRUCTOR =
            "execution(@com.aop.DebugTrace * *.*(..))";

    public HashMap<String, Long> map = new HashMap<>();

    @Pointcut(POINTCUT_METHOD)
    public void methodAnnotatedWithDebugTrace() {}

    @Pointcut(POINTCUT_CONSTRUCTOR)
    public void constructorAnnotatedDebugTrace() {}

    @Around("methodAnnotatedWithDebugTrace() || constructorAnnotatedDebugTrace()")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();

        long methodDuration = stopWatch.getTotalTimeMillis();

        putInMap(methodName, methodDuration);
        DebugLog.log("xx", buildLogMessage(methodName, methodDuration)+" this="+joinPoint.getThis()+" target="+joinPoint.getTarget() +" this= "+this +" max = " + map.get(methodName));

        return result;
    }

    /**
     * Create a log message.
     *
     * @param methodName A string with the method name.
     * @param methodDuration Duration of the method in milliseconds.
     * @return A string representing message.*/

    private static String buildLogMessage(String methodName, long methodDuration) {
        StringBuilder message = new StringBuilder();
        message.append("AOP --> ");
        message.append(methodName);
        message.append(" --> ");
        message.append("[");
        message.append(methodDuration);
        message.append("ms");
        message.append("]");

        return message.toString();
    }

    /*@Before("call(* *(..))")
    public void anyCall(JoinPoint joinPoint){
        Log.d("xx", "anyCall: ");
    }*/
    private void putInMap(String methodName, long methodDuration) {
        if (map.containsKey(methodName)) {
            long cur = map.get(methodName);
            if (methodDuration > cur )
                map.put(methodName, methodDuration);
        } else
            map.put(methodName, methodDuration);
    }
}
