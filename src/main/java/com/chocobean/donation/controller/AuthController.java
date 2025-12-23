package com.chocobean.donation.controller;

import com.chocobean.donation.dto.*;
import com.chocobean.donation.service.UserService;
import com.chocobean.donation.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final UserDetailsService userDetailsService;


    @GetMapping("/login")
    public ResponseEntity<?> login() {
        return ResponseEntity.ok("good!!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody UserLogin userLogin) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLogin.getId(), userLogin.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(userLogin.getId());
        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpForm signUpForm) {
        try {
            userService.signUp(signUpForm);
            return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mypage")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        // userDetails에서 사용자 ID(PK가 아닌 로그인 ID)를 가져옵니다.
        String userId = userDetails.getUsername();

        // 서비스 레이어를 통해 사용자 정보를 DTO로 받아옵니다.
        UserResponse userInfo = userService.getUserInfoByUserId(userId);

        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/mydonation")
    public ResponseEntity<List<MyDonation>> getMyPayments(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();

        List<MyDonation> donationList = userService.getDonationListByUserId(userId);

        return ResponseEntity.ok(donationList);
    }

    @GetMapping("/admin/userState")
    public ResponseEntity<?> getUserState(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userName = userDetails.getUsername();
        int role = userService.getRoleByUserName(userName);

        System.out.println(role);

        List<UserState> userState = userService.getUserState();

        if (role == 0){
            return ResponseEntity.ok(userState);
        }else{
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }
    }
}
