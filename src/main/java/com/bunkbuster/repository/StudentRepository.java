package com.bunkbuster.repository;

import com.bunkbuster.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByBarcode(String barcode);
    List<Student> findByClassName(String className);
    boolean existsByBarcode(String barcode);
}
