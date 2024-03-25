package com.example.demo.service;

import ch.qos.logback.classic.Logger;
import com.example.demo.dto.VacancyDto;
import com.example.demo.entity.VacancyEntity;
import com.example.demo.mapper.VacancyMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VacancyService {

    @Value("${hh.url}")
    private String hhUrl;
    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    private final VacancyMapper mapper = Mappers.getMapper(VacancyMapper.class);
    private final Map<Long, String> employerIdToEmployerUrlCache = new HashMap<>();
    private final VacancyRepository vacancyRepository;
    private HttpRequest request;

    public VacancyService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @Scheduled(cron = "0 0 8,20 * * *")
    public void updateVacancies() {
        Map<Long, VacancyDto> vacancyDtoMap = new HashMap<>();
        Map<Long, String> employerIdToEmployerHHUrlMap = new HashMap<>();

        try (HttpClient client = HttpClient.newHttpClient()) {
            fetchVacancies(vacancyDtoMap, employerIdToEmployerHHUrlMap, client);
            getEmployerUrl(vacancyDtoMap, employerIdToEmployerHHUrlMap, client);

            List<VacancyEntity> entities = mapper.vacancyDtosToVacancies(vacancyDtoMap.values().stream().toList());

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

    private boolean checkVacancyByName(String name) {
        List<String> names = List.of("водитель", "менеджер");
        return names.stream().anyMatch(name::contains);
    }

    private void fetchVacancies(Map<Long, VacancyDto> vacancyDtoMap,
                                Map<Long, String> employerIdToEmployerHHUrlMap, HttpClient client) throws IOException, InterruptedException {
        JSONArray jsonVacancies = getAllVacancies(client);

        for (int j = 0; j < jsonVacancies.length(); j++) {
            VacancyDto vacancyDto = new VacancyDto();
            JSONObject vacancyObject = jsonVacancies.getJSONObject(j);

            if (checkVacancyByName(vacancyObject.getString("name").toLowerCase())) {
                vacancyDto.setId(vacancyObject.getLong("id"));
                vacancyDto.setVacancyName(vacancyObject.getString("name"));
                vacancyDto.setVacancyUrl(vacancyObject.getString("alternate_url"));

                JSONObject employerObject = vacancyObject.getJSONObject("employer");
                if (!employerObject.has("id")) {
                    continue;
                }
                vacancyDto.setEmployerId(employerObject.getLong("id"));
                vacancyDto.setEmployerName(employerObject.getString("name"));

                JSONObject salaryObject = vacancyObject.optJSONObject("salary", new JSONObject());
                if (!salaryObject.isEmpty()) {
                    vacancyDto.setSalaryFrom(salaryObject.optInt("from", 0));
                    vacancyDto.setSalaryTo(salaryObject.optInt("to", 0));
                    vacancyDto.setSalaryCurrency(salaryObject.optString("currency", "RUR"));
                }

                vacancyDtoMap.put(vacancyDto.getId(), vacancyDto);
                employerIdToEmployerHHUrlMap.put(employerObject.getLong("id"), employerObject.getString("url"));
            }
        }
    }

    private JSONArray getAllVacancies(HttpClient client) throws IOException, InterruptedException {
        JSONArray jsonVacancies = new JSONArray();

        for (int i = 0; i < 20; i++) {
            request = getHttpRequest(hhUrl + i);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return jsonVacancies;
            }
            jsonVacancies.putAll(new JSONObject(response.body()).getJSONArray("items"));
        }
        return jsonVacancies;
    }

    private void getEmployerUrl(Map<Long, VacancyDto> vacancyDtoMap,
                                Map<Long, String> employerIdToEmployerHHUrlMap, HttpClient client) {
        Map<Long, String> employerIdToEmployerUrlMap =
                getEmployerIdToEmployerUrlCache(employerIdToEmployerHHUrlMap, client);
        vacancyDtoMap.forEach((key, value) ->
                                      value.setEmployerUrl(employerIdToEmployerUrlMap.getOrDefault(value.getEmployerId(), ""))
        );
    }

    private Map<Long, String> getEmployerIdToEmployerUrlCache(Map<Long, String> employerIdToEmployerHHUrlMap,
                                                              HttpClient client) {
        employerIdToEmployerHHUrlMap.forEach((key, value) -> {
            if (!employerIdToEmployerUrlCache.containsKey(key)) {
                request = getHttpRequest(value);
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 200) {
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(response.body());
                    employerIdToEmployerUrlCache.put(key, jsonObject.getString("site_url"));
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return employerIdToEmployerUrlCache;
    }

    private static HttpRequest getHttpRequest(String value) {
        return HttpRequest.newBuilder()
                .GET()
                .headers("User-Agent", "HH-User-Agent")
                .uri(URI.create(value))
                .build();
    }
}
