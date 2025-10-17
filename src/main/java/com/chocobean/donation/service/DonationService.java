package com.chocobean.donation.service;

import com.chocobean.donation.dto.DonationList;
import com.chocobean.donation.dto.DonationView;
import com.chocobean.donation.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class DonationService {
    private final DonationRepository donationRepository;



    public List<DonationList> getDonations(int code) {
        if (code == 0) {
            return donationRepository.findDonationSummaries(); // 전체 조회
        } else {
            return donationRepository.findDonationSummariesByDonationCode(code);
        }
    }

    public List<DonationList> getDonationsOrderByDonationDeadlineDateAsc() {
        return donationRepository.findAllByOrderByDonationDeadlineDateAsc();
    }

    public DonationView getDonationByNo(Long no) {
        return  donationRepository.findDonationDetailsByDonationNo(no);
    }
}
