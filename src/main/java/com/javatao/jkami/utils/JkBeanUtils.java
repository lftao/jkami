package com.javatao.jkami.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.javatao.jkami.CacheMap;
import com.javatao.jkami.JkException;
import com.javatao.jkami.annotations.Depth;
import com.javatao.jkami.jdbc.BlobImpl;
import com.javatao.jkami.jdbc.LobUtils;

/**
 * BeanUtils Set get
 * 
 * @author TLF
 */
public class JkBeanUtils {
    private final static Log logger = LogFactory.getLog(JkBeanUtils.class);
    private final static String EMPTY = "";
    private final static String SPLIT = "_";
    private final static String POINT = ".";
    private final static Pattern numberPattern = Pattern.compile("^[-\\+]?[\\d]+$");
    // 类方字段缓存
    private static Map<String, Field> classFiledCacheMp = new CacheMap<>();
    // 类方字段缓存
    private static Map<String, PropertyDescriptor> propertyDescriptorCache = new CacheMap<>();

    /**
     * 获得对象的属性类型
     * 
     * @param o
     * @param property
     */
    public static Class<?> getPropertyType(Object o, String property) {
        Class<?> clas = o.getClass();
        Field field = getObjField(clas, property);
        return field.getType();
    }

    /**
     * 获取属性值
     * 
     * @param o
     * @param property
     */
    public static Object getPropertyValue(Object o, String property) {
        try {
            Field field = getObjField(o.getClass(), property);
            return field.get(o);
        } catch (Exception e) {
            throw new JkException(e);
        }
    }

    /**
     * 获取 PropertyDescriptor
     * 
     * @param o
     * @param property
     * @return
     */
    public static PropertyDescriptor getPropertyDescriptor(Object o, String property) {
        try {
            Class<?> clazz = o.getClass();
            String key = clazz.getName().concat(POINT + property);
            PropertyDescriptor propertyDescriptor = propertyDescriptorCache.get(key);
            // 如果不存在
            if (propertyDescriptor == null) {
                initPropertyDescriptorCache(clazz);
                propertyDescriptor = propertyDescriptorCache.get(key);
            }
            if (propertyDescriptor == null) {
                throw new JkException(key + " not fond");
            }
            return propertyDescriptor;
        } catch (IntrospectionException e) {
            throw new JkException(e);
        }
    }

