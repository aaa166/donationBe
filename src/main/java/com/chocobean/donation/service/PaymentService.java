package com.chocobean.donation.service;

import com.chocobean.donation.dto.PayComment;
import com.chocobean.donation.entity.Payment;
import com.chocobean.donation.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

//    public List<PayComment> findPayCommentsByDonationNo(Long no) {
//
//        return payRepository.findPayCommentsByDonationNo(no);
//    }

    @Transactional(readOnly = true)
    public List<PayComment> findPayCommentsByDonationNo(Long no) {
        List<Payment> payments = paymentRepository.findPaymentsWithUserByDonationNo(no);


        return payments.stream()
                .map(payment -> new PayComment(
                        payment.getUser().getUserName(),
                        payment.getPayComment(),
                        payment.getPayAmount(),
                        payment.getPayDate()
                ))
                .collect(Collectors.toList());
    }
}
