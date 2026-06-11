package com.chocobean.donation.integration;

import com.chocobean.donation.dto.Donate;
import com.chocobean.donation.dto.PayComment;
import com.chocobean.donation.entity.Donation;
import com.chocobean.donation.entity.Payment;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.DonationRepository;
import com.chocobean.donation.repository.PaymentRepository;
import com.chocobean.donation.repository.UserRepository;
import com.chocobean.donation.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("PaymentService 통합 테스트")
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Test
    void 결제처리_및_댓글조회_통합검증() {
        // Given: 기부자와 기부글 세팅
        User user = new User();
        user.setUserId("donor123");
        user.setUserName("기부천사");
        user.setTotalAmount(0L);
        User savedUser = userRepository.save(user);

        Donation donation = new Donation();
        donation.setDonationTitle("어려운 이웃 돕기");
        donation.setDonationGoalAmount(1000000L);
        donation.setDonationCurrentAmount(0L);
        Donation savedDonation = donationRepository.save(donation);

        Donate donateDto = new Donate();
        donateDto.setDonationNo(savedDonation.getDonationNo());
        donateDto.setPayAmount(50000L);
        donateDto.setPayComment("작은 정성입니다.");

        // When: 결제 처리 (기부 진행)
        paymentService.processPayment(savedUser.getUserNo(), donateDto);

        // Then: User와 Donation의 금액이 누적 업데이트 되었는지 확인
        User updatedUser = userRepository.findById(savedUser.getUserNo()).orElseThrow();
        assertThat(updatedUser.getTotalAmount()).isEqualTo(50000L);

        Donation updatedDonation = donationRepository.findById(savedDonation.getDonationNo()).orElseThrow();
        assertThat(updatedDonation.getDonationCurrentAmount()).isEqualTo(50000L);

        // Then: 결제 정보(Payment) 저장 확인 및 후원 댓글 조회 검증
        List<PayComment> comments = paymentService.findPayCommentsByDonationNo(savedDonation.getDonationNo());
        assertThat(comments).hasSize(1);
        
        PayComment comment = comments.get(0);
        assertThat(comment.getPayAmount()).isEqualTo(50000L);
        assertThat(comment.getPayComment()).isEqualTo("작은 정성입니다.");
        assertThat(comment.getUserName()).isEqualTo("기부천사");
    }
}
