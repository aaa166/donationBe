package com.chocobean.donation.repository;

import com.chocobean.donation.entity.Provider;
import com.chocobean.donation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

    @Query("SELECT u.userNo FROM User u WHERE u.userId = :userId")
    Long getUserNoByUserId(@Param("userId") String userId);

    User findByUserNo(Long userNo);

    @Modifying
    @Query("UPDATE User u SET u.userState = :state WHERE u.userNo = :userNo")
    void updateUserState(@Param("userNo") Long userNo, @Param("state") String state);

    @Query("SELECT u.userId FROM User u WHERE u.userNo = :userNo")
    String findUserIdByUserNo(@Param("userNo") Long userNo);

    User findByUserName(String userName);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userId = :userId")
    int countByUserId(@Param("userId")String userId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userPhone = :phone")
    int countByUserPhone(@Param("phone")String phone);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userEmail = :email")
    int countByUserEmail(@Param("email")String email);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE User u
                SET u.userId = :id,
                    u.userEmail = :email,
                    u.userPhone = :phone
                WHERE u.userNo = :no
            """)
    void updateUserInfo(
            @Param("no") Long no,
            @Param("id") String id,
            @Param("email") String email,
            @Param("phone") String phone
    );
    //소셜 로그인 조회
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

}