package com.fintech.quickpay.service;

import com.fintech.quickpay.dto.RegisterRequest;
import com.fintech.quickpay.dto.UserDTO;
import com.fintech.quickpay.entity.Account;
import com.fintech.quickpay.entity.User;
import com.fintech.quickpay.exception.ResourceConflictException;
import com.fintech.quickpay.repository.AccountRepository;
import com.fintech.quickpay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user = userRepository.save(user);

        // Create payment account for user
        Account account = new Account();
        account.setUser(user);
        account.setAccountNo(generateAccountNo());
        accountRepository.save(account);

        return convertToDTO(user);
    }

    @Transactional
    public void setPaymentPassword(User user, String paymentPassword) {
        user.setPaymentPassword(passwordEncoder.encode(paymentPassword));
        userRepository.save(user);
    }

    public boolean verifyPaymentPassword(User user, String paymentPassword) {
        if (user.getPaymentPassword() == null) {
            return false;
        }
        return passwordEncoder.matches(paymentPassword, user.getPaymentPassword());
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRealName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setVerified(user.getVerified());
        dto.setHasPaymentPassword(user.getPaymentPassword() != null);
        return dto;
    }

    private String generateAccountNo() {
        return "QP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
