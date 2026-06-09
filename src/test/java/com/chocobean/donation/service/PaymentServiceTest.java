package com.chocobean.donation.service;

import com.chocobean.donation.dto.Donate;
import com.chocobean.donation.dto.PayComment;
import com.chocobean.donation.entity.Donation;
import com.chocobean.donation.entity.Payment;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.DonationRepository;
import com.chocobean.donation.repository.PaymentRepository;
import com.chocobean.donation.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 단위 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonationRepository donationRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Nested
    @DisplayName("findPayCommentsByDonationNo 메서드는")
    class FindPayCommentsByDonationNo {

        @Test
        @DisplayName("기부글 ID로 조회 시 연관된 후원 댓글 DTO(PayComment) 목록을 정상 반환한다")
        void success_findPayCommentsByDonationNo() {
            // Given
            Long donationNo = 1L;

            User user = new User();
            user.setUserNo(10L);
            user.setUserId("user123");
            user.setUserName("홍길동");

            Payment payment = new Payment();
            payment.setPayNo(100L);
            payment.setPayAmount(30000L);
            payment.setPayComment("힘내세요!");
            payment.setPayDate(LocalDate.now());
            payment.setUser(user);

            given(paymentRepository.findPaymentsWithUserByDonationNo(donationNo)).willReturn(List.of(payment));

            // When
            List<PayComment> result = paymentService.findPayCommentsByDonationNo(donationNo);

            // Then
            assertThat(result).hasSize(1);
            PayComment comment = result.get(0);
            assertThat(comment.getUserNo()).isEqualTo(10L);
            assertThat(comment.getUserId()).isEqualTo("user123");
            assertThat(comment.getUserName()).isEqualTo("홍길동");
            assertThat(comment.getPayComment()).isEqualTo("힘내세요!");
            assertThat(comment.getPayAmount()).isEqualTo(30000L);
            verify(paymentRepository, times(1)).findPaymentsWithUserByDonationNo(donationNo);
        }
    }

    @Nested
    @DisplayName("processPayment 메서드는")
    class ProcessPayment {

        private Donate createDonateDto(Long donationNo, Long payAmount, String comment) {
            Donate donate = new Donate();
            donate.setDonationNo(donationNo);
            donate.setPayAmount(payAmount);
            donate.setPayComment(comment);
            return donate;
        }

        @Test
        @DisplayName("성공: 결제가 정상 처리되면 결제 내역이 저장되고 기부금과 사용자 총기부액이 증가한다")
        void success_processPayment() {
            // Given
            Long userNo = 10L;
            Donate donateDto = createDonateDto(1L, 50000L, "응원합니다");

            User user = new User();
            user.setUserNo(userNo);
            user.setTotalAmount(100000L); // 기존 총 기부액

            Donation donation = new Donation();
            donation.setDonationNo(1L);
            donation.setDonationCurrentAmount(200000L); // 기존 모금액

            given(userRepository.findById(userNo)).willReturn(Optional.of(user));
            given(donationRepository.findById(donateDto.getDonationNo())).willReturn(Optional.of(donation));

            // When
            paymentService.processPayment(userNo, donateDto);

            // Then
            // 1. Payment 저장 검증
            verify(paymentRepository, times(1)).save(any(Payment.class));
            // 2. Donation 현재 모금액 합산 검증 (200,000 + 50,000 = 250,000)
            assertThat(donation.getDonationCurrentAmount()).isEqualTo(250000L);
            // 3. User 총 기부금 누적 검증 (100,000 + 50,000 = 150,000)
            assertThat(user.getTotalAmount()).isEqualTo(150000L);
        }

        @Test
        @DisplayName("실패: 결제 금액이 null이거나 0 이하이면 IllegalArgumentException을 던진다")
        void fail_whenAmountIsZeroOrNegative() {
            // Given
            Long userNo = 10L;
            Donate zeroAmountDonate = createDonateDto(1L, 0L, "0원 결제");
            Donate nullAmountDonate = createDonateDto(1L, null, "null 결제");

            // When & Then
            assertThatThrownBy(() -> paymentService.processPayment(userNo, zeroAmountDonate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Pay amount must be greater than zero");

            assertThatThrownBy(() -> paymentService.processPayment(userNo, nullAmountDonate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Pay amount must be greater than zero");

            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 회원 번호인 경우 RuntimeException(User not found)을 던진다")
        void fail_whenUserNotFound() {
            // Given
            Long nonExistentUserNo = 999L;
            Donate donateDto = createDonateDto(1L, 50000L, "기부");

            given(userRepository.findById(nonExistentUserNo)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentService.processPayment(nonExistentUserNo, donateDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");

            verify(donationRepository, never()).findById(anyLong());
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 기부글 번호인 경우 RuntimeException(Donation not found)을 던진다")
        void fail_whenDonationNotFound() {
            // Given
            Long userNo = 10L;
            Donate donateDto = createDonateDto(999L, 50000L, "기부");

            User user = new User();
            user.setUserNo(userNo);

            given(userRepository.findById(userNo)).willReturn(Optional.of(user));
            given(donationRepository.findById(donateDto.getDonationNo())).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentService.processPayment(userNo, donateDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Donation not found");

            verify(paymentRepository, never()).save(any(Payment.class));
        }
    }
}
