package com.monglife.module.mqtt.config;

import com.monglife.module.mqtt.annotation.MqttPublish;
import com.monglife.module.mqtt.dto.MqttResponseEntity;
import com.monglife.module.mqtt.utils.TopicUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
public class MqttPublishBean implements InitializingBean {

    public static final String TOPIC_PREFIX = "{topic}";

    private final ApplicationContext applicationContext;

    @Autowired
    public MqttPublishBean(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 전송 메서드 스캔 및 확인
     * TOPIC_PREFIX 포함 여부 확인
     * returnType -> MqttResponseEntity
     */
    @Override
    public void afterPropertiesSet() {

        String currentBeanName = applicationContext.getBeanNamesForType(this.getClass())[0];
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {

            if (beanName.equals(currentBeanName)) continue;

            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);

            for (Method method : beanClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(MqttPublish.class)) {

                    if (method.getReturnType() != MqttResponseEntity.class) {
                        throw new RuntimeException(beanClass.getName() + "#" + method.getName() + " : return type is not MqttResponseEntity.");
                    }

                    MqttPublish mqttPublish = method.getAnnotation(MqttPublish.class);

                    String topic = TopicUtil.preProcessTopic(mqttPublish.value());

                    Integer topicPrefixCount = TopicUtil.countTopicPrefix(topic, TOPIC_PREFIX);

                    if (topicPrefixCount == 0) {
                        throw new RuntimeException(beanClass.getName() + "#" + method.getName() + " : not exists topic prefix.");
                    } else if (topicPrefixCount > 1) {
                        throw new RuntimeException(beanClass.getName() + "#" + method.getName() + " : top many topic prefix.");
                    }
                }
            }
        }
    }
}


