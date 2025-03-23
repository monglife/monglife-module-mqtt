package com.monglife.module.mqtt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monglife.module.mqtt.client.MqttOutBoundClient;
import com.monglife.module.mqtt.property.MqttModuleProperties;
import com.monglife.module.mqtt.utils.TopicUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttSendService {

    private final MqttModuleProperties mqttModuleProperties;

    private final MqttOutBoundClient mqttOutBoundClient;

    private final ObjectMapper objectMapper;

    public MqttSendService(MqttModuleProperties mqttModuleProperties, MqttOutBoundClient mqttOutBoundClient) {
        this.mqttModuleProperties = mqttModuleProperties;
        this.mqttOutBoundClient = mqttOutBoundClient;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public <T> void sendMessage(String topic, T responseDto) {
        try {
            String payload = objectMapper.writeValueAsString(responseDto);
            this.sendMessage(topic, payload);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 메시지 전송
     * @param topic 토픽
     * @param payload 페이로드
     */
    private void sendMessage(String topic, String payload) {

        String baseTopic = TopicUtil.preProcessTopic(mqttModuleProperties.getPublisher().getBaseTopic());

        String sendTopic = TopicUtil.generateTopic(baseTopic, topic);

        mqttOutBoundClient.send(TopicUtil.preProcessTopic(sendTopic), payload);
    }
}
