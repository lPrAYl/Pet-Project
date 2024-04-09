package com.example.demo.service;

import ch.qos.logback.classic.Logger;
import com.example.demo.dto.VacancyDto;
import com.example.demo.entity.EmployerEntity;
import com.example.demo.entity.VacancyEntity;
import com.example.demo.mapper.VacancyMapper;
import com.example.demo.repository.EmployerRepository;
import com.example.demo.repository.VacancyRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VacancyService {

    @Value("${hh.url}")
    private String hhUrl;
    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    private HttpRequest request;
    private final VacancyMapper mapper = Mappers.getMapper(VacancyMapper.class);
    private final VacancyRepository vacancyRepository;
    private final CacheService cacheService;

    public VacancyService(VacancyRepository vacancyRepository, CacheService cacheService) {
        this.vacancyRepository = vacancyRepository;
        this.cacheService = cacheService;
    }

    @Scheduled(cron = "0 0 1,9,18 * * *")
    public void updateVacancies() {
        List<VacancyDto> vacancyDtos = new ArrayList<>();

        try (HttpClient client = HttpClient.newHttpClient()) {
            fetchVacancies(vacancyDtos, client);
            fillEmployerUrl(vacancyDtos, client);

            List<VacancyEntity> entities = mapper.vacancyDtosToVacancies(vacancyDtos);
            for (VacancyEntity entity : entities) {
                try {
                    vacancyRepository.saveAndFlush(entity);
                } catch (Exception e) {
                }
            }
            log.info("Спарсили очередную пачку вакансий");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Заполнение списка вакансий с hh.ru
     *
     * @param vacancyDtos список вакансий, полученных с hh.ru
     * @param client      HttpClient
     */
    private void fetchVacancies(List<VacancyDto> vacancyDtos,
                                HttpClient client) throws IOException, InterruptedException {
        JSONArray jsonVacancies = getAllVacanciesJSONArray(client);

        for (int j = 0; j < jsonVacancies.length(); j++) {
            JSONObject vacancyObject = jsonVacancies.getJSONObject(j);
            JSONObject employerObject = vacancyObject.getJSONObject("employer");

            if (employerObject.has("id") &&
                    checkVacancyByName(vacancyObject.getString("name").toLowerCase())) {
                VacancyDto vacancyDto = new VacancyDto();

                vacancyDto.setId(vacancyObject.getLong("id"));
                vacancyDto.setVacancyName(vacancyObject.getString("name"));
                vacancyDto.setVacancyUrl(vacancyObject.getString("alternate_url"));

                vacancyDto.setEmployerId(employerObject.getLong("id"));
                vacancyDto.setEmployerName(employerObject.getString("name"));
                vacancyDto.setEmployerHHUrl(employerObject.getString("url"));

                JSONObject salaryObject = vacancyObject.optJSONObject("salary", new JSONObject());
                if (!salaryObject.isEmpty()) {
                    vacancyDto.setSalaryFrom(salaryObject.optInt("from", 0));
                    vacancyDto.setSalaryTo(salaryObject.optInt("to", 0));
                    vacancyDto.setSalaryCurrency(salaryObject.optString("currency", "RUR"));
                }

                vacancyDtos.add(vacancyDto);
            }
        }
    }

    /**
     * Проверка соответствия названия вакансии заданному фильтру
     *
     * @param name название вакансии
     * @return true/false
     */

    private boolean checkVacancyByName(String name) {
        List<String> names = List.of("специалист по трафику", "интернет-маркетолог", "интернет маркетолог", "таргетолог",
                "контекстолог", "специалист по настройке яндекс директ", "Ведущий PPC-специалист",
                "PPC-специалист", "директолог", "специалист по контекстной рекламе", "digital-маркетолог");

        // todo для тестов
//        List<String> names = List.of("водитель");
        return names.stream().anyMatch(name::contains);
    }

    /**
     * @param client HttpClient
     * @return JSONArray of vacancies from hh.ru
     */
    private JSONArray getAllVacanciesJSONArray(HttpClient client) throws IOException, InterruptedException {
        JSONArray jsonVacancies = new JSONArray();

        for (int i = 0; i < 1; i++) {
            request = getHttpRequest(hhUrl + i);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return jsonVacancies;
            }
            jsonVacancies.putAll(new JSONObject(response.body()).getJSONArray("items"));
        }
        return jsonVacancies;
    }

    private void fillEmployerUrl(List<VacancyDto> vacancyDtos,
                                 HttpClient client) {

        for (VacancyDto vacancyDto : vacancyDtos) {
            Optional<EmployerEntity> optionalEmployer = cacheService.getEmployer(vacancyDto.getEmployerId());

            if (optionalEmployer.isPresent()) {
                vacancyDto.setEmployerUrl(optionalEmployer.get().getUrl());
            } else {
                saveEmployer(vacancyDto, client);
            }
        }
    }

    public void saveEmployer(VacancyDto vacancyDto, HttpClient client) {
        request = getHttpRequest(vacancyDto.getEmployerHHUrl());
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return;
            }
            EmployerEntity employer = new EmployerEntity();
            JSONObject jsonObject = new JSONObject(response.body());
            Long employerId = Long.parseLong(jsonObject.getString("id"));
            String employerUrl = jsonObject.getString("site_url");
            employer.setId(employerId);
            employer.setUrl(employerUrl);
            cacheService.save(employer);
            vacancyDto.setEmployerUrl(employerUrl);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpRequest getHttpRequest(String value) {
        return HttpRequest.newBuilder()
                .GET()
                .headers("User-Agent", "HH-User-Agent")
                .uri(URI.create(value))
                .build();
    }
}
