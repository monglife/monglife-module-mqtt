package com.monglife.module.mqtt.annotation;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqttConsumerAdvice {
}
