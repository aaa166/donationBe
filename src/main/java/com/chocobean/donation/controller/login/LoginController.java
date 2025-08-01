package com.chocobean.donation.controller.login;

import com.chocobean.donation.dto.UserLogin;
import com.chocobean.donation.service.UserService;
import com.chocobean.donation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {
    private UserRepository userRepository;
    private final UserService userService;


    @Autowired
    public LoginController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
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

        boolean success = userService.login(user.getId(), user.getPassword());
        Map<String, String> response = new HashMap<>();

        if (success) {
            System.out.println("로그인 성공");
            response.put("status", "success");
            response.put("message", "로그인 성공");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } else {
            System.out.println("로그인 실패");
            response.put("status", "fail");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


}
