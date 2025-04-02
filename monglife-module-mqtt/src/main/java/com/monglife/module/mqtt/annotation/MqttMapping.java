package com.monglife.module.mqtt.annotation;

import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
@Indexed
public @interface MqttMapping {

    String value() default "";
}
