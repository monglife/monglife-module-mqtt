package com.monglife.module.mqtt.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication//(scanBasePackages = "com.monglife.module.mqtt")
public class MonglifeModuleMqttApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonglifeModuleMqttApplication.class, args);
    }

}