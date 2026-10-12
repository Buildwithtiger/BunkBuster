package com.bunkbuster.controller;

import com.bunkbuster.model.Student;
import com.bunkbuster.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired private StudentRepository studentRepo;

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("studentId");
        if (id == null) return "redirect:/auth/student-login";
        Optional<Student> s = studentRepo.findById(id);
        s.ifPresent(st -> model.addAttribute("student", st));
        return "student/home";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("studentId");
        if (id == null) return "redirect:/auth/student-login";
        studentRepo.findById(id).ifPresent(s -> model.addAttribute("student", s));
        return "student/profile";
    }

    @GetMapping("/leaderboard")
    public String leaderboard() { return "student/leaderboard"; }

    @GetMapping("/subjects")
    public String subjects() { return "student/subjects"; }

    @GetMapping("/notices")
    public String notices() { return "student/notices"; }

    @GetMapping("/classmates")
    public String classmates() { return "student/classmates"; }
}
