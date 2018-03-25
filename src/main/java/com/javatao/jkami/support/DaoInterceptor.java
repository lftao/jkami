package com.javatao.jkami.support;

import java.lang.reflect.Method;

/**
 * dao拦截器
 * 
 * @author tao
 */
public interface DaoInterceptor {
    /**
     * 调用方法前置处理
     * 
     * @param proxy
     *            对象
     * @param method
     *            方法
     * @param args
     *            参数
     */
    public void beforeInvoke(Object proxy, Method method, Object[] args);

    /**
     * 后置拦截器
     * 
     * @param proxy
     *            对象
     * @param method
     *            方法
     * @param args
     *            参数
     * @param resutl
     *            返回结果
     */
    public void afterInvoke(Object proxy, Method method, Object[] args, Object resutl);
}
