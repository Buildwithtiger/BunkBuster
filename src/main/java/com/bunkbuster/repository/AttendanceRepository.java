package com.bunkbuster.repository;

import com.bunkbuster.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentId(Long studentId);

    List<Attendance> findByLectureId(Long lectureId);

    List<Attendance> findByStudentIdOrderByDateDesc(Long studentId);

    List<Attendance> findByStudentIdAndDateBetweenOrderByDateDesc(
            Long studentId, LocalDate start, LocalDate end);

    boolean existsByStudentIdAndLectureId(Long studentId, Long lectureId);

    long countByLectureIdAndStatus(Long lectureId, Attendance.Status status);

    long countByDateAndStatus(LocalDate date, Attendance.Status status);

    long countByStudentIdAndStatus(Long studentId, Attendance.Status status);

    List<Attendance> findByLectureIdAndStatus(Long lectureId, Attendance.Status status);

    // Student-wise total present count
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.status = 'PRESENT'")
    long countPresentByStudent(@Param("studentId") Long studentId);

    // Distinct dates for a student
    @Query("SELECT DISTINCT a.date FROM Attendance a WHERE a.student.id = :studentId ORDER BY a.date DESC")
    List<LocalDate> findDistinctDatesByStudent(@Param("studentId") Long studentId);
}
