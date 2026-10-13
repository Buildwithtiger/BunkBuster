package com.bunkbuster.controller;

import com.bunkbuster.model.Lecture;
import com.bunkbuster.model.Student;
import com.bunkbuster.repository.LectureRepository;
import com.bunkbuster.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired private StudentRepository studentRepo;
    @Autowired private LectureRepository lectureRepo;

    private Student getStudent(HttpSession session) {
        Long id = (Long) session.getAttribute("studentId");
        if (id == null) return null;
        return studentRepo.findById(id).orElse(null);
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Student student = getStudent(session);
        if (student == null) return "redirect:/auth/student-login";

        model.addAttribute("student", student);

        List<Lecture> activeLectures = lectureRepo
                .findByClassNameAndEndTimeAfterAndActiveTrueOrderByStartTimeAsc(
                        student.getClassName(), LocalDateTime.now());
        model.addAttribute("lectures", activeLectures);

        List<Student> classmates = studentRepo.findByClassName(student.getClassName());
        List<Student> top5 = classmates.stream()
                .sorted(Comparator.comparing(Student::getRollNumber,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("topStudents", top5);

        return "student/home";
    }

    @PostMapping("/upload-photo")
    public String uploadPhoto(@RequestParam("photo") MultipartFile file, HttpSession session) {
        Student s = getStudent(session);
        if (s == null) return "redirect:/auth/student-login";

        try {
            if (!file.isEmpty()) {
                String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                String dataUrl = "data:" + file.getContentType() + ";base64," + base64;
                s.setProfilePhoto(dataUrl);
                studentRepo.save(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/student/home";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Student s = getStudent(session);
        if (s == null) return "redirect:/auth/student-login";
        model.addAttribute("student", s);
        return "student/profile";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(HttpSession session, Model model) {
        Student s = getStudent(session);
        if (s == null) return "redirect:/auth/student-login";
        model.addAttribute("student", s);

        List<Student> classmates = studentRepo.findByClassName(s.getClassName());
        List<Student> sorted = classmates.stream()
                .sorted(Comparator.comparing(Student::getRollNumber,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        model.addAttribute("students", sorted);
        return "student/leaderboard";
    }

    @GetMapping("/subjects")
    public String subjects(HttpSession session, Model model) {
        Student s = getStudent(session);
        if (s == null) return "redirect:/auth/student-login";
        model.addAttribute("student", s);
        return "student/subjects";
    }

    @GetMapping("/notices")
    public String notices(HttpSession session, Model model) {
        Student s = getStudent(session);
        if (s == null) return "redirect:/auth/student-login";
        model.addAttribute("student", s);
        return "student/notices";
    }

    @GetMapping("/classmates")
    public String classmates(HttpSession session, Model model) {
        Student s = getStudent(session);
        if (s == null) return "redirect:/auth/student-login";
        model.addAttribute("student", s);

        List<Student> all = studentRepo.findByClassName(s.getClassName());
        model.addAttribute("students", all);
        return "student/classmates";
    }

    @GetMapping("/mark-attendance")
    public String markAttendance(HttpSession session, Model model) {
        Student s = getStudent(session);
        if (s == null) return "redirect:/auth/student-login";
        model.addAttribute("student", s);
        return "student/mark-attendance";
    }
}
