package com.monglife.module.mqtt.demo.consumer;

import com.monglife.module.mqtt.annotation.MqttConsumerAdvice;
import com.monglife.module.mqtt.annotation.MqttExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MqttConsumerAdvice
public class DemoConsumerAdvice {

    @MqttExceptionHandler(RuntimeException.class)
    public void mqttAdviceExceptionHandler(RuntimeException e) {
        log.error("[exception handle] consumer advice exception handler test message");
    }
}
