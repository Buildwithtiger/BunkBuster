package com.bunkbuster.controller;

import com.bunkbuster.model.Attendance;
import com.bunkbuster.model.Lecture;
import com.bunkbuster.model.Student;
import com.bunkbuster.repository.AttendanceRepository;
import com.bunkbuster.repository.LectureRepository;
import com.bunkbuster.repository.StudentRepository;
import com.bunkbuster.repository.TeacherRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired private TeacherRepository teacherRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private LectureRepository lectureRepo;
    @Autowired private AttendanceRepository attendanceRepo;

    private void addTeacher(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("teacherId");
        if (id != null) teacherRepo.findById(id).ifPresent(t -> model.addAttribute("teacher", t));
    }

    private boolean notLoggedIn(HttpSession session) {
        return session.getAttribute("teacherId") == null;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);

        List<Student> students = studentRepo.findAll();
        LocalDate today = LocalDate.now();
        long totalStudents = students.size();
        long girlsCount = students.stream().filter(s -> "F".equalsIgnoreCase(s.getGender())).count();
        long boysCount = students.stream().filter(s -> "M".equalsIgnoreCase(s.getGender())).count();
        long presentToday = attendanceRepo.countByDateAndStatus(today, Attendance.Status.PRESENT);
        long absentToday = totalStudents - presentToday;

        List<Lecture> allLectures = lectureRepo.findAll();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59);
        long todayLectures = allLectures.stream()
                .filter(l -> l.getStartTime() != null
                        && l.getStartTime().isAfter(todayStart)
                        && l.getStartTime().isBefore(todayEnd)).count();
        long activeLectures = allLectures.stream()
                .filter(l -> l.getEndTime() != null
                        && l.getEndTime().isAfter(LocalDateTime.now())
                        && Boolean.TRUE.equals(l.getActive())).count();

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("girlsCount", girlsCount);
        model.addAttribute("boysCount", boysCount);
        model.addAttribute("presentToday", presentToday);
        model.addAttribute("absentToday", absentToday);
        model.addAttribute("todayLectures", todayLectures);
        model.addAttribute("activeLectures", activeLectures);
        model.addAttribute("students", students);
        return "teacher/dashboard";
    }

    // ═══════════════════════════════════════════
    // STUDENTS
    // ═══════════════════════════════════════════
    @GetMapping("/students")
    public String students(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);
        model.addAttribute("students", studentRepo.findAll());
        return "teacher/view-students";
    }

    // ═══════════════════════════════════════════
    // STUDENT DETAIL — Attendance history
    // ═══════════════════════════════════════════
    @GetMapping("/students/detail/{id}")
    public String studentDetail(@PathVariable Long id, HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);

        Optional<Student> opt = studentRepo.findById(id);
        if (opt.isEmpty()) return "redirect:/teacher/students";
        Student s = opt.get();
        model.addAttribute("studentDetail", s);

        // Last 30 days
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        List<Attendance> monthAttendance = attendanceRepo
                .findByStudentIdAndDateBetweenOrderByDateDesc(id, start, end);
        model.addAttribute("attendances", monthAttendance);

        long presentCount = monthAttendance.stream()
                .filter(a -> a.getStatus() == Attendance.Status.PRESENT).count();
        long absentCount = monthAttendance.stream()
                .filter(a -> a.getStatus() == Attendance.Status.ABSENT).count();
        long total = monthAttendance.size();
        long percent = total > 0 ? (presentCount * 100 / total) : 0;

        model.addAttribute("presentCount", presentCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("totalCount", total);
        model.addAttribute("percent", percent);

        return "teacher/student-detail";
    }

    @GetMapping("/students/add")
    public String addStudentPage(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);
        return "teacher/add-student";
    }

    @PostMapping("/students/add")
    public String addStudent(@RequestParam String barcode, @RequestParam String name,
                             @RequestParam String password,
                             @RequestParam(required = false) String email,
                             @RequestParam(required = false) String phone,
                             @RequestParam(required = false) String parentPhone,
                             @RequestParam(required = false) String gender,
                             @RequestParam(required = false) String className,
                             @RequestParam(required = false) String rollNumber,
                             HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);

        if (studentRepo.existsByBarcode(barcode)) {
            model.addAttribute("error", "Barcode already exists");
            return "teacher/add-student";
        }

        Student s = new Student();
        s.setBarcode(barcode); s.setName(name); s.setPassword(password);
        s.setEmail(email); s.setPhone(phone); s.setParentPhone(parentPhone);
        s.setGender(gender); s.setClassName(className); s.setRollNumber(rollNumber);
        s.setIsActive(true);
        studentRepo.save(s);
        return "redirect:/teacher/students";
    }

    @GetMapping("/students/edit/{id}")
    public String editStudentPage(@PathVariable Long id, HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);
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
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        studentRepo.findById(id).ifPresent(s -> {
            s.setName(name); s.setEmail(email); s.setPhone(phone);
            s.setParentPhone(parentPhone); s.setGender(gender);
            s.setClassName(className); s.setRollNumber(rollNumber);
            studentRepo.save(s);
        });
        return "redirect:/teacher/students";
    }

    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, HttpSession session) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        studentRepo.deleteById(id);
        return "redirect:/teacher/students";
    }

    // ═══════════════════════════════════════════
    // TEACHERS LIST (colleagues)
    // ═══════════════════════════════════════════
    @GetMapping("/teachers-list")
    public String teachersList(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);
        model.addAttribute("teachers", teacherRepo.findAll());
        return "teacher/teachers-list";
    }

    // ═══════════════════════════════════════════
    // PROFILE
    // ═══════════════════════════════════════════
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);
        return "teacher/profile";
    }

    @PostMapping("/upload-photo")
    public String uploadPhoto(@RequestParam("photo") MultipartFile file, HttpSession session) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        Long id = (Long) session.getAttribute("teacherId");
        try {
            if (!file.isEmpty()) {
                String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                String dataUrl = "data:" + file.getContentType() + ";base64," + base64;
                teacherRepo.findById(id).ifPresent(t -> {
                    t.setProfilePhoto(dataUrl);
                    teacherRepo.save(t);
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/teacher/profile";
    }

    @GetMapping("/at-risk")
    public String atRisk(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);
        model.addAttribute("students", studentRepo.findAll());
        return "teacher/at-risk";
    }

    @GetMapping("/reports")
    public String reports(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);
        return "teacher/reports";
    }

    // ═══════════════════════════════════════════
    // LECTURES
    // ═══════════════════════════════════════════
    @GetMapping("/lectures")
    public String lectures(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);
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
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        LocalDateTime now = LocalDateTime.now();

        Lecture l = new Lecture();
        l.setSubject(subject);
        l.setClassName(className);
        l.setTeacherId((Long) session.getAttribute("teacherId"));
        l.setStartTime(now);
        l.setEndTime(now.plusMinutes(durationMinutes));
        l.setLatitude(latitude);
        l.setLongitude(longitude);
        l.setGeofenceRadius(geofenceRadius);
        l.setActive(true);
        lectureRepo.save(l);
        return "redirect:/teacher/lectures";
    }

    // ═══════════════════════════════════════════
    // EXPIRE LECTURE — Manually end
    // ═══════════════════════════════════════════
    @GetMapping("/lectures/expire/{id}")
    public String expireLecture(@PathVariable Long id, HttpSession session) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";

        Optional<Lecture> opt = lectureRepo.findById(id);
        if (opt.isPresent()) {
            Lecture l = opt.get();
            LocalDateTime now = LocalDateTime.now();

            // 1. Set endTime = now, so no new check-ins
            if (l.getEndTime() != null && l.getEndTime().isAfter(now)) {
                l.setEndTime(now);
            }
            l.setActive(false);
            lectureRepo.save(l);

            // 2. Auto-absent for students who didn't mark
            List<Student> classStudents = studentRepo.findByClassName(l.getClassName());
            for (Student s : classStudents) {
                if (!attendanceRepo.existsByStudentIdAndLectureId(s.getId(), id)) {
                    Attendance a = new Attendance();
                    a.setStudent(s);
                    a.setLecture(l);
                    a.setDate(LocalDate.now());
                    a.setStatus(Attendance.Status.ABSENT);
                    a.setMarkedAt(now);
                    attendanceRepo.save(a);
                }
            }
        }
        return "redirect:/teacher/lectures";
    }

    @GetMapping("/lectures/delete/{id}")
    public String deleteLecture(@PathVariable Long id, HttpSession session) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        lectureRepo.deleteById(id);
        return "redirect:/teacher/lectures";
    }

    @GetMapping("/notices")
    public String notices(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/auth/teacher-login";
        addTeacher(session, model);
        return "teacher/post-notice";
    }
}
