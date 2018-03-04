package com.guo.guns.common.annotion;

import java.lang.annotation.*;

/**
 * 权限注解，用于检查权限 规定访问权限
 *
 * @Author guo
 * @Date 2018-03-04 12:15.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Permission {
    String[] value() default {};
}
