package com.javatao.jkami.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JdbcTypesUtils {
	/* jdbc type >  java.sql.Types */
	private static Map<String, Integer> jdbcTypes = new HashMap<>();
	/* javatype > jdbc type */
	private static Map<Class<?>, String> javaToJdbcTypesMp = new HashMap<>();

	/* java.sql.Types > javatype */
	private static Map<Integer, Class<?>> jdbcToJavaTypesMp = new HashMap<>();
	
	
	static {
		Field[] fields = java.sql.Types.class.getFields();
		for (int i = 0, len = fields.length; i < len; ++i) {
			if (Modifier.isStatic(fields[i].getModifiers())) {
				try {
					String name = fields[i].getName();
					Integer value = (Integer) fields[i].get(java.sql.Types.class);
					jdbcTypes.put(name, value);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	
		javaToJdbcTypesMp.put(Integer.class, "INTEGER");
		javaToJdbcTypesMp.put(Long.class, "BIGINT");
		javaToJdbcTypesMp.put(Boolean.class, "TINYINT");
		javaToJdbcTypesMp.put(Date.class, "DATE");
		javaToJdbcTypesMp.put(String.class, "VARCHAR");
		javaToJdbcTypesMp.put(BigDecimal.class, "DECIMAL");
		javaToJdbcTypesMp.put(Double.class, "DOUBLE");
		javaToJdbcTypesMp.put(Float.class, "FLOAT");
		javaToJdbcTypesMp.put(null, "NULL");
		javaToJdbcTypesMp.put(Blob.class, "BLOB");
		javaToJdbcTypesMp.put(Clob.class, "CLOB");
		
		
	 //初始化jdbcToJavaTypesMp：
	  jdbcToJavaTypesMp.put(new Integer(Types.LONGNVARCHAR), String.class);  // -16 字符串
	  jdbcToJavaTypesMp.put(new Integer(Types.NCHAR), String.class);    // -15 字符串
	  jdbcToJavaTypesMp.put(new Integer(Types.NVARCHAR), String.class);   // -9 字符串
	  jdbcToJavaTypesMp.put(new Integer(Types.ROWID), String.class);    // -8 字符串
	  jdbcToJavaTypesMp.put(new Integer(Types.BIT), Boolean.class);    // -7 布尔
	  jdbcToJavaTypesMp.put(new Integer(Types.TINYINT), Byte.class);    // -6 数字
	  jdbcToJavaTypesMp.put(new Integer(Types.BIGINT), Long.class);    // -5 数字 
	  jdbcToJavaTypesMp.put(new Integer(Types.LONGVARBINARY), Blob.class);  // -4 二进制
	  jdbcToJavaTypesMp.put(new Integer(Types.VARBINARY), Blob.class);   // -3 二进制
	  jdbcToJavaTypesMp.put(new Integer(Types.BINARY), Blob.class);    // -2 二进制
	  jdbcToJavaTypesMp.put(new Integer(Types.LONGVARCHAR), String.class);  // -1 字符串
	  jdbcToJavaTypesMp.put(new Integer(Types.NULL), String.class);    // 0 /
	  jdbcToJavaTypesMp.put(new Integer(Types.CHAR), String.class);    // 1 字符串
	  jdbcToJavaTypesMp.put(new Integer(Types.NUMERIC), BigDecimal.class);  // 2 数字
	  jdbcToJavaTypesMp.put(new Integer(Types.DECIMAL), BigDecimal.class);  // 3 数字
	  jdbcToJavaTypesMp.put(new Integer(Types.INTEGER), Integer.class);   // 4 数字
	  jdbcToJavaTypesMp.put(new Integer(Types.SMALLINT), Short.class);   // 5 数字
	  jdbcToJavaTypesMp.put(new Integer(Types.FLOAT), BigDecimal.class);   // 6 数字
	  jdbcToJavaTypesMp.put(new Integer(Types.REAL), BigDecimal.class);   // 7 数字
	  jdbcToJavaTypesMp.put(new Integer(Types.DOUBLE), BigDecimal.class);  // 8 数字
	  jdbcToJavaTypesMp.put(new Integer(Types.VARCHAR), String.class);   // 12 字符串
	  jdbcToJavaTypesMp.put(new Integer(Types.BOOLEAN), Boolean.class);   // 16 布尔
	  jdbcToJavaTypesMp.put(new Integer(Types.DATALINK), String.class);   // 70 /
	  jdbcToJavaTypesMp.put(new Integer(Types.DATE), Date.class);    // 91 日期
	  jdbcToJavaTypesMp.put(new Integer(Types.TIME), Date.class);    // 92 日期
	  jdbcToJavaTypesMp.put(new Integer(Types.TIMESTAMP), Date.class);   // 93 日期
	  jdbcToJavaTypesMp.put(new Integer(Types.OTHER), Object.class);    // 1111 其他类型？ 
	  jdbcToJavaTypesMp.put(new Integer(Types.JAVA_OBJECT), Object.class);  // 2000 
	  jdbcToJavaTypesMp.put(new Integer(Types.DISTINCT), String.class);   // 2001 
	  jdbcToJavaTypesMp.put(new Integer(Types.STRUCT), String.class);   // 2002 
	  jdbcToJavaTypesMp.put(new Integer(Types.ARRAY), String.class);    // 2003 
	  jdbcToJavaTypesMp.put(new Integer(Types.BLOB), Blob.class);    // 2004 二进制
	  jdbcToJavaTypesMp.put(new Integer(Types.CLOB), Clob.class);    // 2005 大文本
	  jdbcToJavaTypesMp.put(new Integer(Types.REF), String.class);    // 2006 
	  jdbcToJavaTypesMp.put(new Integer(Types.SQLXML), String.class);   // 2009 
	  jdbcToJavaTypesMp.put(new Integer(Types.NCLOB), Clob.class);    // 2011 大文本
	  
	  
	  
	}

	/**
	 * javatype >java.sql.Types
	 * @param key
	 * @return
	 */
	public static int getJdbcType(Object obj) {
		if (obj == null) {
			return jdbcTypes.get(javaToJdbcTypesMp.get(obj));
		}
		for (Class<?> k : javaToJdbcTypesMp.keySet()) {
			if (k!=null&&k.isInstance(obj)) {
				return jdbcTypes.get(javaToJdbcTypesMp.get(k));
			}
		}
		throw new RuntimeException("javaToJdbcTypesMp " + obj.getClass() + " not match jdbcTypes");
	};
	
	public static String getJdbcType(Collection<?> obj) {
		if (obj == null||obj.isEmpty()) {
			return javaToJdbcTypesMp.get(obj);
		}
		
		Object next = obj.iterator().next();
		for (Class<?> k : javaToJdbcTypesMp.keySet()) {
			if (k!=null&&k.isInstance(next)) {
				return  javaToJdbcTypesMp.get(k);
			}
		}
		throw new RuntimeException("javaToJdbcTypesMp " + obj.getClass() + " not match jdbcTypes");
	};
	
	

	/**
	 * java.sql.Types > javatype
	 * @param key
	 * @return
	 */
	public static Class<?> getJavaType(int key) {
		if(jdbcToJavaTypesMp.containsKey(key)){
			return jdbcToJavaTypesMp.get(key);
		}
		throw new RuntimeException("jdbcToJavaTypes   not match javaTypes");
	};
	

	public static void main(String[] args) {

		for (String k : jdbcTypes.keySet()) {

			System.out.println(k + " >>> " + jdbcTypes.get(k));
		}
	}

}
