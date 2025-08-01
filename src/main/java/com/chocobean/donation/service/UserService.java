package com.chocobean.donation.service;

import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    // 생성자 주입
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String userId, String password) {
        Optional<User> userOpt = userRepository.findByUserIdAndUserPassword(userId, password);
        return userOpt.isPresent();
    }
}
