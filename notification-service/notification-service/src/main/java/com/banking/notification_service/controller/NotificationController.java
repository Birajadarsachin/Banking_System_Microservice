package com.banking.notification_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping("/send")
    public String sendNotification(@RequestBody String message) {

        logger.info("Received NOTIFICATION request with message={}", message);

        // Simulated notification sending
        logger.info("NOTIFICATION SENT successfully: {}", message);

        return "Notification delivered";
    }
}
