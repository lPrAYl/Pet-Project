package com.example.demo.mapper;

import com.example.demo.dto.VacancyDto;
import com.example.demo.entity.VacancyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VacancyMapper {

    VacancyMapper INSTANCE = Mappers.getMapper(VacancyMapper.class);

    @Mapping(target = "id", source = "vacancyId")
    VacancyDto vacancyToVacancyDto(VacancyEntity vacancyEntity);

    @Mapping(target = "vacancyId", source = "id")
    VacancyEntity vacancyDtoToVacancy(VacancyDto vacancyDto);

    List<VacancyDto> vacanciesToVacancyDtos(List<VacancyEntity> vacancyEntities);

    List<VacancyEntity> vacancyDtosToVacancies(List<VacancyDto> vacancyDtos);
}
