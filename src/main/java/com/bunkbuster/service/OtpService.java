package com.bunkbuster.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    private final Map<Long, String> otpStore = new HashMap<>();
    private final Map<Long, Long> otpExpiry = new HashMap<>();
    private static final long OTP_VALIDITY_MS = 5 * 60 * 1000; // 5 min

    public String generateLogoutOtp(Long studentId) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(studentId, otp);
        otpExpiry.put(studentId, System.currentTimeMillis() + OTP_VALIDITY_MS);
        System.out.println("🔑 OTP for student " + studentId + " = " + otp);
        return otp;
    }

    public boolean verifyOtp(Long studentId, String otp) {
        String saved = otpStore.get(studentId);
        Long expiry = otpExpiry.get(studentId);
        if (saved == null || expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            otpStore.remove(studentId);
            otpExpiry.remove(studentId);
            return false;
        }
        if (saved.equals(otp)) {
            otpStore.remove(studentId);
            otpExpiry.remove(studentId);
            return true;
        }
        return false;
    }
}