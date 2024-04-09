package com.example.demo.service;

import com.example.demo.entity.EmployerEntity;
import com.example.demo.repository.EmployerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CacheService {

    private final EmployerRepository employerRepository;

    public CacheService(EmployerRepository employerRepository) {
        this.employerRepository = employerRepository;
    }

    @Cacheable(value = "employers")
    public Optional<EmployerEntity> getEmployer(Long employerId) {
        return employerRepository.findById(employerId);
    }

    @CacheEvict(value = "employers", key = "#employer.id")
    public void save(EmployerEntity employer) {
        employerRepository.save(employer);
    }
}
