package com.chocobean.donation.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<com.chocobean.donation.entity.User, Long> {

    Optional<com.chocobean.donation.entity.User> findByUserIdAndUserPassword(String userId, String userPassword);

    Optional<com.chocobean.donation.entity.User> findByUserId(String userId);

    boolean existsByUserId(String userId);


}
