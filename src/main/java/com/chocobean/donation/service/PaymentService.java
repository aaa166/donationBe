package com.chocobean.donation.service;

import com.chocobean.donation.dto.PayComment;
import com.chocobean.donation.repository.PayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PayRepository payRepository;

    public List<PayComment> findPayCommentsByDonationNo(Long no) {

        return payRepository.findPayCommentsByDonationNo(no);
    }
}
