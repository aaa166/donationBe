package com.chocobean.donation.service;

import com.chocobean.donation.dto.SignUpForm;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignUpService {
    private final UserRepository userRepository;

    public void processSignUp(SignUpForm signUpForm){
        // 1. 필수값확인
        if (!validation(signUpForm)) {
            log.error("필수 X");
            throw new RuntimeException("필수 X");
        }
        //2.아이디검증(첫글자 영어,숫자허용,영어는 소문자만 가능)
        if (!validateId((signUpForm.getUserId()))){
            log.error("아이디검증 X");
            throw new RuntimeException("아이디검증 X");
        }
        //3.아이디 중복확인
        if (!isUniqueId((signUpForm.getUserId()))){
            log.error("중복");
            throw new RuntimeException("아이디 중복");
        }
    }

    public void insertUser(SignUpForm signUpForm) {
        User user = new User();
//        SignUpForm user = new SignUpForm();
        user.setUserName(signUpForm.getUserName());
        user.setUserId(signUpForm.getUserId());
        user.setUserPassword(signUpForm.getUserPassword());
        user.setUserEmail(signUpForm.getUserEmail());
        user.setUserPhone(signUpForm.getUserPhone());
        userRepository.save(user);

    }









    private boolean validation(SignUpForm signUpForm) {
        if (signUpForm.getUserId()==null
                || signUpForm.getUserId().isEmpty()
                || signUpForm.getUserPassword()==null
                || signUpForm.getUserPassword().isEmpty())   return false;

        return true;
    }
    private  boolean validateId(String id){
        String regex = "^[a-z][a-z0-9]{2,}$";
        if (id.matches(regex))  return true;
        else return false;
    }
    private boolean isUniqueId(String id){
        if (userRepository.existsByUserId(id))  return false;
        return true;
    }


}
