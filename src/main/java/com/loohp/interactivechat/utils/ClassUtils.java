package com.loohp.interactivechat.utils;

import java.lang.reflect.Array;

public class ClassUtils {

    public static Class<?> arrayType(Class<?> type) {
        return Array.newInstance(type, 0).getClass();
    }

}
