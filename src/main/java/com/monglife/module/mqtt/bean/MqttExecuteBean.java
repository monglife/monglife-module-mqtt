package com.monglife.module.mqtt.bean;

import com.monglife.module.mqtt.utils.TopicUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttExecuteBean {

    private final MqttMappingBean mqttMappingBean;

    private final MqttExceptionBean mqttExceptionBean;

    /**
     * 매서드 매핑 및 실행
     * @param topic topic
     * @param payload payload
     */
    public void invoke(String topic, String payload) throws Exception {

        topic = TopicUtil.preProcessTopic(topic);

        List<MqttMappingBean.TopicMethod> topicMappingMethods = mqttMappingBean.getTopicMappingMethods(topic);

        // topic 과 매칭되는 메서드가 1개인 경우
        if (topicMappingMethods.size() == 1) {
            try {
                // 매칭 메서드 실행
                mqttMappingBean.invoke(topic, payload, topicMappingMethods.get(0));
            } catch (Throwable throwable) {
                // 예외 처리 메서드 실행
                mqttExceptionBean.invoke(throwable);
            }
        }
        // topic 과 매칭되는 메서드가 없는 경우
        else if(topicMappingMethods.isEmpty()) {
            log.error("{} : not match method", topic);
        }
        // topic 과 매칭되는 메서드가 다수인 경우
        else {
            log.error("{} : Too many mapping methods", topic);
        }
    }
}
