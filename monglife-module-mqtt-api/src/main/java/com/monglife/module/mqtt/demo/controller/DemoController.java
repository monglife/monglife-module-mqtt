package com.monglife.module.mqtt.demo.controller;

import com.monglife.module.mqtt.demo.publisher.DemoPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DemoController {

    private final DemoPublisher demoPublisher;

    @PostMapping("/mqtt")
    public void mqttTest() {
        // publish test message
        demoPublisher.demoPublish(1L, "publish test message.");
    }
}
