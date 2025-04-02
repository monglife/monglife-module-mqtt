package com.monglife.module.mqtt.client;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttOutBoundClient {

    void send(@Header(MqttHeaders.TOPIC) String topic, String payload);
}
