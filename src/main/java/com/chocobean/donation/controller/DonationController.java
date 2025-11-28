package com.chocobean.donation.controller;

import com.chocobean.donation.dto.DonationList;
import com.chocobean.donation.dto.DonationView;
import com.chocobean.donation.service.DonationService;
import com.chocobean.donation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DonationController {

    private final DonationService donationService;
    private final UserService userService;

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

    @GetMapping("/public/donationApply")
    public ResponseEntity<?> getDonationApply(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userName = userDetails.getUsername();
        int role = userService.getRoleByUserName(userName);
        System.out.println("userName :"+ userName);
        System.out.println("role :"+ role);
        if (role == 1){
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }else{
            return ResponseEntity.ok("ok");
        }
    }


}
