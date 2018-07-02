package com.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Created by duwei on 18-3-11.
 */

@Aspect
public class FieldAspect {

//    @Before("set(int com.aop.AspectBean.age)")  @Before("set(@com.aop.FieldTrace * *)
    @Around("set(@com.aop.FieldTrace * *) && !withincode(com.aop..*.new(..)) && @annotation(fieldMessage)")
    public void beforeSetField(JoinPoint joinPoint, FieldTrace fieldMessage){
        DebugLog.log("xx","[beforSetField]-----------------"+fieldMessage.value()+" this="+joinPoint.getThis()+" target="+joinPoint.getTarget());
    }
//    @annotation(fieldMessage)用来表示fieldMessage参数是注解类型
//    /*"+fieldMessage.value()+"*/
}
