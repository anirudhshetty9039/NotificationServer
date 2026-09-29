package com.example.notificationservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 30)
    private String phoneNumber;

    @Size(max = 50)
    private String slackId;

    @Size(max = 30)
    private String whatsappNumber;

    @Column(nullable = false)
    private boolean notificationsEnabled = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSlackId() {
        return slackId;
    }

    public void setSlackId(String slackId) {
        this.slackId = slackId;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean hasContactInfo(NotificationMedium medium) {
        return switch (medium) {
            case SMS -> phoneNumber != null && !phoneNumber.isBlank();
            case WHATSAPP -> whatsappNumber != null && !whatsappNumber.isBlank();
            case SLACK -> slackId != null && !slackId.isBlank();
            case EMAIL -> email != null && !email.isBlank();
        };
    }
}
