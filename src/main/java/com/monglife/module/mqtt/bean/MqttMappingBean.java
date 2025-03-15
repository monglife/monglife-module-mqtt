package com.monglife.module.mqtt.bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monglife.module.mqtt.annotation.MqttConsumer;
import com.monglife.module.mqtt.annotation.MqttMapping;
import com.monglife.module.mqtt.annotation.MqttPayload;
import com.monglife.module.mqtt.property.MqttModuleProperties;
import com.monglife.module.mqtt.utils.TopicUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Slf4j
@Component
public class MqttMappingBean implements InitializingBean {

    private static final String WILD_CARD_WORD = "+";

    private final ApplicationContext applicationContext;

    private final Map<String, TopicMethod> mqttMethodMapping;

    private final ObjectMapper objectMapper;

    private final MqttModuleProperties mqttModuleProperties;

    @Autowired
    public MqttMappingBean(ApplicationContext applicationContext, MqttModuleProperties mqttModuleProperties) {
        this.applicationContext = applicationContext;
        this.mqttMethodMapping = new HashMap<>();
        this.mqttModuleProperties = mqttModuleProperties;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * mqtt 매핑 메서드 스캔
     */
    @Override
    public void afterPropertiesSet() {

        String[] beanNames = applicationContext.getBeanNamesForAnnotation(MqttConsumer.class);

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);

            MqttMapping clazzMqttMapping = beanClass.getAnnotation(MqttMapping.class);

            String clazzTopic = clazzMqttMapping == null ? "" : TopicUtil.preProcessTopic(clazzMqttMapping.value());

            for (Method method : beanClass.getDeclaredMethods()) {
                // 메서드 파라미터에서 MqttPayload 가 다건인지 확인

                int mqttPayloadCount = Arrays.stream(method.getParameters())
                        .filter(parameter -> parameter.isAnnotationPresent(MqttPayload.class))
                        .toList()
                        .size();

                if (mqttPayloadCount > 1) {
                    throw new RuntimeException(beanClass.getName() + "#" + method.getName() + " : too many @MqttPayload parameters.");
                }

                if (method.isAnnotationPresent(MqttMapping.class)) {

                    MqttMapping methodMqttMapping = method.getAnnotation(MqttMapping.class);

                    String methodTopic = TopicUtil.preProcessTopic(methodMqttMapping.value());

                    String baseTopic = TopicUtil.preProcessTopic(mqttModuleProperties.getConsumer().getBaseTopic());

                    String topic = TopicUtil.generateTopic(baseTopic, clazzTopic, methodTopic);

                    // 파라미터 ( {%} ) 을 WILD_CARD_WORD 로 대치 후 key 값으로 사용
                    String key = topic.replaceAll("\\{[a-zA-Z0-9]+}", WILD_CARD_WORD);

                    if (mqttMethodMapping.containsKey(key)) {
                        throw new RuntimeException(beanClass.getName() + "#" + method.getName() + " : duplicate mapping methods.");
                    } else {
                        mqttMethodMapping.put(key, TopicMethod.builder()
                                .method(method)
                                .mapping(topic)
                                .wildMapping(key)
                                .build());
                    }
                }
            }
        }
    }

    /**
     * 메서드 실행
     */
    public void invoke(String topic, String payload, TopicMethod topicMappingMethod) throws Throwable {
        try {
            Method method = topicMappingMethod.getMethod();
            Object[] parameters = this.getParameters(topic, payload, topicMappingMethod);

            log.debug("{} => method: {}#{}, parameters: {}", topic, method.getDeclaringClass(), method.getName(), parameters);

            Class<?> methodClazz = method.getDeclaringClass();
            Object methodClazzBean = applicationContext.getBean(methodClazz);
            method.setAccessible(true);
            method.invoke(methodClazzBean, parameters);

        } catch (IllegalAccessException | InvocationTargetException exception) {
            if (exception instanceof InvocationTargetException invocationTargetException) {
                throw invocationTargetException.getTargetException();
            } else {
                throw exception;
            }
        }
    }

    /**
     * 매핑 메서드 탐색
     * @param topic 토픽
     * @return 매칭된 매핑 메서드 리스트
     */
    public List<TopicMethod> getTopicMappingMethods(String topic) {

        List<TopicMethod> topicMappingMethods = new ArrayList<>();
        List<String> topicSplit = Arrays.stream(topic.split("/")).toList();

        for (String wildMapping : this.mqttMethodMapping.keySet()) {
            List<String> wildMappingSplit = Arrays.stream(wildMapping.split("/")).toList();

            if (topicSplit.size() == wildMappingSplit.size()) {
                boolean isAllMatch = true;
                for (int index = 0; index < topicSplit.size(); index++) {

                    String topicTemp = topicSplit.get(index);
                    String wildMappingTemp = wildMappingSplit.get(index);

                    if (WILD_CARD_WORD.equals(wildMappingTemp)) continue;

                    if (!topicTemp.equals(wildMappingTemp)) {
                        isAllMatch = false;
                        break;
                    }
                }
                if (isAllMatch) topicMappingMethods.add(this.mqttMethodMapping.get(wildMapping));
            }
        }
        return topicMappingMethods;
    }

    /**
     * 메서드 파라 미터 정리
     * @param topic 토픽
     * @param topicMappingMethod 매핑 메서드 Dto
     * @return 메서드 파라미터 순서에 맞는 Object 배열
     */
    private Object[] getParameters(String topic, String payload, TopicMethod topicMappingMethod) {

        List<Object> parameters = new ArrayList<>();

        List<String> topicSplit = Arrays.stream(topic.split("/")).toList();

        Method method = topicMappingMethod.getMethod();
        String[] mappings = topicMappingMethod.getMappings();

        // topic 에서 파라 미터 값 추출
        Map<String, String> topicParameters = new HashMap<>(); // <파라 미터 명, 파라 미터 값>
        for (int parameterIndex : topicMappingMethod.getParameterIndexes()) {
            String fieldName = mappings[parameterIndex];

            fieldName = fieldName.replace("{", "");
            fieldName = fieldName.replace("}", "");

            topicParameters.put(fieldName, topicSplit.get(parameterIndex));
        }

        for (Parameter parameter : method.getParameters()) {

            Class<?> parameterType = parameter.getType();
            String parameterName = parameter.getName();

            // @PathVariable 체크
            if (parameter.isAnnotationPresent(PathVariable.class)) {
                PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);

                parameterName = pathVariable.value().isEmpty() ? parameterName : pathVariable.value();
                String parameterValueStr = topicParameters.get(parameterName);

                Object parameterValue = null;
                if (parameterType.equals(short.class) || parameterType.equals(Short.class)) {
                    parameterValue = Short.parseShort(parameterValueStr);
                } else if (parameterType.equals(int.class) || parameterType.equals(Integer.class)) {
                    parameterValue = Integer.parseInt(parameterValueStr);
                } else if (parameterType.equals(long.class) || parameterType.equals(Long.class)) {
                    parameterValue = Long.parseLong(parameterValueStr);
                } else if (parameterType.equals(float.class) || parameterType.equals(Float.class)) {
                    parameterValue = Float.parseFloat(parameterValueStr);
                } else if (parameterType.equals(double.class) || parameterType.equals(Double.class)) {
                    parameterValue = Double.parseDouble(parameterValueStr);
                } else if (parameterType.equals(String.class)) {
                    parameterValue = parameterValueStr;
                }

                parameters.add(parameterValue);

            }
            // @MqttPayload 체크
            else if (parameter.isAnnotationPresent(MqttPayload.class)) {
                try {
                    // @NoArgConstructor 가 필요 -> ObjectMapper 는 기본 생성자가 없으면 파싱이 불가능
                    Object parameterValue = objectMapper.readValue(payload, parameterType);
                    parameters.add(parameterValue);
                } catch (JsonProcessingException e) {
                    parameters.add(null);
                }
            } else {
                parameters.add(null);
            }
        }

        return parameters.toArray();
    }

    /**
     * Mqtt Mapping Method Dto
     */
    public static class TopicMethod {

        @Getter
        private final Method method;

        @Getter
        // 와일드 카드를 포함한 매핑 문자열
        private final String wildMapping;

        // 와일드 카드를 포함한 매핑 문자 배열
        private final String[] wildMappings;

        @Getter
        // 원본 매핑 문자열
        private final String mapping;

        @Getter
        // 원본 매핑 문자 배열
        private final String[] mappings;

        @Builder
        public TopicMethod(Method method, String wildMapping, String mapping) {
            this.method = method;
            this.wildMapping = wildMapping;
            this.wildMappings = wildMapping.split("/");
            this.mapping = mapping;
            this.mappings = mapping.split("/");
        }

        // topic 상 파라미터 위치 인덱스 목록
        public List<Integer> getParameterIndexes() {
            List<Integer> parameterIndexes = new ArrayList<>();
            for (int index = 0; index < this.wildMappings.length; index++) {
                if (WILD_CARD_WORD.equals(this.wildMappings[index])) {
                    parameterIndexes.add(index);
                }
            }
            return parameterIndexes;
        }
    }
}


