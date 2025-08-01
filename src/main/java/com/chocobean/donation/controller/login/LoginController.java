package com.chocobean.donation.controller.login;

import com.chocobean.donation.entity.User;
import com.chocobean.donation.dto.UserLogin;
import com.chocobean.donation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class LoginController {
    private UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @GetMapping("/login")
    public ResponseEntity<?> login() {
        return ResponseEntity.ok("good!!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLogin user) {
        System.out.println("-----------------------------------------");
        System.out.println("아이디: " + user.getId());
        System.out.println("비밀번호: " + user.getPassword());



        Optional<User> userOpt = userRepository.findByUserIdAndUserPassword(
                user.getId(),
                user.getPassword()
        );

        if (userOpt.isPresent()) {
            User foundUser  = userOpt.get();
            System.out.println("로그인 성공");
            System.out.println("아이디: " + user.getId());
            System.out.println("비밀번호: " + user.getPassword());
            return ResponseEntity.ok("로그인 성공");
        } else {
            System.out.println("로그인 실패");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }


}
