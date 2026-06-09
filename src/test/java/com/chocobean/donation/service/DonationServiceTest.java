package com.chocobean.donation.service;

import com.chocobean.donation.dto.DonationList;
import com.chocobean.donation.dto.DonationState;
import com.chocobean.donation.dto.DonationView;
import com.chocobean.donation.dto.InsertDonation;
import com.chocobean.donation.entity.Donation;
import com.chocobean.donation.entity.DonationCategory;
import com.chocobean.donation.repository.CategoryRepository;
import com.chocobean.donation.repository.DonationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DonationService 단위 테스트")
class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DonationService donationService;

    private Donation createDummyDonation(Long donationNo, String title, Long goalAmount, Long currentAmount) {
        Donation donation = new Donation();
        donation.setDonationNo(donationNo);
        donation.setDonationTitle(title);
        donation.setDonationContent("기부 상세 내용");
        donation.setDonationOrganization("초코빈 기부 재단");
        donation.setDonationTarget("어린이");
        donation.setDonationTargetCount(100);
        donation.setDonationGoalAmount(goalAmount);
        donation.setDonationCurrentAmount(currentAmount);
        donation.setDonationPlan("집행 계획");
        donation.setDonationCreateDate(LocalDate.now());
        donation.setDonationDeadlineDate(LocalDate.now().plusDays(30));
        donation.setDonationImg("test_image.png");
        donation.setDonationState("A");
        return donation;
    }

    @Nested
    @DisplayName("addDonationAmount 메서드는")
    class AddDonationAmount {

        @Test
        @DisplayName("기부글 ID와 추가할 결제 금액을 받아서 기존 모금액에 누적 합산한다")
        void success_addDonationAmount() {
            // Given
            Long donationNo = 1L;
            Long payAmount = 50000L;
            Donation mockDonation = createDummyDonation(donationNo, "테스트 기부", 1000000L, 500000L);

            given(donationRepository.getDonationByDonationNo(donationNo)).willReturn(mockDonation);

            // When
            donationService.addDonationAmount(donationNo, payAmount);

            // Then
            assertThat(mockDonation.getDonationCurrentAmount()).isEqualTo(550000L);
            verify(donationRepository, times(1)).getDonationByDonationNo(donationNo);
        }
    }

    @Nested
    @DisplayName("getDonations 메서드는")
    class GetDonations {

        @Test
        @DisplayName("카테고리 ID가 0일 때, 모든 기부 목록을 반환한다")
        void success_getDonations_all() {
            // Given
            int categoryId = 0;
            Donation donation1 = createDummyDonation(1L, "기부글 1", 1000000L, 200000L);
            Donation donation2 = createDummyDonation(2L, "기부글 2", 2000000L, 400000L);
            given(donationRepository.findAll()).willReturn(List.of(donation1, donation2));

            // When
            List<DonationList> result = donationService.getDonations(categoryId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("기부글 1");
            assertThat(result.get(0).getProgress()).isEqualTo(20); // 200,000 / 1,000,000 * 100
            assertThat(result.get(1).getTitle()).isEqualTo("기부글 2");
            assertThat(result.get(1).getProgress()).isEqualTo(20); // 400,000 / 2,000,000 * 100
            verify(donationRepository, times(1)).findAll();
            verify(donationRepository, never()).findDonationsByCategoryId(anyInt());
        }

        @Test
        @DisplayName("카테고리 ID가 0이 아닐 때, 해당 카테고리의 기부 목록만 조회하여 반환한다")
        void success_getDonations_byCategory() {
            // Given
            int categoryId = 2;
            Donation donation = createDummyDonation(1L, "특정 카테고리 기부글", 1000000L, 300000L);
            given(donationRepository.findDonationsByCategoryId(categoryId)).willReturn(List.of(donation));

            // When
            List<DonationList> result = donationService.getDonations(categoryId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("특정 카테고리 기부글");
            assertThat(result.get(0).getProgress()).isEqualTo(30);
            verify(donationRepository, never()).findAll();
            verify(donationRepository, times(1)).findDonationsByCategoryId(categoryId);
        }
    }

    @Nested
    @DisplayName("getDonationsOrderByDonationDeadlineDateAsc 메서드는")
    class GetDonationsOrderByDonationDeadlineDateAsc {

        @Test
        @DisplayName("마감일 기준 오름차순으로 정렬된 기부 목록을 반환한다")
        void success_getDonationsOrderByDonationDeadlineDateAsc() {
            // Given
            Donation donation1 = createDummyDonation(1L, "임박 기부글 1", 1000000L, 100000L);
            Donation donation2 = createDummyDonation(2L, "임박 기부글 2", 1000000L, 200000L);
            given(donationRepository.findAllByOrderByDonationDeadlineDateAsc()).willReturn(List.of(donation1, donation2));

            // When
            List<DonationList> result = donationService.getDonationsOrderByDonationDeadlineDateAsc();

            // Then
            assertThat(result).hasSize(2);
            verify(donationRepository, times(1)).findAllByOrderByDonationDeadlineDateAsc();
        }
    }

    @Nested
    @DisplayName("getDonationByNo 메서드는")
    class GetDonationByNo {

        @Test
        @DisplayName("존재하는 ID로 조회 시 기부 상세 DTO(DonationView)를 반환한다")
        void success_getDonationByNo() {
            // Given
            Long donationNo = 1L;
            Donation donation = createDummyDonation(donationNo, "기부 상세조회", 5000000L, 1000000L);
            given(donationRepository.findById(donationNo)).willReturn(Optional.of(donation));

            // When
            DonationView result = donationService.getDonationByNo(donationNo);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDonationTitle()).isEqualTo("기부 상세조회");
            assertThat(result.getDonationGoalAmount()).isEqualTo(5000000L);
            verify(donationRepository, times(1)).findById(donationNo);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 EntityNotFoundException 예외를 던진다")
        void fail_getDonationByNo_notFound() {
            // Given
            Long nonExistentNo = 999L;
            given(donationRepository.findById(nonExistentNo)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> donationService.getDonationByNo(nonExistentNo))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("해당 기부를 찾을 수 없습니다. No: " + nonExistentNo);
            verify(donationRepository, times(1)).findById(nonExistentNo);
        }
    }

    @Nested
    @DisplayName("insertDonation 메서드는")
    class InsertDonationTest {

        @Test
        @DisplayName("기부글 등록 DTO와 카테고리 이름을 입력받아 엔티티를 생성하고 저장한다")
        void success_insertDonation() {
            // Given
            InsertDonation dto = new InsertDonation();
            dto.setDonationTitle("신규 기부글");
            dto.setDonationContent("신규 기부글 상세");
            dto.setDonationOrganization("어린이 재단");
            dto.setDonationTarget("어린이");
            dto.setDonationTargetCount(50);
            dto.setDonationGoalAmount(2000000L);
            dto.setDonationPlan("집행 계획서");
            dto.setDonationImg("new_img.png");
            dto.setDonationDeadlineDate(LocalDate.now().plusDays(20));

            List<String> categoryNames = List.of("교육", "아동");
            DonationCategory category1 = new DonationCategory();
            category1.setCategoryId(1);
            category1.setCategoryName("교육");

            DonationCategory category2 = new DonationCategory();
            category2.setCategoryId(2);
            category2.setCategoryName("아동");

            given(categoryRepository.findByCategoryNameIn(categoryNames)).willReturn(List.of(category1, category2));

            // When
            donationService.insertDonation(dto, categoryNames);

            // Then
            verify(categoryRepository, times(1)).findByCategoryNameIn(categoryNames);
            verify(donationRepository, times(1)).save(any(Donation.class));
        }
    }

    @Nested
    @DisplayName("getDonationState 메서드는")
    class GetDonationState {

        @Test
        @DisplayName("모든 기부글을 조회하여 기부 상태 목록 DTO(DonationState)로 변환해 반환한다")
        void success_getDonationState() {
            // Given
            Donation donation1 = createDummyDonation(1L, "기부 1", 1000000L, 500000L);
            Donation donation2 = createDummyDonation(2L, "기부 2", 2000000L, 1000000L);
            given(donationRepository.findAllByOrderByDonationNoAsc()).willReturn(List.of(donation1, donation2));

            // When
            List<DonationState> result = donationService.getDonationState();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDonationNo()).isEqualTo(1L);
            assertThat(result.get(1).getDonationNo()).isEqualTo(2L);
            verify(donationRepository, times(1)).findAllByOrderByDonationNoAsc();
        }
    }

    @Nested
    @DisplayName("updateDonationState 메서드는")
    class UpdateDonationState {

        @Test
        @DisplayName("isVisible 파라미터가 true일 때 기부 상태를 비활성화('D')로 업데이트한다")
        void success_updateDonationState_visibleTrue() {
            // Given
            Long donationNo = 1L;
            boolean isVisible = true;

            // When
            donationService.updateDonationState(donationNo, isVisible);

            // Then
            verify(donationRepository, times(1)).updateDonationStateByNo(donationNo, "D");
        }

        @Test
        @DisplayName("isVisible 파라미터가 false일 때 기부 상태를 활성화('A')로 업데이트한다")
        void success_updateDonationState_visibleFalse() {
            // Given
            Long donationNo = 1L;
            boolean isVisible = false;

            // When
            donationService.updateDonationState(donationNo, isVisible);

            // Then
            verify(donationRepository, times(1)).updateDonationStateByNo(donationNo, "A");
        }
    }

    @Nested
    @DisplayName("getDonationStateByNo 메서드는")
    class GetDonationStateByNo {

        @Test
        @DisplayName("기부 번호로 조회한 기부글의 상태가 'A'(게시 중)이면 true를 반환한다")
        void success_returnTrue_whenStateIsA() {
            // Given
            Long donationNo = 1L;
            given(donationRepository.getDonationStateByDonationNo(donationNo)).willReturn("A");

            // When
            boolean result = donationService.getDonationStateByNo(donationNo);

            // Then
            assertThat(result).isTrue();
            verify(donationRepository, times(1)).getDonationStateByDonationNo(donationNo);
        }

        @Test
        @DisplayName("기부 번호로 조회한 기부글의 상태가 'A'가 아니면 false를 반환한다")
        void success_returnFalse_whenStateIsNotA() {
            // Given
            Long donationNo = 1L;
            given(donationRepository.getDonationStateByDonationNo(donationNo)).willReturn("D");

            // When
            boolean result = donationService.getDonationStateByNo(donationNo);

            // Then
            assertThat(result).isFalse();
            verify(donationRepository, times(1)).getDonationStateByDonationNo(donationNo);
        }
    }
}
