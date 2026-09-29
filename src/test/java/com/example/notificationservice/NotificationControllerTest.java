package com.example.notificationservice;

import com.example.notificationservice.dto.SendNotificationRequest;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.NotificationMedium;
import com.example.notificationservice.entity.NotificationStatus;
import com.example.notificationservice.exception.NotificationValidationException;
import com.example.notificationservice.exception.UserNotFoundException;
import com.example.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    void shouldAcceptValidRequest() throws Exception {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(10L);
        notification.setMedium(NotificationMedium.SMS);
        notification.setMessage("Your OTP is 123456");
        notification.setStatus(NotificationStatus.SENT);
        notification.setScheduledAt(Instant.now());

        when(notificationService.sendImmediateNotification(10L, NotificationMedium.SMS, "Your OTP is 123456"))
                .thenReturn(notification);

        SendNotificationRequest request = new SendNotificationRequest(10L, NotificationMedium.SMS, "Your OTP is 123456");

        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":null,\"medium\":\"SMS\",\"message\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForUserNotFound() throws Exception {
        when(notificationService.sendImmediateNotification(eq(99L), eq(NotificationMedium.SMS), eq("hello")))
                .thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":99,\"medium\":\"SMS\",\"message\":\"hello\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotificationById() throws Exception {
        Notification notification = new Notification();
        notification.setId(5L);
        notification.setUserId(1L);
        notification.setMedium(NotificationMedium.SLACK);
        notification.setMessage("hello");
        notification.setStatus(NotificationStatus.PENDING);
        when(notificationService.getNotificationById(5L)).thenReturn(notification);

        mockMvc.perform(get("/api/v1/notifications/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }
}
