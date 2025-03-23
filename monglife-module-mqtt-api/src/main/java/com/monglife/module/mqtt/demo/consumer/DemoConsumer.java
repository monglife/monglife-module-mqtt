package com.monglife.module.mqtt.demo.consumer;

import com.monglife.module.mqtt.demo.dto.DemoTopicRequestDto;
import com.monglife.module.mqtt.annotation.MqttConsumer;
import com.monglife.module.mqtt.annotation.MqttMapping;
import com.monglife.module.mqtt.annotation.MqttPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@MqttConsumer
public class DemoConsumer {

    @MqttMapping("/demo/topic/{id}")
    public void demoConsume(@PathVariable("id") Long id, @MqttPayload DemoTopicRequestDto demoTopicRequestDto) {

        String message = demoTopicRequestDto.getMessage();

        log.info("[consume] id: {}, message: {}", id, message);
    }
}
