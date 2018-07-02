package com.dw;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;


public class ReflectUtil {

    public static Field findField(Object instance, String name) throws NoSuchFieldException {
        Class clazz = instance.getClass();

        while(clazz != null) {
            try {
                Field e = clazz.getDeclaredField(name);
                if(!e.isAccessible()) {
                    e.setAccessible(true);
                }

                return e;
            } catch (NoSuchFieldException var4) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    public static Method findMethod(Object instance, String name, Class... parameterTypes) throws NoSuchMethodException {
        Class clazz = instance.getClass();

        while(clazz != null) {
            try {
                Method e = clazz.getDeclaredMethod(name, parameterTypes);
                if(!e.isAccessible()) {
                    e.setAccessible(true);
                }

                return e;
            } catch (NoSuchMethodException var5) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchMethodException("Method " + name + " with parameters " + Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }
}
