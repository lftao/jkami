/**
 * 
 */
package com.javatao.jkami;

import java.util.HashSet;
import java.util.Set;

/**
 * 排除懒加载字段
 * 
 * @author tao
 */
public class LazyBeanHolder {
    private static ThreadLocal<HashSet<String>> lazyContext = new ThreadLocal<HashSet<String>>();

    public static void addNotLazyLoad(String prop) {
        HashSet<String> propSet = lazyContext.get();
        if (propSet == null) {
            propSet = new HashSet<>();
        }
        propSet.add(prop);
        set(propSet);
    }

    public static void set(HashSet<String> props) {
        lazyContext.set(props);
    }

    public static Set<String> get() {
        return lazyContext.get() == null ? new HashSet<String>() : lazyContext.get();
    }

    public static void clear() {
        lazyContext.remove();
    }
}
