/**
 * 
 */
package com.javatao.jkami.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 结果集类型
 * 
 * @author tao
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.FIELD })
public @interface ResultType {
    /**
     * 返回类型
     * 
     * @return 类型
     */
    Class<?> value();
}
