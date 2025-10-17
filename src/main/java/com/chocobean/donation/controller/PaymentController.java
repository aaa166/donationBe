package com.chocobean.donation.controller;

import com.chocobean.donation.dto.PayComment;
import com.chocobean.donation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/public/donationComments/{donationNo}")
    public ResponseEntity<List<PayComment>> getDonationViewByNo(
            @PathVariable("donationNo") Long no
    ) {
        List<PayComment> PayComment = paymentService.findPayCommentsByDonationNo(no);

        return ResponseEntity.ok(PayComment);
    }
}
