package com.chocobean.donation.service;

import com.chocobean.donation.dto.DonationList;
import com.chocobean.donation.dto.DonationView;
import com.chocobean.donation.entity.Donation;
import com.chocobean.donation.repository.DonationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationService {
    private final DonationRepository donationRepository;


    @Transactional(readOnly = true)
    public List<DonationList> getDonations(int code) {
        List<Donation> donations;
        if (code == 0) {
            donations = donationRepository.findAll();
        } else {
            donations = donationRepository.findByDonationCode(code);
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
                donation.getDonationDeadlineDate()
        );
    }

    private DonationView toDonationViewDto(Donation donation) {
        return new DonationView(
                donation.getDonationTitle(),
                donation.getDonationContent(),
                donation.getDonationInstitutionCode(),
                donation.getDonationOrganization(),
                donation.getDonationTarget(),
                donation.getDonationTargetPeople(),
                donation.getDonationGoalAmount(),
                donation.getDonationCurrentAmount(),
                donation.getDonationAmountPlan(),
                donation.getDonationCreateDate(),
                donation.getDonationDeadlineDate(),
                donation.getDonationImg()
        );
    }
}