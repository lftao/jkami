package com.javatao.jkami.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解sql
 * 
 * @author tao
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface Sql {
    /**
     * sql 语句
     * 
     * @return sql
     */
    String value();

    /**
     * 强制每次都查询
     * 
     * @return true/false
     */
    boolean force() default false;
}
