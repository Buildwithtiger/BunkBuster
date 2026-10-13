package com.bunkbuster.controller;

import com.bunkbuster.model.Lecture;
import com.bunkbuster.model.Student;
import com.bunkbuster.model.Teacher;
import com.bunkbuster.repository.LectureRepository;
import com.bunkbuster.repository.StudentRepository;
import com.bunkbuster.repository.TeacherRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired private TeacherRepository teacherRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private LectureRepository lectureRepo;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";

        Optional<Teacher> t = teacherRepo.findById(id);
        t.ifPresent(teacher -> model.addAttribute("teacher", teacher));

        model.addAttribute("totalStudents", 50);
        model.addAttribute("presentCount", 45);
        model.addAttribute("absentCount", 5);
        model.addAttribute("girlsPresent", 18);
        model.addAttribute("boysPresent", 27);
        model.addAttribute("todayLectures", 3);
        model.addAttribute("presentPercent", 90);
        model.addAttribute("absentPercent", 10);

        List<Student> students = studentRepo.findAll();
        model.addAttribute("students", students);
        return "teacher/dashboard";
    }

    @GetMapping("/students")
    public String students(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";
        model.addAttribute("students", studentRepo.findAll());
        return "teacher/view-students";
    }

    @GetMapping("/students/add")
    public String addStudentPage(HttpSession session) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";
        return "teacher/add-student";
    }

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
                             HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";

        if (studentRepo.existsByBarcode(barcode)) {
            model.addAttribute("error", "Barcode already exists");
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
        return "redirect:/teacher/students";
    }

    @GetMapping("/students/edit/{id}")
    public String editStudentPage(@PathVariable Long id, HttpSession session, Model model) {
        Long tid = (Long) session.getAttribute("teacherId");
        if (tid == null) return "redirect:/auth/teacher-login";
        studentRepo.findById(id).ifPresent(s -> model.addAttribute("student", s));
        return "teacher/edit-student";
    }

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
        Long tid = (Long) session.getAttribute("teacherId");
        if (tid == null) return "redirect:/auth/teacher-login";
        studentRepo.findById(id).ifPresent(s -> {
            s.setName(name);
            s.setEmail(email);
            s.setPhone(phone);
            s.setParentPhone(parentPhone);
            s.setGender(gender);
            s.setClassName(className);
            s.setRollNumber(rollNumber);
            studentRepo.save(s);
        });
        return "redirect:/teacher/students";
    }

    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, HttpSession session) {
        Long tid = (Long) session.getAttribute("teacherId");
        if (tid == null) return "redirect:/auth/teacher-login";
        studentRepo.deleteById(id);
        return "redirect:/teacher/students";
    }

    @GetMapping("/at-risk")
    public String atRisk(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";
        model.addAttribute("students", studentRepo.findAll());
        return "teacher/at-risk";
    }

    @GetMapping("/reports")
    public String reports(HttpSession session) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";
        return "teacher/reports";
    }

    @GetMapping("/lectures")
    public String lectures(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";
        model.addAttribute("lectures", lectureRepo.findAll());
        return "teacher/set-lecture";
    }

    @PostMapping("/lectures/add")
    public String addLecture(@RequestParam String subject,
                             @RequestParam String className,
                             @RequestParam int durationMinutes,
                             @RequestParam(required = false, defaultValue = "0") Double latitude,
                             @RequestParam(required = false, defaultValue = "0") Double longitude,
                             @RequestParam(required = false, defaultValue = "50") Double geofenceRadius,
                             HttpSession session) {
        Long teacherId = (Long) session.getAttribute("teacherId");
        if (teacherId == null) return "redirect:/auth/teacher-login";

        LocalDateTime now = LocalDateTime.now();

        Lecture l = new Lecture();
        l.setSubject(subject);
        l.setClassName(className);
        l.setTeacherId(teacherId);
        l.setStartTime(now);
        l.setEndTime(now.plusMinutes(durationMinutes));
        l.setLatitude(latitude);
        l.setLongitude(longitude);
        l.setGeofenceRadius(geofenceRadius);
        l.setActive(true);
        lectureRepo.save(l);

        return "redirect:/teacher/lectures";
    }

    @GetMapping("/lectures/delete/{id}")
    public String deleteLecture(@PathVariable Long id, HttpSession session) {
        Long tid = (Long) session.getAttribute("teacherId");
        if (tid == null) return "redirect:/auth/teacher-login";
        lectureRepo.deleteById(id);
        return "redirect:/teacher/lectures";
    }

    @GetMapping("/notices")
    public String notices(HttpSession session) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id == null) return "redirect:/auth/teacher-login";
        return "teacher/post-notice";
    }
}
