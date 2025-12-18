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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationService {
    private final DonationRepository donationRepository;
    private final CategoryRepository categoryRepository;

    @Transactional()
    public void addAmount(Long donationNo, Long payAmount) {
        Donation donation = donationRepository.getDonationByDonationNo(donationNo);
        donation.setDonationCurrentAmount(donation.getDonationCurrentAmount() + payAmount);
    }


    @Transactional(readOnly = true)
    public List<DonationList> getDonations(int categoryId) {
        List<Donation> donations;
        if (categoryId == 0) {
            donations = donationRepository.findAll();
        } else {
            donations = donationRepository.findDonationsByCategoryId(categoryId);
        }

        // Entity 리스트를 DTO 리스트로 변환하여 반환
        return donations.stream()
                .map(this::toDonationListDto) // 변환 메서드 호출
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<DonationList> getDonationsOrderByDonationDeadlineDateAsc() {
        List<Donation> donations = donationRepository.findAllByOrderByDonationDeadlineDateAsc();
        return donations.stream()
                .map(this::toDonationListDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public DonationView getDonationByNo(Long no) {
        Donation donation = donationRepository.findById(no)
                .orElseThrow(() -> new EntityNotFoundException("해당 기부를 찾을 수 없습니다. No: " + no));

        // Entity를 DTO로 변환하여 반환
        return toDonationViewDto(donation);
    }


    // --- DTO 변환을 위한 private 헬퍼 메서드 ---


    private DonationList toDonationListDto(Donation donation) {
        // 달성률 계산 (0으로 나누는 경우 방지)
        int percentage = 0;
        if (donation.getDonationGoalAmount() != 0 && donation.getDonationGoalAmount() > 0) {
            percentage = (int) (donation.getDonationCurrentAmount() * 100.0 / donation.getDonationGoalAmount());
        }

        return new DonationList(
                donation.getDonationNo(),
                donation.getDonationTitle(),
                donation.getDonationOrganization(),
                percentage,
                donation.getDonationCurrentAmount(),
                donation.getDonationImg(),
                donation.getDonationDeadlineDate().atStartOfDay()
        );
    }

    private DonationView toDonationViewDto(Donation donation) {
        return new DonationView(
                donation.getDonationTitle(),
                donation.getDonationContent(),
                donation.getDonationOrganization(),
                donation.getDonationTarget(),
                donation.getDonationTargetCount(),
                donation.getDonationGoalAmount(),
                donation.getDonationCurrentAmount(),
                donation.getDonationPlan(),
                donation.getDonationCreateDate().atStartOfDay(),
                donation.getDonationDeadlineDate().atStartOfDay(),
                donation.getDonationImg()
        );
    }
    @Transactional
    public void insertDonation(InsertDonation insertDonation, List<String> categoryNames) {

        List<DonationCategory> categoryEntities = categoryRepository.findByCategoryNameIn(categoryNames);

        Donation donation = new Donation();
        donation.setDonationTitle(insertDonation.getDonationTitle());
        donation.setDonationContent(insertDonation.getDonationContent());
        donation.setDonationOrganization(insertDonation.getDonationOrganization());
        donation.setDonationTarget(insertDonation.getDonationTarget());
        donation.setDonationTargetCount(insertDonation.getDonationTargetCount());
        donation.setDonationGoalAmount(insertDonation.getDonationGoalAmount());
        donation.setDonationPlan(insertDonation.getDonationPlan());
        donation.setDonationImg(insertDonation.getDonationImg());
        donation.setDonationState("D");
        donation.setDonationCreateDate(LocalDate.now());
        donation.setDonationDeadlineDate(insertDonation.getDonationDeadlineDate());

        donation.setCategories(categoryEntities);

        donationRepository.save(donation);
    }


    public List<DonationState> getDonationState() {
        List<Donation> donations = donationRepository.findAll();

        return donations.stream()
                .map(this::convertToDonationStateDto) // 각 Entity를 DTO로 변환하는 메서드 호출
                .collect(Collectors.toList());
    }
    private DonationState convertToDonationStateDto(Donation donation) {


        return new DonationState(
                donation.getDonationNo(),
                donation.getDonationTitle(),
                donation.getDonationOrganization(),
                donation.getDonationDeadlineDate(),
                donation.getDonationState()
        );
    }
    @Transactional
    public void updateDonationState(Long no, boolean isVisible) {
        if (isVisible)donationRepository.updateDonationStateByNo(no, "D");
        else donationRepository.updateDonationStateByNo(no, "A");
    }

    public boolean getDonationStateByNo(Long no) {
        return Objects.equals(donationRepository.getDonationStateByDonationNo(no), "A");
    }
}