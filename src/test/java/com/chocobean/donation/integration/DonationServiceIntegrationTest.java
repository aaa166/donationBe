package com.chocobean.donation.integration;

import com.chocobean.donation.dto.DonationList;
import com.chocobean.donation.dto.DonationView;
import com.chocobean.donation.dto.InsertDonation;
import com.chocobean.donation.entity.Donation;
import com.chocobean.donation.entity.DonationCategory;
import com.chocobean.donation.repository.CategoryRepository;
import com.chocobean.donation.repository.DonationRepository;
import com.chocobean.donation.service.DonationService;
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
@DisplayName("DonationService 통합 테스트")
class DonationServiceIntegrationTest {

    @Autowired
    private DonationService donationService;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void 기부글_등록_및_조회_통합검증() {
        // Given: 카테고리 데이터 준비
        DonationCategory category = new DonationCategory();
        category.setCategoryName("교육");
        categoryRepository.save(category);

        InsertDonation dto = new InsertDonation();
        dto.setDonationTitle("통합 테스트 기부글");
        dto.setDonationContent("내용입니다.");
        dto.setDonationOrganization("초코빈 재단");
        dto.setDonationTarget("청소년");
        dto.setDonationTargetCount(100);
        dto.setDonationGoalAmount(5000000L);
        dto.setDonationPlan("집행 계획서");
        dto.setDonationImg("test.png");
        dto.setDonationDeadlineDate(LocalDate.now().plusDays(30));

        // When: 기부글 등록
        donationService.insertDonation(dto, List.of("교육"));

        // Then: DB에서 등록된 기부글 확인
        List<DonationList> allDonations = donationService.getDonations(0);
        assertThat(allDonations).isNotEmpty();
        
        DonationList savedDonation = allDonations.stream()
                .filter(d -> d.getTitle().equals("통합 테스트 기부글"))
                .findFirst()
                .orElseThrow();
                
        // 상세 조회 검증
        DonationView view = donationService.getDonationByNo(savedDonation.getDonationNo());
        assertThat(view.getDonationGoalAmount()).isEqualTo(5000000L);
    }

    @Test
    void 기부금액_누적_통합검증() {
        // Given: 기부글 하나 직접 저장
        Donation donation = new Donation();
        donation.setDonationTitle("기부금액 추가 테스트");
        donation.setDonationContent("...");
        donation.setDonationGoalAmount(100000L);
        donation.setDonationCurrentAmount(10000L);
        donation.setDonationState("A");
        Donation saved = donationRepository.save(donation);

        // When: 금액 누적
        donationService.addDonationAmount(saved.getDonationNo(), 30000L);

        // Then: DB 누적 확인
        Donation updated = donationRepository.findById(saved.getDonationNo()).orElseThrow();
        assertThat(updated.getDonationCurrentAmount()).isEqualTo(40000L);
    }

    @Test
    void 기부글_상태변경_통합검증() {
        // Given: 기부글 저장
        Donation donation = new Donation();
        donation.setDonationTitle("상태변경 테스트");
        donation.setDonationState("A");
        Donation saved = donationRepository.save(donation);

        // When: 비활성화 처리 (isVisible=true)
        donationService.updateDonationState(saved.getDonationNo(), true);

        // Then: 'D' 로 변경되었는지 확인
        Donation updated = donationRepository.findById(saved.getDonationNo()).orElseThrow();
        assertThat(updated.getDonationState()).isEqualTo("D");
    }
}
