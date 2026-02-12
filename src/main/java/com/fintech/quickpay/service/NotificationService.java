package com.fintech.quickpay.service;

import com.fintech.quickpay.entity.Notification;
import com.fintech.quickpay.entity.Transaction;
import com.fintech.quickpay.entity.User;
import com.fintech.quickpay.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendTransactionNotification(User user, Transaction transaction, boolean isReceiver) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(Notification.NotificationType.TRANSACTION);

        if (isReceiver) {
            notification.setTitle("Payment Received");
            notification.setContent(String.format("You received %.2f from account %s",
                    transaction.getAmount(), transaction.getFromAccount().getAccountNo()));
        } else {
            notification.setTitle("Payment Sent");
            notification.setContent(String.format("You sent %.2f to account %s. Fee: %.2f",
                    transaction.getAmount(), transaction.getToAccount().getAccountNo(), transaction.getFee()));
        }

        notificationRepository.save(notification);
    }

    @Transactional
    public void sendSecurityAlert(User user, String title, String content) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(Notification.NotificationType.SECURITY);
        notification.setTitle(title);
        notification.setContent(content);
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendSystemNotification(User user, String title, String content) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(Notification.NotificationType.SYSTEM);
        notification.setTitle(title);
        notification.setContent(content);
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(User user) {
        return (List<Notification>) notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unread.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unread);
    }
}
