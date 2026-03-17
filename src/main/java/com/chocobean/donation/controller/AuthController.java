package com.chocobean.donation.controller;

import com.chocobean.donation.dto.*;
import com.chocobean.donation.service.EmailService;
import com.chocobean.donation.service.UserService;
import com.chocobean.donation.utils.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
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
    private final EmailService emailService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


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

        // Redis에 refreshToken 저장 (key: refreshToken:{userId}, value: refreshToken, TTL: 15일)
        redisTemplate.opsForValue().set(
                "refreshToken:" + userDetails.getUsername(),
                refreshToken,
                Duration.ofDays(15)
        );

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpForm signUpForm) {
//        System.out.println("Redis value for 'os': " + redisTemplate.opsForValue().get("os"));
        String status = redisTemplate.opsForValue().get("email:status:" + signUpForm.getUserEmail());

        if (status == null || !status.equals("VERIFIED")) {
            return ResponseEntity.badRequest().body("이메일 인증이 만료되었거나 완료되지 않았습니다.");
        }

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
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        try {
            // 1. 토큰에서 userId 추출
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            System.out.println(username);

            // 2. Redis에서 저장된 refreshToken 조회
            String savedToken = redisTemplate.opsForValue().get("refreshToken:" + username);

            // 3. Redis 토큰과 전달받은 토큰 비교
            if (savedToken == null || !savedToken.equals(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
            }

            // 4. JWT 자체 유효성 검증
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtTokenUtil.validateToken(refreshToken, userDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
            }

            // 5. 새 accessToken 발급
            String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token Expired");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication Failed");
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
        }
        // Redis에서 refreshToken 삭제 → 해당 토큰으로 재발급 불가
        redisTemplate.delete("refreshToken:" + userDetails.getUsername());
        return ResponseEntity.ok("로그아웃 완료");
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

    //메일 발송
    @GetMapping("/auth/sendEmailVerification")
    public ResponseEntity<?> sendEmailVerification(
            @RequestParam("email") String email
    ) {
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        redisTemplate.opsForValue().set(
                "email:verify:" + email,
                code,
                Duration.ofMinutes(5)
        );
//        System.out.println(code);
//        System.out.println(email);
//        System.out.println(redisTemplate.opsForValue().get("email:verify:" + email));
        //이메일 발송
        try {
            String subject = "[초코빈] 회원가입 인증번호 안내";
            String content = "안녕하세요. 초코빈입니다.\n\n" +
                    "인증번호는 [" + code + "] 입니다.\n" +
                    "5분 이내에 입력해주세요.";

            emailService.sendEmail(email, subject, content);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("메일 발송에 실패했습니다.");
        }
    }
    //메일 인증
    @GetMapping("/auth/verifyCode")
    public ResponseEntity<?> verifyCode(
            @RequestParam String email,
            @RequestParam String code
    ) {
        String savedCode = redisTemplate.opsForValue().get("email:verify:" + email);
        if (savedCode != null && savedCode.equals(code)) {
            redisTemplate.delete("email:verify:" + email);

            redisTemplate.opsForValue().set(
                    "email:status:" + email,
                    "VERIFIED",
                    Duration.ofMinutes(10)
            );

            return ResponseEntity.ok("인증 성공");
        }
        return ResponseEntity.status(400).body("인증번호가 틀렸거나 만료되었습니다.");
    }

}
