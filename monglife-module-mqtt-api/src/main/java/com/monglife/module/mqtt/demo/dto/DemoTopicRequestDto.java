package com.monglife.module.mqtt.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DemoTopicRequestDto {

    private String message;

    @Builder
    public DemoTopicRequestDto(String message) {
        this.message = message;
    }
}
