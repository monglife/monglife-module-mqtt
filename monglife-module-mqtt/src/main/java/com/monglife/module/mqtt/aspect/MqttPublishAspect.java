package com.monglife.module.mqtt.aspect;

import com.monglife.module.mqtt.annotation.MqttPublish;
import com.monglife.module.mqtt.bean.MqttPublishBean;
import com.monglife.module.mqtt.dto.MqttResponseEntity;
import com.monglife.module.mqtt.service.MqttSendService;
import com.monglife.module.mqtt.utils.TopicUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Aspect
@Component
public class MqttPublishAspect {

    private final MqttSendService mqttSendService;

    @Autowired
    public MqttPublishAspect(MqttSendService mqttSendService) {
        this.mqttSendService = mqttSendService;
    }

    @Pointcut("execution(com.monglife.module.mqtt.dto.MqttResponseEntity *(..))")
    private void executionWithMqttResponseEntityPointcut() {}

    @AfterReturning(value = "executionWithMqttResponseEntityPointcut() && @annotation(mqttPublish)", returning = "mqttResponseEntity")
    public void afterReturning(JoinPoint joinPoint, MqttPublish mqttPublish, MqttResponseEntity<?> mqttResponseEntity) {

        String annotationTopic = TopicUtil.preProcessTopic(mqttPublish.value());

        if (mqttResponseEntity.getTopics().isEmpty()) {
            mqttSendService.sendMessage(annotationTopic, new HashMap<>());
        } else {
            for (String topic : mqttResponseEntity.getTopics()) {
                String sendTopic = annotationTopic.replace(MqttPublishBean.TOPIC_PREFIX, topic);
                mqttSendService.sendMessage(sendTopic, mqttResponseEntity.getBody());
            }
        }
    }
}
