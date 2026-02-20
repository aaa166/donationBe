package com.chocobean.donation.service;

import com.chocobean.donation.dto.*;
import com.chocobean.donation.entity.Payment;
import com.chocobean.donation.entity.Provider;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.PaymentRepository;
import com.chocobean.donation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaymentRepository paymentRepository;



    @Transactional
    public User signUp(SignUpForm signUpForm) {
        // 아이디 형식 검증
        String idRegex = "^[a-z][a-z0-9]{2,}$";
        if (!Pattern.matches(idRegex, signUpForm.getUserId())) {
            throw new IllegalArgumentException("아이디 형식이 올바르지 않습니다.");
        }

        // 아이디 중복 확인
        if (userRepository.existsByUserId(signUpForm.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        User user = new User();
        user.setUserName(signUpForm.getUserName());
        user.setUserId(signUpForm.getUserId());
        // ★★★ 비밀번호 암호화 (가장 중요)
        user.setUserPassword(passwordEncoder.encode(signUpForm.getUserPassword()));
        user.setUserEmail(signUpForm.getUserEmail());
        user.setUserPhone(signUpForm.getUserPhone());
        user.setUserRole(1);
        user.setUserState("A");
        user.setTotalAmount(0L);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserInfoByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        return new UserResponse(user);
    }


    @Transactional(readOnly = true)
    public List<MyDonation> getDonationListByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));



        List<Payment> payments = paymentRepository.findPaymentsWithDonationByUserNo(user.getUserNo());


        return payments.stream()
                .map(payment -> new MyDonation(
                        payment.getDonation().getDonationTitle(), // JOIN FETCH 덕분에 추가 쿼리 없이 접근 가능
                        payment.getPayAmount(),
                        payment.getPayDate()
                ))
                .collect(Collectors.toList());
    }

//    public int getRoleByUserName(String userName) {
//        int role = userRepository.getRoleByUserName(userName);
//
//        return role;
//    }
    @Transactional
    public int getRoleByUserName(String userId) {
        User user= userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));;
        int role = user.getUserRole();

        return role;
    }

    public Long getUserNoByUserId(String userId) {
        return userRepository.getUserNoByUserId(userId);
    }
    @Transactional
    public void addUserAmount(Long userNo, Long amount) {
        User user = userRepository.findByUserNo(userNo);
        user.setTotalAmount(user.getTotalAmount() + amount);

    }

    @Transactional
    public List<UserState> getUserState() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::convertToUserStateDto) //
                .collect(Collectors.toList());
    }
    private UserState convertToUserStateDto(User user) {


        return new UserState(
                user.getUserNo(),
                user.getUserId(),
                user.getUserName(),
                user.getUserEmail(),
                user.getUserPhone(),
                user.getUserRole(),
                user.getTotalAmount(),
                user.getUserState()
        );
    }

    @Transactional
    public void changeUserState(Map<String, Object> userData) {
        String userState = (String) userData.get("userState");
        Long userNo = Long.valueOf(userData.get("userNo").toString());
        if (Objects.equals(userState, "A")){
            userRepository.updateUserState(userNo, "I");
        }else{
            userRepository.updateUserState(userNo, "A");
        }
    }

    @Transactional
    public void updateUserInfo(Long userNo, UserResponse userResponse) {
       String id = userResponse.getUserId();
       String email = userResponse.getUserEmail();
       String phone = userResponse.getUserPhone();

       userRepository.updateUserInfo(userNo,id,email,phone);
    }
    @Transactional
    public boolean duplicateIdCheck(String userId) {
        System.out.println(userId);
        System.out.println(userRepository.countByUserId(userId));
        return userRepository.countByUserId(userId) == 0;
    }
    @Transactional
    public boolean duplicateEmailCheck(String email) {
        return  userRepository.countByUserEmail(email) == 0;
    }
    @Transactional
    public boolean duplicatePhoneCheck(String phone) {
        return  userRepository.countByUserPhone(phone) == 0;
    }

    @Transactional
    public String updatePassword(Map<String, String> inputData, Long userNo) {
        User user = userRepository.findByUserNo(userNo);
        //비밀번호 확인
        if (!passwordEncoder.matches(inputData.get("currentPassword"),user.getUserPassword())) {
            return "unauthorized";
        }
        //새 비밀번호 일치 확인
        if (!inputData.get("newPassword").equals(inputData.get("confirmPassword"))){
            return "mismatch";
        }

        String encodedPassword =
                passwordEncoder.encode((String) inputData.get("newPassword"));
        user.updatePassword(encodedPassword);

        return "ok";
    }

    //소셜로그인
    @Transactional
    public User socialLogin(String userName, String userEmail, String userPhone,
                            String providerId, Provider provider) {

        Optional<User> optionalUser = userRepository.findByProviderAndProviderId(provider, providerId);

        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            User user = new User();
            user.setUserName(userName);
            user.setUserEmail(userEmail);
            user.setUserPhone(userPhone);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setUserRole(1);
            user.setUserState("A");
            user.setTotalAmount(0L);

            return userRepository.save(user);
        }
    }
}