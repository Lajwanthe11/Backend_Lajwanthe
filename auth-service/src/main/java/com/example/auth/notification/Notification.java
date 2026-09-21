package com.example.auth.notification;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * User who should receive the notification.
     */
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String tenantId;

    /*
     * Username is stored for notification display/history.
     */
    @Column(nullable = false, length = 100)
    private String username;

    /*
     * Type of notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    /*
     * Channel through which notification is delivered.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    /*
     * Delivery status.
     *
     * PENDING -> notification created but not delivered
     * SENT    -> notification delivered successfully
     * FAILED  -> delivery failed
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    /*
     * Notification title.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /*
     * Notification message.
     */
    @Column(nullable = false, length = 2000)
    private String message;

    /*
     * Optional Security Alert ID.
     *
     * This allows Notification Management to identify
     * which Security Alert generated this notification.
     */
    @Column(name = "alert_id")
    private Long alertId;

    /*
     * Read state is independent from delivery status.
     *
     * Example:
     * status = SENT
     * read = false
     *
     * means notification was delivered but not read yet.
     */
    @Column(nullable = false)
    private boolean read = false;

    /*
     * Time when notification was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /*
     * Time when notification was successfully delivered.
     */
    private LocalDateTime sentAt;

    /*
     * Time when user read the notification.
     */
    private LocalDateTime readAt;

    /*
     * Automatically set creation time and default delivery status.
     */
    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = NotificationStatus.PENDING;
        }
    }

    // ---------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}