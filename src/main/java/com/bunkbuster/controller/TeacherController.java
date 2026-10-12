package com.bunkbuster.controller;

import com.bunkbuster.model.Student;
import com.bunkbuster.model.Teacher;
import com.bunkbuster.repository.StudentRepository;
import com.bunkbuster.repository.TeacherRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired private TeacherRepository teacherRepo;
    @Autowired private StudentRepository studentRepo;

    // ═══════════════════════════════════════════
    // DASHBOARD
    // ═══════════════════════════════════════════
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";

        Optional<Teacher> t = teacherRepo.findById(id);
        t.ifPresent(teacher -> model.addAttribute("teacher", teacher));

        // Live count (placeholder — baad mein real data aayega)
        model.addAttribute("totalStudents", 50);
        model.addAttribute("presentCount", 45);
        model.addAttribute("absentCount", 5);
        model.addAttribute("girlsPresent", 18);
        model.addAttribute("boysPresent", 27);
        model.addAttribute("todayLectures", 3);
        model.addAttribute("presentPercent", 90);
        model.addAttribute("absentPercent", 10);

        // Students list
        List<Student> students = studentRepo.findAll();
        model.addAttribute("students", students);

        return "teacher/dashboard";
    }

    // ═══════════════════════════════════════════
    // VIEW ALL STUDENTS
    // ═══════════════════════════════════════════
    @GetMapping("/students")
    public String students(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";

        List<Student> students = studentRepo.findAll();
        model.addAttribute("students", students);
        return "teacher/view-students";
    }

    // ═══════════════════════════════════════════
    // ADD STUDENT PAGE
    // ═══════════════════════════════════════════
    @GetMapping("/students/add")
    public String addStudentPage(HttpSession session) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";
        return "teacher/add-student";
    }

    // ═══════════════════════════════════════════
    // ADD STUDENT (POST)
    // ═══════════════════════════════════════════
    @PostMapping("/students/add")
    public String addStudent(@RequestParam String barcode,
                             @RequestParam String name,
                             @RequestParam String password,
                             @RequestParam(required = false) String email,
                             @RequestParam(required = false) String phone,
                             @RequestParam(required = false) String parentPhone,
                             @RequestParam(required = false) String gender,
                             @RequestParam(required = false) String className,
                             @RequestParam(required = false) String rollNumber,
                             HttpSession session,
                             Model model) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";

        // Check duplicate barcode
        if (studentRepo.existsByBarcode(barcode)) {
            model.addAttribute("error", "❌ Barcode already exists");
            return "teacher/add-student";
        }

        Student s = new Student();
        s.setBarcode(barcode);
        s.setName(name);
        s.setPassword(password);
        s.setEmail(email);
        s.setPhone(phone);
        s.setParentPhone(parentPhone);
        s.setGender(gender);
        s.setClassName(className);
        s.setRollNumber(rollNumber);
        s.setIsActive(true);

        studentRepo.save(s);

        model.addAttribute("success", "✅ Student added successfully!");
        return "redirect:/teacher/students";
    }

    // ═══════════════════════════════════════════
    // EDIT STUDENT PAGE
    // ═══════════════════════════════════════════
    @GetMapping("/students/edit/{id}")
    public String editStudentPage(@PathVariable Long id, HttpSession session, Model model) {
        Long teacherId = (Long) session.getAttribute("teacherId");
        if (teacherId == null) return "redirect:/auth/teacher-login";

        Optional<Student> s = studentRepo.findById(id);
        s.ifPresent(student -> model.addAttribute("student", student));
        return "teacher/edit-student";
    }

    // ═══════════════════════════════════════════
    // EDIT STUDENT (POST)
    // ═══════════════════════════════════════════
    @PostMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String parentPhone,
                              @RequestParam(required = false) String gender,
                              @RequestParam(required = false) String className,
                              @RequestParam(required = false) String rollNumber,
                              HttpSession session) {
        Long teacherId = (Long) session.getAttribute("teacherId");
        if (teacherId == null) return "redirect:/auth/teacher-login";

        Optional<Student> opt = studentRepo.findById(id);
        if (opt.isPresent()) {
            Student s = opt.get();
            s.setName(name);
            s.setEmail(email);
            s.setPhone(phone);
            s.setParentPhone(parentPhone);
            s.setGender(gender);
            s.setClassName(className);
            s.setRollNumber(rollNumber);
            studentRepo.save(s);
        }
        return "redirect:/teacher/students";
    }

    // ═══════════════════════════════════════════
    // DELETE STUDENT
    // ═══════════════════════════════════════════
    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, HttpSession session) {
        Long teacherId = (Long) session.getAttribute("teacherId");
        if (teacherId == null) return "redirect:/auth/teacher-login";

        studentRepo.deleteById(id);
        return "redirect:/teacher/students";
    }

    // ═══════════════════════════════════════════
    // AT-RISK STUDENTS
    // ═══════════════════════════════════════════
    @GetMapping("/at-risk")
    public String atRisk(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";
        model.addAttribute("students", studentRepo.findAll());
        return "teacher/at-risk";
    }

    // ═══════════════════════════════════════════
    // REPORTS
    // ═══════════════════════════════════════════
    @GetMapping("/reports")
    public String reports(HttpSession session) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";
        return "teacher/reports";
    }

    // ═══════════════════════════════════════════
    // SET LECTURE
    // ═══════════════════════════════════════════
    @GetMapping("/lectures")
    public String lectures(HttpSession session) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";
        return "teacher/set-lecture";
    }

    // ═══════════════════════════════════════════
    // NOTICES
    // ═══════════════════════════════════════════
    @GetMapping("/notices")
    public String notices(HttpSession session) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";
        return "teacher/post-notice";
    }
}
