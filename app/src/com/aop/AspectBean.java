package com.aop;

/**
 * Created by duwei on 18-3-11.
 */

public class AspectBean {

    @FieldTrace("bean")
    int age;

    public AspectBean(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
