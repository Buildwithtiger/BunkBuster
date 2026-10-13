package com.bunkbuster.repository;

import com.bunkbuster.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    List<Lecture> findByClassName(String className);

    List<Lecture> findByTeacherId(Long teacherId);

    List<Lecture> findByEndTimeAfterAndActiveTrueOrderByStartTimeAsc(LocalDateTime now);

    List<Lecture> findByClassNameAndEndTimeAfterAndActiveTrueOrderByStartTimeAsc(
            String className, LocalDateTime now);
}
