package com.javatao.jkami.spring;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import org.springframework.beans.factory.FactoryBean;

import com.javatao.jkami.annotations.ResultType;
import com.javatao.jkami.proxy.MapperProxy;

/**
 * 拓展spring bean
 * 
 * @author tao
 * @param <T>
 *            类型
 */
public class MapperFactoryBean<T> implements FactoryBean<T> {
    private Class<T> mapperInterface;
    private MapperProxy<T> mapperProxy;

    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public void setMapperProxy(MapperProxy<T> mapperProxy) {
        this.mapperProxy = mapperProxy;
    }

    @Override
    public T getObject() throws Exception {
        return newInstance();
    }

    @Override
    public Class<?> getObjectType() {
        return this.mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @SuppressWarnings("unchecked")
    private T newInstance() {
        //注解类型
        ResultType antType = mapperInterface.getAnnotation(ResultType.class);
        if (antType != null) {
            mapperProxy.setResultType(antType.value());
        }else{
            //获取泛型
            Type[] genType = mapperInterface.getGenericInterfaces();
            if (genType.length>0) {
               Type[] params = ((ParameterizedType) genType[0]).getActualTypeArguments();
               if(params.length>0){
                   mapperProxy.setResultType((Class<?>) params[0]);
               }
            }
        }
        mapperProxy.setMapperInterface(mapperInterface);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
    }
}
