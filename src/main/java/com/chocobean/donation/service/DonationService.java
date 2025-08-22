package com.chocobean.donation.service;

import com.chocobean.donation.dto.DonationList;
import com.chocobean.donation.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class DonationService {
    private final DonationRepository donationRepository;



    public List<DonationList> getDonations() {
        return donationRepository.findDonationSummaries();
    }

    public List<DonationList> getDonationsOrderByDonationDeadlineDateAsc() {
        return donationRepository.findAllByOrderByDonationDeadlineDateAsc();
    }
}
