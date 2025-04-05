package com.monglife.module.mqtt.demo.consumer;

import com.monglife.module.mqtt.annotation.MqttExceptionHandler;
import com.monglife.module.mqtt.demo.dto.DemoTopicRequestDto;
import com.monglife.module.mqtt.annotation.MqttConsumer;
import com.monglife.module.mqtt.annotation.MqttMapping;
import com.monglife.module.mqtt.annotation.MqttPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@MqttConsumer
@MqttMapping("/demo/topic/consume")
public class DemoConsumer {

    @MqttMapping("/test/{id}")
    public void demoConsume(@PathVariable("id") Long id, @MqttPayload DemoTopicRequestDto demoTopicRequestDto) {
        String message = demoTopicRequestDto.getMessage();
        log.info("[consume] id: {}, message: {}", id, message);
    }

    @MqttMapping("/exception")
    public void demoConsumeExceptionHandler() {
        log.info("[consume] throwing runtime exception");
        throw new RuntimeException();
    }

    @MqttMapping("/exception/advice")
    public void demoConsumeAdviceExceptionHandler() {
        log.info("[consume] throwing runtime exception for consumer advice");
        throw new RuntimeException();
    }

    @MqttExceptionHandler(RuntimeException.class)
    public void mqttExceptionHandler(RuntimeException e) {
        log.error("[exception handle] consumer exception handler test message");
    }
}
