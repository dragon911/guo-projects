package com.guo.guns.common.annotion;

import java.lang.annotation.*;

/**
 * 多数据源标识
 *
 * @Author guo
 * @Date 2018-03-04 12:37.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DataSource {
    String name() default "";
}
