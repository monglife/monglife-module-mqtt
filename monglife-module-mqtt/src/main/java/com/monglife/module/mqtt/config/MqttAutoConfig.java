package com.monglife.module.mqtt.config;

import com.monglife.module.mqtt.aspect.MqttPublishAspect;
import com.monglife.module.mqtt.bean.MqttExceptionBean;
import com.monglife.module.mqtt.bean.MqttExecuteBean;
import com.monglife.module.mqtt.bean.MqttMappingBean;
import com.monglife.module.mqtt.bean.MqttPublishBean;
import com.monglife.module.mqtt.client.MqttOutBoundClient;
import com.monglife.module.mqtt.property.MqttModuleProperties;
import com.monglife.module.mqtt.service.MqttSendService;
import com.monglife.module.mqtt.utils.TopicUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Slf4j
@AutoConfiguration
@ComponentScan(
    basePackageClasses = {
        MqttPublishAspect.class,
        MqttOutBoundClient.class,
        MqttExceptionBean.class,
        MqttExecuteBean.class,
        MqttMappingBean.class,
        MqttPublishBean.class,
        MqttSendService.class,
    }
)
@Import(MqttOutBoundClient.class)
@EnableConfigurationProperties(MqttModuleProperties.class)
public class MqttAutoConfig {

    /**
     * Mqtt Connect Configuration
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory(MqttModuleProperties mqttModuleProperties) {

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setConnectionTimeout(mqttModuleProperties.getConnectTimeout());
        options.setKeepAliveInterval(mqttModuleProperties.getKeepAliveInterval());
        options.setAutomaticReconnect(mqttModuleProperties.getAutomaticReconnect());

        options.setServerURIs(new String[]{ "tcp://" + mqttModuleProperties.getHost() + ":" + mqttModuleProperties.getPort() });
        if (!mqttModuleProperties.getUserName().isBlank()) {
            options.setUserName(mqttModuleProperties.getUserName());
        }

        if (!mqttModuleProperties.getPassword().isBlank()) {
            options.setPassword(mqttModuleProperties.getPassword().toCharArray());
        }

        factory.setConnectionOptions(options);

        return factory;
    }

    /**
     * Mqtt Outbound Configuration
     */
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(
            @Qualifier("mqttClientFactory") MqttPahoClientFactory mqttPahoClientFactory,
            MqttModuleProperties mqttModuleProperties
    ) {

        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(MqttAsyncClient.generateClientId(), mqttPahoClientFactory);

        messageHandler.setDefaultQos(mqttModuleProperties.getPublisher().getQos());
        messageHandler.setDefaultTopic(TopicUtil.preProcessTopic(mqttModuleProperties.getPublisher().getBaseTopic()) + "/error");
        messageHandler.setDefaultRetained(mqttModuleProperties.getPublisher().getRetained());
        messageHandler.setAsync(mqttModuleProperties.getPublisher().getAsync());

        return messageHandler;
    }

    /**
     * Mqtt Inbound Configuration
     */
    @Bean
    @ConditionalOnProperty(value = "module.mqtt.consumer.enabled", havingValue = "true")
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ConditionalOnProperty(value = "module.mqtt.consumer.enabled", havingValue = "true")
    public MessageProducer mqttInboundMessageDrivenAdapter(
            @Qualifier("mqttInboundChannel") MessageChannel mqttInboundChannel,
            @Qualifier("mqttClientFactory") MqttPahoClientFactory mqttPahoClientFactory,
            @Qualifier("mqttMappingBean") MqttMappingBean mqttMappingBean,
            MqttModuleProperties mqttModuleProperties
    ) {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(MqttAsyncClient.generateClientId(), mqttPahoClientFactory);

        adapter.setCompletionTimeout(mqttModuleProperties.getConsumer().getCompletionTimeout());
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(mqttModuleProperties.getConsumer().getQos());
        adapter.setOutputChannel(mqttInboundChannel);

        // 구독 토픽 추가
        mqttMappingBean.getConsumeTopics().forEach(adapter::addTopic);

        return adapter;
    }

    @Bean
    @ConditionalOnProperty(value = "module.mqtt.consumer.enabled", havingValue = "true")
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public MessageHandler mqttInbound(@Qualifier("mqttExecuteBean") MqttExecuteBean mqttExecuteBean) {
        return message -> {
            String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
            String payload = (String) message.getPayload();

            if (topic != null && !topic.isBlank()) {
                try {
                    mqttExecuteBean.invoke(topic, payload);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        };
    }
}
