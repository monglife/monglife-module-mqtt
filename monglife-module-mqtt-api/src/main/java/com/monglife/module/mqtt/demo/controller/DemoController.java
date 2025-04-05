package com.monglife.module.mqtt.demo.controller;

import com.monglife.module.mqtt.demo.publisher.DemoPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DemoController {

    private final DemoPublisher demoPublisher;

    @PostMapping("")
    public void mqttPublishTest() {
        demoPublisher.demoPublish(1L, "publish test message.");
    }

    @PostMapping("/exception")
    public void mqttPublishExceptionHandlerTest() {
        demoPublisher.demoPublishExceptionHandler();
    }

    @PostMapping("/exception/advice")
    public void mqttPublishAdviceExceptionHandleTest() {
        demoPublisher.demoPublishAdviceExceptionHandler();
    }
}
