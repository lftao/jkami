package com.javatao.jkami;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.javatao.jkami.utils.JkBeanUtils;

public class Main {
    private static Object getProperty_2(Object pt1, String propertyName) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(pt1.getClass());
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        Object reValue = null;
        for (PropertyDescriptor pd : pds) {
            if (pd.getName().equals(propertyName)) {
                Method methodGetX = pd.getReadMethod();
                reValue = methodGetX.invoke(pt1);
                break;
            }
        }
        return reValue;
    }
    private static Object write(Object pt1, String propertyName,Object value) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(pt1.getClass());
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        Object reValue = null;
        for (PropertyDescriptor pd : pds) {
            if (pd.getName().equals(propertyName)) {
                Method methodGetX = pd.getWriteMethod();
                methodGetX.invoke(pt1,value);
                break;
            }
        }
        return reValue;
    }

    public static void main(String[] args) throws Exception {
        Page vo = new Page();
        vo.setPage(1);
        String propertyName = "total";
        
        long start = System.currentTimeMillis();
        PropertyDescriptor pd = new PropertyDescriptor(propertyName, Page.class);
       
        Class<?> propertyType = pd.getPropertyType();
        
        
        Class<?> clazz = Page.class;
       
        
        Field field = clazz.getDeclaredField(propertyName);
        for (int i = 0; i < 1000000; i++) {
            //JkBeanUtils.getObjField(clazz, propertyName);
           // field.setAccessible(true);
           // field.get(vo);
            
          // Method md = clazz.getMethod("getPage");
          //  Method methodGetX = pd.getReadMethod();
           // methodGetX.invoke(vo);
            //Object value = md.invoke(vo);
            
            //getProperty_2(vo, propertyName);
            //JkBeanUtils.getPropertyValue(vo, propertyName);
            //JkBeanUtils.getPropertyValue(vo, propertyName);
            //JkBeanUtils.setProperty(vo, propertyName, i);
            JkBeanUtils.setProperty(vo, propertyName, i);
            
           // FieldUtils.readField(vo, propertyName,true);
            //FieldUtils.writeField(vo, propertyName, i, true);
            
            //write(vo, propertyName, i);
        }
        long end = System.currentTimeMillis();
        System.out.println((end-start));
        
        final Map<String, Page> cache = new CacheMap<>(1000);
        final Map<String, Page> map = new HashMap<>();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("ShutdownHook>>"+map.size());
            }
        }));
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(3000);
                        System.out.println("cache size >>"+cache.size());
                        System.out.println("map size >>"+map.size());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        
        for (Integer i = 0; i < 100000000; i++) {
            cache.put(i.toString(), new Page());
        }
        
        //https://github.com/dingey/jdbc-mapper
        
    }
}
