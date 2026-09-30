package com.naveena.employeemanagement.repository;

import com.naveena.employeemanagement.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
	
    Page<Employee> findByDepartment(String department, Pageable pageable);

    List<Employee> findByDepartment(String department);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByNameContainingIgnoreCase(String name);

    List<Employee> findBySalaryBetween(Double min, Double max);

    long countByDepartment(String department);

    @Query("SELECT DISTINCT e.department FROM Employee e")
    List<String> findDistinctDepartments();
}