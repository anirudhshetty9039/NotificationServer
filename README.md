# Notification Service

A modular Spring Boot notification service for sending SMS, WhatsApp, and Slack messages with a reusable strategy-based design, validation, retries, and scheduling.

## Overview

This project models a simple monolithic notification platform that can process both ad-hoc and scheduled notifications. The system supports multiple notification mediums without hard-coded branching and is structured so new channels such as Email can be added with minimal disruption.

## Architecture Diagram

```text
Client / Admin
   |
   v
[Controller]
   |
   v
[Service Layer]
   |
   +--> [NotificationSenderRegistry]
   |       |
   |       +--> SMS Sender --> SmsProvider --> FakeSmsProvider
   |       +--> WhatsApp Sender --> WhatsAppProvider --> FakeWhatsAppProvider
   |       +--> Slack Sender --> SlackProvider --> FakeSlackProvider
   |
   +--> [NotificationRepository]
   |
   +--> [UserRepository]
   |
   v
[H2 Database]
```

## Design Decisions

- Strategy Pattern is used for notification mediums so each sender owns its validation and delivery logic.
- Registry-based lookup avoids giant if/else or switch statements inside the core service.
- Service layer is responsible for business rules and retry behavior.
- Scheduler only triggers due notifications; it does not implement business logic.
- User inventory is assumed to exist and is referenced by userId.
- The domain includes a future-ready subscription flag on the User to support unsubscribe work without reshaping the service layer.

## Design Patterns Used

- Strategy Pattern: NotificationSender implementations for SMS, WhatsApp, and Slack.
- Dependency Injection: Spring-managed beans and constructor injection.
- Repository Pattern: Spring Data JPA repositories.
- Template-based validation: medium-specific validation is delegated to sender implementations.

## Notification Flow

1. An administrator sends a request to POST /api/v1/notifications/send.
2. The controller validates the DTO.
3. The service resolves the user by userId.
4. The notification medium is resolved via the sender registry.
5. Delivery is attempted with retry logic.
6. The notification status changes from PENDING to PROCESSING to SENT or FAILED.
7. Scheduled notifications are picked up by the scheduler based on scheduledAt.

## Retry Flow

The service retries transient failures up to a configurable maximum count configured in application.yml.

Configuration:

```yaml
notification:
  retry:
    max-attempts: 3
    delay: 1000
```

Behavior:

- Validation errors are not retried.
- Delivery errors are retried until the max attempt count is reached.
- The status is set to RETRYING during the retry cycle.
- Final failure records the last error and sets status to FAILED.

## Scheduling

The scheduler is implemented with Spring's @Scheduled annotation:

```java
@Scheduled(fixedDelayString = "${notification.scheduler.delay}")
public void processPendingNotifications() {
    notificationService.processDueNotifications();
}
```

The scheduler pulls only notifications that are due and ready for processing using a database query against scheduledAt and status. This keeps the design efficient and avoids loading all notifications into memory.

## API Documentation

### Send notification immediately

POST /api/v1/notifications/send

Request body:

```json
{
  "userId": 123,
  "medium": "SMS",
  "message": "Your OTP is 123456"
}
```

### Schedule notification

POST /api/v1/notifications/schedule

Request body:

```json
{
  "userId": 123,
  "medium": "WHATSAPP",
  "message": "Reminder: your appointment is tomorrow.",
  "scheduledAt": "2026-09-30T10:00:00Z"
}
```

### Fetch notification status

GET /api/v1/notifications/{id}

### Query by status

GET /api/v1/notifications?status=PENDING

## Core Domain Model

### User

- id
- name
- email
- phoneNumber
- slackId
- whatsappNumber
- notificationsEnabled

This includes a simple subscription-related flag so global unsubscribe support can be added later without rewriting the core notification flow.

### Notification

- id
- userId
- medium
- message
- status
- scheduledAt
- createdAt
- updatedAt
- retryCount
- lastError

## Example Curl Requests

```bash
curl -X POST http://localhost:8080/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"medium":"SMS","message":"Your OTP is 123456"}'

curl -X POST http://localhost:8080/api/v1/notifications/schedule \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"medium":"SLACK","message":"Your report is ready","scheduledAt":"2026-09-30T10:00:00Z"}'

curl http://localhost:8080/api/v1/notifications/1
```

## How to Add Email Without Modifying Existing Business Logic

Email can be added by following the same strategy pattern:

1. Add EMAIL to NotificationMedium.
2. Implement EmailNotificationSender that implements NotificationSender.
3. Implement an EmailProvider abstraction, e.g. FakeEmailProvider.
4. Register the sender as a Spring bean.
5. The registry automatically picks it up without touching NotificationService logic.

This is the key benefit of the registry + strategy approach:

```java
Map<NotificationMedium, NotificationSender>
```

No giant switch statement is needed. No business logic changes are required when a new medium is introduced.

## Future Unsubscribe Support

The User model already has a notificationsEnabled flag, which is a lightweight extension point for future global unsubscribe behavior. A future Subscription or UserNotificationPreference model can be added without major refactoring because the sending logic is already abstracted behind the sender strategy and the service consults the user context before delivery.

## Future Authentication

The controller layer is designed to be extended with Spring Security later. The core service logic remains independent of authentication concerns, which keeps the business logic clean and testable.

## How to Run

```bash
mvn clean package
java -jar target/notification-service-0.0.1-SNAPSHOT.jar
```

Then open:

- http://localhost:8080/api/v1/notifications
- H2 console: http://localhost:8080/h2-console

## How to Run Tests

```bash
mvn test
```

## Important Project Structure

```text
src/
  main/
    java/com/example/notificationservice/
      config/
      controller/
      dto/
      entity/
      exception/
      provider/
      repository/
      scheduler/
      service/
      strategy/
      NotificationServiceApplication.java
  test/
    java/com/example/notificationservice/
```

## Tradeoffs

- This is intentionally a modular monolith, not a distributed platform.
- For an interview project, the provider implementations are fake and in-memory rather than real Twilio/Slack integrations.
- Retry behavior is intentionally simple and deterministic to keep the code understandable.

## Future Improvements

- Add email sender and provider.
- Add subscription management APIs and DB-backed preferences.
- Add request-level authentication and audit logs.
- Add metrics and monitoring for delivery outcomes.
- Introduce a queue for asynchronous processing in larger production workloads.

## Major Classes

- NotificationServiceApplication: Spring Boot bootstrapping entry point.
- NotificationService: Core business logic and retry orchestration.
- NotificationController: REST exposure.
- NotificationScheduler: Timer-based trigger for pending notifications.
- NotificationSenderRegistry: Maps medium to correct sender.
- SmsNotificationSender / WhatsAppNotificationSender / SlackNotificationSender: Strategy implementations.
- UserRepository / NotificationRepository: Data access layers.
- GlobalExceptionHandler: Consistent API error handling.

## Interview Notes

The design intentionally favors extensibility and readability over premature complexity. The use of strategy plus registry demonstrates a clear understanding of object-oriented design, dependency injection, and enterprise application architecture.
