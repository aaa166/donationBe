package com.chocobean.donation.controller;

import com.chocobean.donation.dto.SignUpForm;
import com.chocobean.donation.dto.UserLogin;
import com.chocobean.donation.service.SignUpService;
import com.chocobean.donation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;
    private final SignUpService signUpService;
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private SignUpService signUpService;


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

    @PostMapping("/signup")
    public ResponseEntity<?> login(@RequestBody SignUpForm signUpForm) {
        System.out.println(signUpForm);
        try {
            signUpService.processSignUp(signUpForm);

            signUpService.insertUser(signUpForm);

        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }


        return ResponseEntity.ok().build();
    }


}
