package com.monglife.module.mqtt.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MqttResponseEntity<T> {

    private T body;

    private List<String> topics;

    @Builder
    public MqttResponseEntity(T body, List<String> topics) {
        this.body = body;
        this.topics = topics;
    }

    public static <T> MqttResponseEntityBuilder<T> body(T body) {
        return new MqttResponseEntityBuilder<T>().body(body);
    }

    public static <T> MqttResponseEntity<T> topic(final String... topics) {
        return new MqttResponseEntity<T>(null, Arrays.stream(topics).toList());
    }

    public static <T> MqttResponseEntity<T> topics(final List<String> topics) {
        return new MqttResponseEntity<T>(null, topics.stream().toList());
    }

    public static <T> MqttResponseEntity<T> empty() {
        return new MqttResponseEntity<T>(null, new ArrayList<>());
    }

    public static class MqttResponseEntityBuilder<T> {

        private T body;

        private List<String> topics;

        MqttResponseEntityBuilder() {
        }

        public MqttResponseEntityBuilder<T> body(final T body) {
            this.body = body;
            return this;
        }

        public MqttResponseEntity<T> topic(final String... topics) {
            this.topics = Arrays.stream(topics).toList();
            return new MqttResponseEntity<T>(this.body, this.topics);
        }

        public MqttResponseEntity<T> topics(final List<String> topics) {
            this.topics = topics.stream().toList();
            return new MqttResponseEntity<T>(this.body, this.topics);
        }
    }
}
