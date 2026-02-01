package com.chocobean.donation.service;

import com.chocobean.donation.repository.UserRepository;
import com.chocobean.donation.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    System.out.println("에러: DB에서 " + userId + " 사용자를 찾을 수 없습니다.");
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
                });

        // 🔹 DB 숫자 → Spring Security ROLE 문자열 변환
        String roleStr;
        switch (user.getUserRole()) {
            case 0: roleStr = "ROLE_ADMIN"; break;   // 관리자
            case 1: roleStr = "ROLE_USER"; break;    // 일반
            case 2: roleStr = "ROLE_COMPANY"; break; // 기업
            default: roleStr = "ROLE_USER"; break;
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                user.getUserPassword(),
                List.of(new SimpleGrantedAuthority(roleStr))
        );
    }
}
