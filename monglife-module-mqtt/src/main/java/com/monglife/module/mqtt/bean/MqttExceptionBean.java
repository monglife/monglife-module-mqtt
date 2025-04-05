package com.monglife.module.mqtt.bean;

import com.monglife.module.mqtt.annotation.MqttConsumer;
import com.monglife.module.mqtt.annotation.MqttConsumerAdvice;
import com.monglife.module.mqtt.annotation.MqttExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttExceptionBean implements InitializingBean {

    private final ApplicationContext applicationContext;

    private final Map<Class<? extends Throwable>, Method> mqttExceptionHandlerMapping;

    @Autowired
    public MqttExceptionBean(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.mqttExceptionHandlerMapping = new HashMap<>();
    }

    @Override
    public void afterPropertiesSet() {

        String[] mqttConsumerAdviceBeanNames = applicationContext.getBeanNamesForAnnotation(MqttConsumerAdvice.class);
        String[] mqttConsumerBeanNames = applicationContext.getBeanNamesForAnnotation(MqttConsumer.class);

        searchAll(mqttConsumerAdviceBeanNames);
        searchAll(mqttConsumerBeanNames);
    }

    /**
     * Mqtt Exception Handler Method Search All
     * @param beanNames Components Names
     */
    private void searchAll(String[] beanNames) {
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);

            for (Method method : beanClass.getDeclaredMethods()) {
                // MqttExceptionHandler Annotation 을 가진 메서드
                if (method.isAnnotationPresent(MqttExceptionHandler.class)) {
                    // 반환 타입이 void 가 아닌 경우에 패스
                    if (method.getReturnType() != void.class) {
                        throw new RuntimeException(beanClass.getName() + "#" + method.getName() + " : @MqttExceptionHandler must return void.");
                    }

                    MqttExceptionHandler mqttExceptionBean = method.getAnnotation(MqttExceptionHandler.class);
                    Class<? extends Throwable>[] exceptions = mqttExceptionBean.value();

                    for (Class<? extends Throwable> exception : exceptions) {
                        mqttExceptionHandlerMapping.put(exception, method);
                    }
                }
            }
        }
    }

    /**
     * 예외 처리 메서드 실행
     * @param throwable 예외 클래스
     */
    public void invoke(Throwable throwable) throws Exception {

        boolean isInvoke = false;

        for (Class<?> exceptionMappingClazz : mqttExceptionHandlerMapping.keySet()) {
            // 예외 처리 메서드가 있는 경우 실행
            if (exceptionMappingClazz.isAssignableFrom(throwable.getClass())) {
                // 예외 처리 메서드 실행
                try {
                    Method method = mqttExceptionHandlerMapping.get(exceptionMappingClazz);
                    Class<?> methodClazz = method.getDeclaringClass();
                    Object exceptionHandlerClazzBean = applicationContext.getBean(methodClazz);
                    method.setAccessible(true);
                    method.invoke(exceptionHandlerClazzBean, Collections.singletonList(throwable).toArray());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("invoke mqtt exception bean method error.");
                } finally {
                    isInvoke = true;
                }
            }
        }

        // 예외 클래스에서 처리 못하는 경우 throw
        if (!isInvoke) {
            throw new Exception(throwable);
        }
    }
}


