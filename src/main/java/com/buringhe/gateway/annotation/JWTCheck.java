package com.buringhe.gateway.annotation;

import java.lang.annotation.*;

/**
 *  Jwt 检查
 * Create by buring  on 2020/7/22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JWTCheck {

    String value() default "";

}
