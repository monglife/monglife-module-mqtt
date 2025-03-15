package com.monglife.module.mqtt.utils;

public class TopicUtil {

    public static String preProcessTopic(String topic) {

        String nextTopic = topic;

        while (nextTopic.startsWith("/")) {
            nextTopic = nextTopic.substring(1);
        }

        while (nextTopic.endsWith("/")) {
            nextTopic = nextTopic.substring(0, nextTopic.length() - 1);
        }

        return nextTopic;
    }

    public static String generateTopic(String ... topics) {

        if (topics == null || topics.length == 0) return "";

        StringBuilder generateTopic = new StringBuilder(topics[0]);

        for (int index = 1; index < topics.length; index++) {
            String topic = topics[index];

            if (topic.isBlank()) continue;

            generateTopic.append("/").append(topic);
        }

        return generateTopic.toString();
    }

    public static Integer countTopicPrefix(String topic, String topicPrefix) {

        int count = 0;
        int index = 0;

        while ((index = topic.indexOf(topicPrefix, index)) != -1) {
            count++;
            index += topicPrefix.length();
        }

        return count;
    }
}
