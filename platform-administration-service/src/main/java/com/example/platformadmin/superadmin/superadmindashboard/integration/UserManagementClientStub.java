package com.example.platformadmin.superadmin.superadmindashboard.integration;

import com.example.platformadmin.user.entity.User;
import com.example.platformadmin.user.enums.UserStatus;
import com.example.platformadmin.user.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Component("superAdminUserClientStub")
public class UserManagementClientStub implements UserManagementClient {

    private final UserRepository userRepository;

    public UserManagementClientStub(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserStatistics getUserStatistics() {
        List<User> allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream()
                .filter(u -> !u.isDeleted() && u.getStatus() == UserStatus.ACTIVE)
                .count();
        long onlineUsers = (long) Math.ceil(activeUsers * 0.15); // Dynamic estimated active sessions

        return new UserStatistics(totalUsers, activeUsers, onlineUsers);
    }

    @Override
    public List<LoginActivityRecord> getRecentLoginActivities(int limit) {
        return userRepository.findAll().stream()
                .filter(u -> !u.isDeleted())
                .limit(limit)
                .map(u -> new LoginActivityRecord(
                        String.valueOf(u.getId()),
                        u.getEmail(),
                        "127.0.0.1",
                        u.getUpdatedAt() != null ? u.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : Instant.now(),
                        u.getStatus() == UserStatus.ACTIVE ? "SUCCESS" : "INACTIVE"
                ))
                .toList();
    }
}