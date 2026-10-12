package com.bunkbuster.controller;

import com.bunkbuster.model.Student;
import com.bunkbuster.model.Teacher;
import com.bunkbuster.repository.StudentRepository;
import com.bunkbuster.repository.TeacherRepository;
import com.bunkbuster.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired private StudentRepository studentRepo;
    @Autowired private TeacherRepository teacherRepo;
    @Autowired private OtpService otpService;

    @GetMapping("/student-login")
    public String studentLoginPage() {
        return "auth/student-login";
    }

    @PostMapping("/student-login")
    public String studentLogin(@RequestParam String barcode,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        String b = barcode.trim();
        String p = password.trim();

        System.out.println("═══════════════════════════════════");
        System.out.println("🔍 LOGIN DEBUG");
        System.out.println("   Form barcode  = [" + b + "]");
        System.out.println("   Form password = [" + p + "]");

        Optional<Student> opt = studentRepo.findByBarcode(b);
        if (opt.isEmpty()) {
            System.out.println("   ❌ Barcode DB mein nahi mila");
            model.addAttribute("error", "❌ Barcode nahi mila");
            return "auth/student-login";
        }
        Student s = opt.get();
        System.out.println("   DB password   = [" + s.getPassword() + "]");
        System.out.println("   Match? " + s.getPassword().equals(p));
        System.out.println("═══════════════════════════════════");

        if (!s.getPassword().equals(p)) {
            model.addAttribute("error", "❌ Password galat");
            return "auth/student-login";
        }
        session.setAttribute("studentId", s.getId());
        session.setAttribute("studentName", s.getName());
        return "redirect:/student/home";
    }

    @GetMapping("/teacher-login")
    public String teacherLoginPage() {
        return "auth/teacher-login";
    }

    @PostMapping("/teacher-login")
    public String teacherLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        Optional<Teacher> opt = teacherRepo.findByEmail(email.trim());
        if (opt.isEmpty() || !opt.get().getPassword().equals(password.trim())) {
            model.addAttribute("error", "❌ Email ya password galat");
            return "auth/teacher-login";
        }
        session.setAttribute("teacherId", opt.get().getId());
        session.setAttribute("teacherName", opt.get().getName());
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/logout")
    public String logoutPage(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/auth/student-login";
        String otp = otpService.generateLogoutOtp(studentId);
        model.addAttribute("otp", otp);
        return "auth/logout-otp";
    }

    @PostMapping("/logout/verify")
    public String verifyLogout(@RequestParam String otp,
                               HttpSession session,
                               Model model) {
        Long studentId = (Long) session.getAttribute("studentId");
        boolean ok = otpService.verifyOtp(studentId, otp);
        if (!ok) {
            model.addAttribute("error", "❌ OTP galat ya expire ho gaya");
            return "auth/logout-otp";
        }
        session.invalidate();
        return "redirect:/auth/student-login";
    }
}
