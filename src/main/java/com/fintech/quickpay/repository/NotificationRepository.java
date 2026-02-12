package com.fintech.quickpay.repository;

import com.fintech.quickpay.entity.Notification;
import com.fintech.quickpay.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsReadFalse(User user);

    Long countByUserAndIsReadFalse(User user);

    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
}
