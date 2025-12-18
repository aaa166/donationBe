package com.chocobean.donation.repository;

import com.chocobean.donation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

    @Query("SELECT u.userNo FROM User u WHERE u.userId = :userId")
    Long getUserNoByUserId(@Param("userId") String userId);
}