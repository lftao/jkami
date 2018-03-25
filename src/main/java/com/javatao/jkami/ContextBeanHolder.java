/**
 * 
 */
package com.javatao.jkami;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bean上下文处理
 * 
 * @author tao
 */
public class ContextBeanHolder {
    private static ThreadLocal<HashSet<String>> lazyContext = new ThreadLocal<HashSet<String>>();
    private static ThreadLocal<Map<String, Object>> sqlParams = new ThreadLocal<Map<String, Object>>();

    /**
     * 添加排除懒加载字段
     * 
     * @param prop
     *            字段
     */
    public static void addNotLazyLoad(String prop) {
        HashSet<String> propSet = lazyContext.get();
        if (propSet == null) {
            propSet = new HashSet<>();
        }
        propSet.add(prop);
        lazyContext.set(propSet);
    }

    /**
     * 获取排除字段
     * 
     * @return 字段集合
     */
    public static Set<String> getNotLazy() {
        return lazyContext.get() == null ? new HashSet<String>() : lazyContext.get();
    }

    /**
     * 放入线程变量 -可以直接在sql中使用
     * 
     * @param key
     *            key
     * @param value
     *            value
     */
    public static void put(String key, Object value) {
        Map<String, Object> map = sqlParams.get();
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(key, value);
        sqlParams.set(map);
    }

    /**
     * 设置线程变量 -可以直接在sql中使用
     * 
     * @param sqlParams
     *            包装参数
     */
    public static void setSqlParams(Map<String, Object> sqlParams) {
        ContextBeanHolder.sqlParams.set(sqlParams);
    }

    /**
     * 获取线程中的sql参数
     * 
     * @return 参数
     */
    public static Map<String, Object> getSqlParams() {
        Map<String, Object> map = sqlParams.get();
        if (map == null) {
            map = new HashMap<>();
        }
        return map;
    }

    /**
     * 清空 -lazyContext
     */
    public static void clear() {
        lazyContext.remove();
    }

    /**
     * 清空 -sqlParams
     */
    public static void clearSqlParams() {
        sqlParams.remove();
    }
}
