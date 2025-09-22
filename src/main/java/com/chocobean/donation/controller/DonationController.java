package com.chocobean.donation.controller;

import com.chocobean.donation.dto.DonationList;
import com.chocobean.donation.entity.Donation;
import com.chocobean.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DonationController {

    private final DonationService donationService;

    @GetMapping("/donations")
    public List<DonationList> getDonations(@RequestParam(defaultValue = "0") Integer code) {
        return donationService.getDonations(code);
    }
    @GetMapping("/donationsDate")
    public List<DonationList> getDonationsDate() {
        return donationService.getDonationsOrderByDonationDeadlineDateAsc();
    }
}
