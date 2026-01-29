package com.chocobean.donation.controller;

import com.chocobean.donation.dto.*;
import com.chocobean.donation.service.UserService;
import com.chocobean.donation.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final UserDetailsService userDetailsService;


    @GetMapping("/auth/login")
    public ResponseEntity<?> login() {
        return ResponseEntity.ok("good!!");
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody UserLogin userLogin) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLogin.getId(), userLogin.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(userLogin.getId());
        final String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
        final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails.getUsername());

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));
    }

    @PostMapping("/auth/signup")
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

    @PostMapping("/admin/changeUserState")
    public ResponseEntity<?> changeUserState(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> userData
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userName = userDetails.getUsername();
        int role = userService.getRoleByUserName(userName);

//        String userState = (String) userData.get("userState");
//        Long userNo = Long.valueOf(userData.get("userNo").toString());

        if (role == 0){
            userService.changeUserState(userData);
            return ResponseEntity.ok("상태 변경");
        }else{
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshAccessToken(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        // 1. RefreshToken 만료 체크
        if (jwtTokenUtil.isTokenExpired(refreshToken)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token expired");
        }

        // 2. RefreshToken에서 username 추출
        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

        // 3. 사용자 정보 조회
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        // 4. 새로운 AccessToken 발급
        String newAccessToken =
                jwtTokenUtil.generateAccessToken(userDetails);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken
        ));
    }

    @PatchMapping("/updateUserInfo")
    public ResponseEntity<?> updateUserInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserResponse userResponse
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userId = userDetails.getUsername();
        Long userNo = userService.getUserNoByUserId(userId);
        userService.updateUserInfo(userNo,userResponse);

        return ResponseEntity.ok("ok");

    }
    //중복검사
    @GetMapping("/duplicateIdCheck")
    public ResponseEntity<?> duplicateIdCheck(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("userId") String userId
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        if (userService.duplicateIdCheck(userId)){
            return ResponseEntity.ok("ok");
        }else {
            return ResponseEntity.status(409).body("DUPLICATE_ID");
        }
    }
    @GetMapping("/duplicateEmailCheck")
    public ResponseEntity<?> duplicateEmailCheck(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("email") String email
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        if (userService.duplicateEmailCheck(email)){
            return ResponseEntity.ok("ok");
        }else {
            return ResponseEntity.status(409).body("DUPLICATE_EMAIL");
        }
    }
    @GetMapping("/duplicatePhoneCheck")
    public ResponseEntity<?> duplicatePhoneCheck(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("phone") String phone
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        if (userService.duplicatePhoneCheck(phone)){
            return ResponseEntity.ok("ok");
        }else {
            return ResponseEntity.status(409).body("DUPLICATE_PHONE");
        }
    }

    @PatchMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> inputData
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userId = userDetails.getUsername();
        Long userNo = userService.getUserNoByUserId(userId);

        String check = userService.updatePassword(inputData,userNo);
        if (check.equals("unauthorized")){
            return ResponseEntity.status(422).body("unauthorized");
        }
        if (check.equals("mismatch")){
            return ResponseEntity.status(422).body("mismatch");
        }
        return ResponseEntity.ok("ok");
    }

}
