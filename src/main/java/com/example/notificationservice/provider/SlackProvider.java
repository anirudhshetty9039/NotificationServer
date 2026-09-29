package com.example.notificationservice.provider;

public interface SlackProvider {
    void send(String slackId, String message);
}