    // 初始化 PropertyDescriptorCache
    private static void initPropertyDescriptorCache(Class<?> clazz) throws IntrospectionException {
        String cname = clazz.getName().concat(POINT);
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            String name = pd.getName();
            propertyDescriptorCache.put(cname + name, pd);
        }
    }

    /**
     * 设置参数值
     * 
     * @param o
     * @param property
     * @param value
     */
    public static Object setProperty(Object o, String property, Object value) {
        try {
            Field field = getObjField(o.getClass(), property);
            Class<?> type = field.getType();
            if (type.isInstance(value)) {
                field.set(o, value);
            } else if (type.isAssignableFrom(Blob.class)) {
                if (value instanceof byte[]) {
                    field.set(o, new BlobImpl((byte[]) value));
                    return o;
                }
                if (value instanceof Clob) {
                    field.set(o, new BlobImpl(LobUtils.toBytes((Clob) value)));
                    return o;
                }
            } else if (type.isAssignableFrom(String.class)) {
                if (value instanceof Clob) {
                    String vl = LobUtils.toStr((Clob) value);
                    field.set(o, vl);
                    return o;
                }
                if (value instanceof Blob) {
                    String vl = LobUtils.toStr((Blob) value);
                    field.set(o, vl);
                    return o;
                }
            } else {
                if (value instanceof List) {
                    List<?> lv = (List<?>) value;
                    if (lv == null || lv.size() == 0) {
                        return o;
                    }
                    if (type.isAssignableFrom(String.class)) {
                        String vvo = lv.get(0).toString();
                        for (int i = 1; i < lv.size(); i++) {
                            vvo += "," + lv.get(i);
                        }
                        field.set(o, vvo);
                        return o;
                    } else {
                        if (lv.size() == 1) {
                            field.set(o, lv.get(0));
                        } else {
                            throw new JkException(" list:size = " + lv.size());
                        }
                    }
                } else if (type.isAssignableFrom(Boolean.class)) {
                    if (value instanceof Boolean) {
                        field.set(o, value);
                    } else if (value instanceof Number) {
                        field.set(o, value.toString().equals("1") ? true : false);
                    }
                } else if (value instanceof Boolean) {
                    if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(Short.class) || type.isAssignableFrom(Long.class)
                            || type.isAssignableFrom(Number.class)) {
                        field.set(o, (Boolean) value ? 1 : 0);
                    }
                } else if (value instanceof Number) {
                    Number number = (Number) value;
                    if (type.isAssignableFrom(Long.class)) {
                        field.set(o, number.longValue());
                    } else if (type.isAssignableFrom(Integer.class)) {
                        field.set(o, number.intValue());
                    } else if (type.isAssignableFrom(Float.class)) {
                        field.set(o, number.floatValue());
                    } else if (type.isAssignableFrom(Double.class)) {
                        field.set(o, number.doubleValue());
                    } else if (type.isAssignableFrom(Short.class)) {
                        field.set(o, number.shortValue());
                    } else {
                        try {
                            field.set(o, type.getConstructor(String.class).newInstance(value.toString()));
                        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (type.isEnum()) {// 枚举2016.07.13 tao add
                    try {
                        Object[] ts = type.getEnumConstants();
                        for (Object ob : ts) {
                            if (ob.toString().equals(value.toString())) {
                                field.set(o, ob);
                                return o;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    field.set(o, value);
                }
            }
            return o;
        } catch (Exception e) {
            if (e instanceof JkException) {
                throw (JkException) e;
            }
            throw new JkException(o.getClass().getName() + POINT + property + " set error ", e);
        }
    }

    /**
     * 获得Field 包含父类
     * 
     * @param clazz
     * @return
     */
    public static Field getObjField(Class<?> clazz, String name) {
        return getObjField(clazz, name, 1);
    }

    private static Field getObjField(Class<?> clazz, String name, int n) {
        String key = clazz.getName().concat(POINT + name);
        Field field = classFiledCacheMp.get(key);
        if (field != null) {
            return field;
        }
        if (n > 2) {
            throw new JkException(key + " not found");
        }
        initFileCache(clazz);
        return getObjField(clazz, name, ++n);
    }

    /**
     * 初始化加载 clazz-field
     * 
     * @param clazz
     */
    private static void initFileCache(Class<?> clazz) {
        List<Field> allFields = getAllFields(clazz);
        for (Field field : allFields) {
            String namne = field.getName();
            String key = clazz.getName().concat(POINT + namne);
            classFiledCacheMp.put(key, field);
        }
    }

    /**
     * 获得所有 Field 包含父类
     * 
     * @param clazz
     * @return
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> allField = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            allField.add(field);
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass.getDeclaredFields().length > 0) {
            allField.addAll(getAllFields(superclass));
        }
        return allField;
    }

    /**
     * 首字母大写
     * 
     * @param column
     * @return user > User
     */
    public static String firstToHump(String column) {
        if (isBlank(column)) {
            return EMPTY;
        } else {
            return column.substring(0, 1).toUpperCase() + column.substring(1);
        }
    }

    /**
     * 首字母小写
     * 
     * @param column
     * @return
     */
    public static String firstToMix(String column) {
        if (isBlank(column)) {
            return EMPTY;
        } else {
            return column.substring(0, 1).toLowerCase() + column.substring(1);
        }
    }

    /**
     * 字段转驼峰命名
     * 
     * @param column
     * @return user_name > userName
     */
    public static String columnToHump(String column) {
        if (isBlank(column)) {
            return EMPTY;
        } else if (column.indexOf(SPLIT) < 0) {
            return column.substring(0, 1).toLowerCase() + column.substring(1);
        } else {
            StringBuilder result = new StringBuilder();
            String[] columns = column.split(SPLIT);
            for (String columnSplit : columns) {
                if (columnSplit.isEmpty()) {
                    continue;
                }
                if (result.length() == 0) {
                    result.append(columnSplit.toLowerCase());
                } else {
                    result.append(columnSplit.substring(0, 1).toUpperCase()).append(columnSplit.substring(1).toLowerCase());
                }
            }
            return result.toString();
        }
    }

    /**
     * map 返回 实体
     * 
     * @param map
     * @param classz
     *            or object
     * @return
     */
    public static <T> Object mapToObject(Map<String, Object> map, Object classz) {
        return mapToObject(map, classz, null);
    }

    /**
     * map 返回 实体
     */
    public static <T> Object mapToObject(Map<String, Object> map, Object classz, PsKey psKey) {
        Object obj = classz;
        if (classz instanceof Class) {
            try {
                obj = ((Class<?>) classz).newInstance();
            } catch (Exception e) {
                throw new JkException(e);
            }
        }
        for (Entry<String, Object> entry : map.entrySet()) {
            try {
                String key = entry.getKey();
                if (psKey != null) {
                    key = psKey.before(key);
                }
                Object value = entry.getValue();
                setProperty(obj, key, value);
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return obj;
    }

    public interface PsKey {
        String before(String key);
    };

    /**
     * map 返回 实体
     * 
     * @param map
     * @param classz
     * @return
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> classz) {
        return (T) mapToObject(map, classz);
    }

    /**
     * 字段转驼峰命名
     * 
     * @param column
     * @return userName > user_name
     */
    public static String columnToHumpReversal(String column) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < column.length(); i++) {
            char charAt = column.charAt(i);
            if (Character.isUpperCase(charAt)) {
                result.append("_");
                charAt = Character.toLowerCase(charAt);
            }
            result.append(charAt);
        }
        return result.toString();
    }

    /**
     * 判断段是否是数字
     */
    public static boolean isNumber(String str) {
        return numberPattern.matcher(str).matches();
    }

    /**
     * 是否是实体
     */
    public static boolean isBeanClass(Class<?> clz) {
        try {
            String s = clz.getName();
            if (s.indexOf("java.lang") > -1) {
                return false;
            }
            if (s.indexOf("java.util") > -1) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断的包装类
     * 
     * @param clz
     * @return
     */
    public static boolean isWrapClass(Class<?> clz) {
        try {
            return ((Class<?>) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断的是否为空
     */
    public static boolean isBlank(String s) {
        if (s == null || EMPTY.equals(s.trim())) {
            return true;
        }
        return false;
    }

    /**
     * 返回 maxDepth default 3
     * 
     * @param classType
     * @return
     */
    public static int getMaxDepth(Class<?> classType) {
        int maxDepth = 2;
        if (classType != null) {
            Depth depth = classType.getAnnotation(Depth.class);
            if (depth != null) {
                maxDepth = depth.value();
            }
        }
        return maxDepth;
    }
}