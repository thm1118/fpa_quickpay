package com.fintech.quickpay.controller;

import com.fintech.quickpay.entity.Notification;
import com.fintech.quickpay.security.CurrentUser;
import com.fintech.quickpay.security.UserPrincipal;
import com.fintech.quickpay.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@CurrentUser UserPrincipal principal) {
        List<Notification> notifications = notificationService.getUserNotifications(principal.getUser());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@CurrentUser UserPrincipal principal) {
        List<Notification> notifications = notificationService.getUnreadNotifications(principal.getUser());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@CurrentUser UserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal.getUser());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@CurrentUser UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getUser());
        return ResponseEntity.ok().build();
    }
}
