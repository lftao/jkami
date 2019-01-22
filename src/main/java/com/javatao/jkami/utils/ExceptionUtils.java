package com.javatao.jkami.utils;

import java.lang.reflect.InvocationTargetException;

import com.javatao.jkami.JkException;

public class ExceptionUtils {
    public static Exception getException(Throwable e) {
        if (e == null) {
            return null;
        }
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException) e).getTargetException();
        }
        Throwable cause = e.getCause();
        if (cause instanceof JkException) {
            return (Exception) cause;
        }
        while (cause != null) {
            e = cause;
            cause = cause.getCause();
        }
        return (Exception) e;
    }
}
