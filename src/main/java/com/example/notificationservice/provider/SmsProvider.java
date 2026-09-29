package com.example.notificationservice.provider;

public interface SmsProvider {
    void send(String phoneNumber, String message);
}
