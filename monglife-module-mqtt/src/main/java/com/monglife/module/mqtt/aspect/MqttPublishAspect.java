package com.monglife.module.mqtt.aspect;

import com.monglife.module.mqtt.annotation.MqttPublish;
import com.monglife.module.mqtt.config.MqttPublishBean;
import com.monglife.module.mqtt.dto.MqttResponseEntity;
import com.monglife.module.mqtt.service.MqttSendService;
import com.monglife.module.mqtt.utils.TopicUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class MqttPublishAspect {

    private final MqttSendService mqttSendService;

    public MqttPublishAspect(MqttSendService mqttSendService) {
        this.mqttSendService = mqttSendService;
    }

    @Pointcut("execution(com.monglife.module.mqtt.dto.MqttResponseEntity *(..))")
    private void executionPointcut() {}

    @AfterReturning(value = "executionPointcut() && @annotation(mqttPublish)", returning = "mqttResponseEntity")
    public void afterReturning(JoinPoint joinPoint, MqttPublish mqttPublish, MqttResponseEntity<?> mqttResponseEntity) {

        String annotationTopic = TopicUtil.preProcessTopic(mqttPublish.value());

        for (String topic : mqttResponseEntity.getTopics()) {

            String sendTopic = annotationTopic.replace(MqttPublishBean.TOPIC_PREFIX, topic);

            mqttSendService.sendMessage(sendTopic, mqttResponseEntity.getBody());
        }
    }
}
