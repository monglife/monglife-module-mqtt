package com.monglife.module.mqtt.demo.publisher;

import com.monglife.module.mqtt.annotation.MqttPublish;
import com.monglife.module.mqtt.demo.dto.DemoTopicRequestDto;
import com.monglife.module.mqtt.dto.MqttResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DemoPublisher {

    @MqttPublish("/demo/topic/{topic}")
    public MqttResponseEntity<DemoTopicRequestDto> demoPublish(Long id, String message) {

        String topic = String.valueOf(id);
        DemoTopicRequestDto body = DemoTopicRequestDto.builder()
                .message(message)
                .build();

        return MqttResponseEntity
                .body(body)
                .topic(topic);
    }
}
