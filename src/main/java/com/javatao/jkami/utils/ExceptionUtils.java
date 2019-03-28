package com.javatao.jkami.utils;

import java.lang.reflect.InvocationTargetException;

import com.javatao.jkami.JkException;

public class ExceptionUtils {
    public static RuntimeException runtimeException(Throwable e) {
        Exception ex = getException(e);
        if (ex != null&&ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        }
        return new RuntimeException(ex);
    }

    public static Exception getException(Throwable e) {
        if (e == null) {
            return null;
        }
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException) e).getTargetException();
        }
        if (e instanceof JkException) {
            return (Exception) e;
        }
        if (e instanceof RuntimeException) {
            return new JkException(e);
        }
        Throwable cause = e.getCause();
        while (cause != null&&!(cause instanceof RuntimeException)) {
            e = cause;
            cause = cause.getCause();
        }
        return (Exception) e;
    }
}
