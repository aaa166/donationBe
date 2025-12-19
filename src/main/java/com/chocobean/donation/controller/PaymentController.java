package com.chocobean.donation.controller;

import com.chocobean.donation.dto.Donate;
import com.chocobean.donation.dto.PayComment;
import com.chocobean.donation.service.DonationService;
import com.chocobean.donation.service.PaymentService;
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
public class PaymentController {

    private final PaymentService paymentService;
    private final DonationService donationService;
    private final UserService userService;

    @GetMapping("/public/donationComments/{donationNo}")
    public ResponseEntity<List<PayComment>> getPayCommentsByDonationNo(
            @PathVariable("donationNo") Long no
    ) {
        List<PayComment> PayComment = paymentService.findPayCommentsByDonationNo(no);

        return ResponseEntity.ok(PayComment);
    }

    @PostMapping("/donate")
    public ResponseEntity<?> getDonationApply(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Donate donate
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userId = userDetails.getUsername();
        Long userNo = userService.getUserNoByUserId(userId);
        //payment 결제내역 추가
        paymentService.donate(userNo,donate);

        Long amount = donate.getPayAmount();
        //donation 금액 증가
        donationService.addDonationAmount(donate.getDonationNo(), amount);
        //user 기부금액 증가
        userService.addUserAmount(userNo,amount);

        return ResponseEntity.ok("ok");
    }
}
