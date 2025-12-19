package com.chocobean.donation.service;

import com.chocobean.donation.dto.Donate;
import com.chocobean.donation.dto.PayComment;
import com.chocobean.donation.entity.Donation;
import com.chocobean.donation.entity.Payment;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.DonationRepository;
import com.chocobean.donation.repository.PaymentRepository;
import com.chocobean.donation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final DonationRepository donationRepository;

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

    public void donate(Long userNo, Donate donate) {



        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Donation donation = donationRepository.findById(donate.getDonationNo())
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        Payment payment = new Payment();
        payment.setPayAmount(donate.getPayAmount());
        payment.setPayComment(donate.getPayComment());
        payment.setPayDate(LocalDate.now());
        payment.setUser(user);
        payment.setDonation(donation);

        paymentRepository.save(payment);
    }
}
