package com.bunkbuster.controller;

import com.bunkbuster.model.Attendance;
import com.bunkbuster.model.Lecture;
import com.bunkbuster.model.Student;
import com.bunkbuster.repository.AttendanceRepository;
import com.bunkbuster.repository.LectureRepository;
import com.bunkbuster.repository.StudentRepository;
import com.bunkbuster.util.DistanceCalculator;
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
@RequestMapping("/student")
public class StudentController {

    @Autowired private StudentRepository studentRepo;
    @Autowired private LectureRepository lectureRepo;
    @Autowired private AttendanceRepository attendanceRepo;

    private Student getStudent(HttpSession session) {
        if (session == null) return null;
        Long id = (Long) session.getAttribute("studentId");
        if (id == null) return null;
        return studentRepo.findById(id).orElse(null);
    }

    private Set<Long> getMarkedLectureIds(Long studentId) {
        try {
            List<Attendance> list = attendanceRepo.findByStudentId(studentId);
            if (list == null) return new HashSet<>();
            return list.stream()
                    .filter(a -> a != null && a.getLecture() != null && a.getLecture().getId() != null)
                    .map(a -> a.getLecture().getId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Student student = getStudent(session);
        if (student == null) return "redirect:/auth/student-login";

        model.addAttribute("student", student);

        // Active lectures (null-safe)
        List<Lecture> activeLectures = new ArrayList<>();
        try {
            if (student.getClassName() != null) {
                activeLectures = lectureRepo
                        .findByClassNameAndEndTimeAfterAndActiveTrueOrderByStartTimeAsc(
                                student.getClassName(), LocalDateTime.now());
                if (activeLectures == null) activeLectures = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("lectures", activeLectures);

        // Marked lecture IDs
        Set<Long> markedLectureIds = getMarkedLectureIds(student.getId());
        model.addAttribute("markedLectureIds", markedLectureIds);

        // Top 5 classmates
        List<Student> top5 = new ArrayList<>();
        try {
            if (student.getClassName() != null) {
                List<Student> classmates = studentRepo.findByClassName(student.getClassName());
                if (classmates != null) {
                    top5 = classmates.stream()
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparing(
                                    s -> s.getRollNumber() != null ? s.getRollNumber() : "ZZZ"))
                            .limit(5)
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("topStudents", top5);

        return "student/home";
    }

    // ═══════════════════════════════════════════
    // MARK ATTENDANCE
    // ═══════════════════════════════════════════
    @GetMapping("/mark-attendance")
    public String markAttendancePage(HttpSession session, Model model) {
        Student student = getStudent(session);
        if (student == null) return "redirect:/auth/student-login";
        model.addAttribute("student", student);

        List<Lecture> activeLectures = new ArrayList<>();
        try {
            if (student.getClassName() != null) {
                activeLectures = lectureRepo
                        .findByClassNameAndEndTimeAfterAndActiveTrueOrderByStartTimeAsc(
                                student.getClassName(), LocalDateTime.now());
                if (activeLectures == null) activeLectures = new ArrayList<>();
            }
        } catch (Exception e) { e.printStackTrace(); }
        model.addAttribute("lectures", activeLectures);

        model.addAttribute("markedLectureIds", getMarkedLectureIds(student.getId()));

        return "student/mark-attendance";
    }

    @PostMapping("/mark-attendance")
    public String markAttendance(@RequestParam Long lectureId,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) Double latitude,
                                 @RequestParam(required = false) Double longitude,
                                 @RequestParam(required = false) Double accuracy,
                                 HttpSession session, Model model) {
        Student student = getStudent(session);
        if (student == null) return "redirect:/auth/student-login";

        Optional<Lecture> lectureOpt = lectureRepo.findById(lectureId);
        if (lectureOpt.isEmpty()) return errorPage(model, student, "❌ Lecture nahi mili");
        Lecture lecture = lectureOpt.get();
        LocalDateTime now = LocalDateTime.now();

        if (lecture.getEndTime() != null && now.isAfter(lecture.getEndTime())) {
            return errorPage(model, student, "⏰ Lecture time khatam ho gaya");
        }

        if (attendanceRepo.existsByStudentIdAndLectureId(student.getId(), lectureId)) {
            return errorPage(model, student, "⚠️ Aap already mark kar chuke ho");
        }

        Attendance.Status finalStatus;
        double distance = 0;

        if ("ABSENT".equalsIgnoreCase(status)) {
            finalStatus = Attendance.Status.ABSENT;
        } else {
            if (latitude == null || longitude == null) {
                return errorPage(model, student, "📍 GPS location required");
            }
            if (accuracy != null && accuracy < 5) {
                return errorPage(model, student, "🚫 Location suspicious (fake GPS)");
            }
            if (lecture.getLatitude() != null && lecture.getLongitude() != null
                    && lecture.getLatitude() != 0 && lecture.getLongitude() != 0) {
                distance = DistanceCalculator.calculate(
                        lecture.getLatitude(), lecture.getLongitude(),
                        latitude, longitude);
                double radius = lecture.getGeofenceRadius() != null ? lecture.getGeofenceRadius() : 50.0;
                if (distance > radius) {
                    return errorPage(model, student,
                            String.format("📍 Aap college ke bahar ho (%.0f m door). Max: %.0f m", distance, radius));
                }
            }
            finalStatus = Attendance.Status.PRESENT;
        }

        Attendance a = new Attendance();
        a.setStudent(student);
        a.setLecture(lecture);
        a.setDate(LocalDate.now());
        a.setStatus(finalStatus);
        a.setLatitude(latitude);
        a.setLongitude(longitude);
        a.setDistanceFromClass(distance);
        a.setMarkedAt(now);
        attendanceRepo.save(a);

        model.addAttribute("student", student);
        model.addAttribute("success", finalStatus == Attendance.Status.PRESENT
                ? "✅ Present lag gayi!" : "📝 Absent mark kar diya");
        model.addAttribute("lectureName", lecture.getSubject());
        model.addAttribute("distance", String.format("%.0f", distance));
        model.addAttribute("markedTime", now.toLocalTime().toString().substring(0, 5));
        model.addAttribute("status", finalStatus.toString());
        return "student/mark-success";
    }

    private String errorPage(Model model, Student student, String error) {
        model.addAttribute("student", student);
        model.addAttribute("error", error);
        List<Lecture> activeLectures = new ArrayList<>();
        try {
            if (student.getClassName() != null) {
                activeLectures = lectureRepo
                        .findByClassNameAndEndTimeAfterAndActiveTrueOrderByStartTimeAsc(
                                student.getClassName(), LocalDateTime.now());
                if (activeLectures == null) activeLectures = new ArrayList<>();
            }
        } catch (Exception e) { e.printStackTrace(); }
        model.addAttribute("lectures", activeLectures);
        model.addAttribute("markedLectureIds", getMarkedLectureIds(student.getId()));
        return "student/mark-attendance";
    }

    // ═══════════════════════════════════════════
    // MY ATTENDANCE
    // ═══════════════════════════════════════════
    @GetMapping("/my-attendance")
    public String myAttendance(HttpSession session, Model model) {
        Student student = getStudent(session);
        if (student == null) return "redirect:/auth/student-login";
        model.addAttribute("student", student);

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);

        List<Attendance> monthAttendance = new ArrayList<>();
        try {
            monthAttendance = attendanceRepo
                    .findByStudentIdAndDateBetweenOrderByDateDesc(student.getId(), start, end);
            if (monthAttendance == null) monthAttendance = new ArrayList<>();
        } catch (Exception e) { e.printStackTrace(); }
        model.addAttribute("attendances", monthAttendance);

        long presentCount = monthAttendance.stream()
                .filter(a -> a != null && a.getStatus() == Attendance.Status.PRESENT).count();
        long absentCount = monthAttendance.stream()
                .filter(a -> a != null && a.getStatus() == Attendance.Status.ABSENT).count();
        long total = monthAttendance.size();
        long percent = total > 0 ? (presentCount * 100 / total) : 0;

        Map<LocalDate, String> dayStatus = new LinkedHashMap<>();
        for (Attendance a : monthAttendance) {
            if (a != null && a.getDate() != null && a.getStatus() != null) {
                dayStatus.put(a.getDate(), a.getStatus().toString());
            }
        }
        model.addAttribute("dayStatus", dayStatus);
        model.addAttribute("presentCount", presentCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("totalCount", total);
        model.addAttribute("percent", percent);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        return "student/my-attendance";
    }

    @PostMapping("/upload-photo")
    public String uploadPhoto(@RequestParam("photo") MultipartFile file, HttpSession session) {
        Student s = getStudent(session);
        if (s == null) return "redirect:/auth/student-login";
        try {
            if (file != null && !file.isEmpty()) {
                String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                String dataUrl = "data:" + file.getContentType() + ";base64," + base64;
                s.setProfilePhoto(dataUrl);
                studentRepo.save(s);
            }
        } catch (Exception e) { e.printStackTrace(); }
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
        List<Student> classmates = new ArrayList<>();
        try {
            if (s.getClassName() != null) {
                classmates = studentRepo.findByClassName(s.getClassName());
                if (classmates == null) classmates = new ArrayList<>();
            }
        } catch (Exception e) { e.printStackTrace(); }
        model.addAttribute("students", classmates.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        st -> st.getRollNumber() != null ? st.getRollNumber() : "ZZZ"))
                .collect(Collectors.toList()));
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
        List<Student> classmates = new ArrayList<>();
        try {
            if (s.getClassName() != null) {
                classmates = studentRepo.findByClassName(s.getClassName());
                if (classmates == null) classmates = new ArrayList<>();
            }
        } catch (Exception e) { e.printStackTrace(); }
        model.addAttribute("students", classmates);
        return "student/classmates";
    }
}
