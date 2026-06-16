package com.chocobean.donation.controller;

import com.chocobean.donation.dto.Donate;
import com.chocobean.donation.dto.PayComment;
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
        String userId = userDetails.getUsername();
        Long userNo = userService.getUserNoByUserId(userId);
        if (donate.getPayAmount() == null || donate.getPayAmount() <= 0) {
            return ResponseEntity.badRequest().body("INVALID_PAY_AMOUNT");
        }

        paymentService.processPayment(userNo, donate);

        return ResponseEntity.ok("ok");
    }
}
