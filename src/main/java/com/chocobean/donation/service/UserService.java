package com.chocobean.donation.service;

import com.chocobean.donation.dto.MyDonation;
import com.chocobean.donation.dto.SignUpForm;
import com.chocobean.donation.dto.UserResponse;
import com.chocobean.donation.entity.Payment;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.PaymentRepository;
import com.chocobean.donation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public int getRoleByUserName(String userId) {
        User user= userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));;
        int role = user.getUserRole();

        return role;
    }

}