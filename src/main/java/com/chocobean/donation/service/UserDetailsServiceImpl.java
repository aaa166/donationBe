package com.chocobean.donation.service;

import com.chocobean.donation.repository.UserRepository;
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

        com.chocobean.donation.entity.User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    // 👇👇👇 만약 이 에러가 발생하면, DB에 해당 유저가 없는 것입니다.
                    System.out.println("에러: DB에서 " + userId + " 사용자를 찾을 수 없습니다.");
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
                });

        String role = user.getUserRole() == 0 ? "ROLE_ADMIN" : "ROLE_USER";

        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                user.getUserPassword(),
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}