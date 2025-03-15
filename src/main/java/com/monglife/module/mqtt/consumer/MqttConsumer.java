package com.monglife.module.mqtt.consumer;

import com.monglife.module.mqtt.bean.MqttExecuteBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@MessagingGateway(defaultRequestChannel = "mqttInboundChannel")
public class MqttConsumer implements MessageHandler {

    private final MqttExecuteBean mqttExecuteBean;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {

        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = (String) message.getPayload();

        try {
            mqttExecuteBean.invoke(topic == null ? "" : topic, payload);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
