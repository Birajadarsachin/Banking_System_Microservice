package com.banking.transaction_service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationClient {

    @Autowired
    private RestTemplate restTemplate;

    public void sendNotification(String message) {
        String url = "http://NOTIFICATION-SERVICE/api/notifications/send";
        restTemplate.postForObject(url, message, String.class);
    }
}
