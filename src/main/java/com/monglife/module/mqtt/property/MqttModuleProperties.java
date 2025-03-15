package com.monglife.module.mqtt.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "module.mqtt")
public class MqttModuleProperties {

    private String host;

    private Integer port;

    private String userName;

    private String password;

    private Consumer consumer = new Consumer();

    private Publisher publisher = new Publisher();


    @Getter
    @Setter
    public static class Consumer {

        private Boolean enabled = false;

        private String baseTopic = "";

        private List<String> topics = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Publisher {

        private String baseTopic = "";
    }
}
