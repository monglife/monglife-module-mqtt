package com.monglife.module.mqtt.config;

import com.monglife.module.mqtt.consumer.MqttConsumer;
import com.monglife.module.mqtt.property.MqttModuleProperties;
import com.monglife.module.mqtt.utils.TopicUtil;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@AutoConfiguration
@EnableConfigurationProperties(MqttModuleProperties.class)
public class MqttAutoConfig {

    /**
     * Mqtt Connect Configuration
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory(MqttModuleProperties mqttModuleProperties) {

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);

        options.setServerURIs(new String[]{ "tcp://" + mqttModuleProperties.getHost() + ":" + mqttModuleProperties.getPort() });
        options.setUserName(mqttModuleProperties.getUserName());
        options.setPassword(mqttModuleProperties.getPassword().toCharArray());

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

        messageHandler.setAsync(true);
        messageHandler.setDefaultQos(2);
        messageHandler.setDefaultTopic(TopicUtil.preProcessTopic(mqttModuleProperties.getPublisher().getBaseTopic()) + "/error");

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
            MqttModuleProperties mqttModuleProperties
    ) {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(MqttAsyncClient.generateClientId(), mqttPahoClientFactory);

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setOutputChannel(mqttInboundChannel);


        String baseTopic = TopicUtil.preProcessTopic(mqttModuleProperties.getConsumer().getBaseTopic());

        // 구독 토픽 추가
        mqttModuleProperties.getConsumer().getTopics().forEach(topic -> {

            topic = TopicUtil.preProcessTopic(topic);

            String consumeTopic = TopicUtil.generateTopic(baseTopic, topic);

            adapter.addTopic(consumeTopic);
        });

        return adapter;
    }

    @Bean
    @ConditionalOnProperty(value = "module.mqtt.consumer.enabled", havingValue = "true")
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public MessageHandler mqttInbound(@Autowired MqttConsumer mqttConsumer) {
        return mqttConsumer;
    }
}
