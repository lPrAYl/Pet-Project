package com.example.demo.repository;

import com.example.demo.entity.VacancyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyRepository extends JpaRepository<VacancyEntity, Long> {

    List<VacancyEntity> findAllByIsSentIsFalse();

    @Modifying
    @Query(value = "insert into vacancy (\n" +
            "is_sent, salary_from, salary_to, employer_id, vacancy_id, employer_name,\n" +
            "employer_url, vacancy_name, salary_currency, vacancy_url)\n" +
            "values (false, :#{#vacancy.salaryFrom}, :#{#vacancy.salaryTo}, :#{#vacancy.employerId}," +
            ":#{#vacancy.vacancyId}, :#{#vacancy.employerName}, :#{#vacancy.employerUrl}, :#{#vacancy.name}" +
            ":#{#vacancy.salaryCurrency}, :#{#vacancy.vacancyUrl})\n" +
            "on conflict do nothing", nativeQuery = true)
    void saveVacancy(@Param("vacancy") VacancyEntity vacancy);
}
