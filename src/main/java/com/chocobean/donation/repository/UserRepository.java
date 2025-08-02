package com.chocobean.donation.repository;


import com.chocobean.donation.entity.User; // User 엔티티 위치에 따라 수정
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserIdAndUserPassword(String userId, String userPassword);

    Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

}
