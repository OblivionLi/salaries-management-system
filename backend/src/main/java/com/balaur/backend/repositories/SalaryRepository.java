package com.balaur.backend.repositories;

import com.balaur.backend.models.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Integer> {
    @Query("select s from Salary s where s.id = :id")
    Optional<Salary> findById(@Param("id") Long id);
}
