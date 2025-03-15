package com.monglife.module.mqtt.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqttExceptionHandler {
    Class<? extends Throwable>[] value() default {};
}
