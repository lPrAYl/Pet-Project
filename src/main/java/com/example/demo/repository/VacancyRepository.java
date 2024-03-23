package com.example.demo.repository;

import com.example.demo.entity.VacancyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyRepository extends JpaRepository<VacancyEntity, Long> {

    List<VacancyEntity> findAllByIsSentIsFalseOrderByIdDesc();
}
