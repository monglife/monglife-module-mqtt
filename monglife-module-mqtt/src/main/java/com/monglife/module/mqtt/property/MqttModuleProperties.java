package com.monglife.module.mqtt.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "module.mqtt")
public class MqttModuleProperties {

    private String host = "";

    private Integer port = 1883;

    private String userName = "";

    private String password = "";

    private Integer connectTimeout = 30;

    private Integer keepAliveInterval = 60;

    private Boolean automaticReconnect = false;

    private Consumer consumer = new Consumer();

    private Publisher publisher = new Publisher();


    @Getter
    @Setter
    public static class Consumer {

        private Boolean enabled = false;

        private Integer qos = 1;

        private String baseTopic = "";

        private Integer completionTimeout = 5000;
    }

    @Getter
    @Setter
    public static class Publisher {

        private Integer qos = 1;

        private String baseTopic = "";

        private Boolean retained = false;

        private Boolean async = true;
    }
}
