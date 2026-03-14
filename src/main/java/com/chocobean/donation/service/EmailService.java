package com.chocobean.donation.service;

import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String content) {
        log.info("메일전송시작: {}-{}",subject,to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom("kmcslo97@gmail.com");

        try {
            mailSender.send(message);
//            System.out.println("메일 발송 완료: " + to);
        } catch (Exception e) {
//            System.err.println("메일 발송 실패: " + e.getMessage());
            log.error("메일발송실패: {}", e);
        }
    }


}