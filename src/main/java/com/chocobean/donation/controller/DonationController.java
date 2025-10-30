package com.chocobean.donation.controller;

import com.chocobean.donation.dto.DonationList;
import com.chocobean.donation.dto.DonationView;
import com.chocobean.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DonationController {

    private final DonationService donationService;

    @GetMapping("/public/donations")
    public List<DonationList> getDonations(@RequestParam(defaultValue = "0") Integer categoryId) {
        return donationService.getDonations(categoryId);
    }
    @GetMapping("/public/donationsDate")
    public List<DonationList> getDonationsDate() {
        return donationService.getDonationsOrderByDonationDeadlineDateAsc();
    }

    @GetMapping("/public/donationView/{donationNo}")
    public ResponseEntity<DonationView> getDonationViewByNo(
            @PathVariable("donationNo") Long no
    ) {
        DonationView donationData = donationService.getDonationByNo(no);

        return ResponseEntity.ok(donationData);
    }
}
