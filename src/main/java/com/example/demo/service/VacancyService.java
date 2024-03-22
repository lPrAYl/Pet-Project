package com.example.demo.service;

import ch.qos.logback.classic.Logger;
import com.example.demo.dto.VacancyDto;
import com.example.demo.mapper.VacancyMapper;
import com.example.demo.repository.VacancyRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mapstruct.Mapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VacancyService {
    Logger log = (Logger) LoggerFactory.getLogger(VacancyService.class);
    Map<Long, String> employerIdToEmployerUrlCache = new HashMap<>();
    private final ExcelService excelService;
    private final VacancyRepository vacancyRepository;



    public VacancyService(ExcelService excelService, VacancyRepository vacancyRepository) {
        this.excelService = excelService;
        this.vacancyRepository = vacancyRepository;
    }

//    @Scheduled(cron ="0 0 8,20 * * *")
    public void getAllVacancies() {
//        JSONArray jsonArray = new JSONArray();
        Map<Long, VacancyDto> vacancyDtoMap = new HashMap<>();
        Map<Long, String> employerIdToEmployerHHUrlMap = new HashMap<>();

        try (HttpClient client = HttpClient.newHttpClient()) {
//            fetchVacancies(client, jsonArray);
            String url = "https://api.hh.ru/vacancies/?per_page=20&page=";

            for (int i = 0; i < 1; i++) {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .headers("User-Agent", "HH-User-Agent")
                        .uri(URI.create(url + i))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    return;
                }

                JSONArray jsonArray = new JSONObject(response.body()).getJSONArray("items");

                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject vacancyObject = jsonArray.getJSONObject(j);
                    VacancyDto vacancyDto = new VacancyDto();

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

            getEmployerUrl(employerIdToEmployerHHUrlMap, vacancyDtoMap, client);

            vacancyDtoMap.values().forEach(vacancyDto ->
                    vacancyRepository.saveVacancy(VacancyMapper.INSTANCE.vacancyDtoToVacancy(vacancyDto)));
            excelService.generateExcelFile();
            log.info("Спарсили очередную пачку вакансий");
        } catch (IOException |
                 InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkVacancyByName(String name) {
        List<String> names = List.of("водитель", "менеджер");
        return names.stream().anyMatch(name::contains);
    }

//    private void fetchVacancies(HttpClient client, JSONArray jsonArray) throws IOException, InterruptedException {
//        String url = "https://api.hh.ru/vacancies/?per_page=100&page=";
//
//        for (int i = 0; i < 20; i++) {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .GET()
//                    .uri(URI.create(url + i))
//                    .build();
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            jsonArray.putAll(new JSONObject(response.body()).getJSONArray("items"));
//        }
//    }

    private void getEmployerUrl(Map<Long, String> mapEmployerIdToEmployerHHUrl,
                                Map<Long, VacancyDto> vacancyDtoMap,
                                HttpClient client) {
        Long start = System.currentTimeMillis();

        Map<Long, String> employerIdToEmployerUrlMap = getEmployerIdToEmployerUrlCache(mapEmployerIdToEmployerHHUrl, client);

        System.out.println(System.currentTimeMillis() - start);

        vacancyDtoMap.forEach((key, value) ->
                value.setEmployerUrl(employerIdToEmployerUrlMap.getOrDefault(value.getEmployerId(), ""))
        );
        System.out.println();
    }


    private Map<Long, String> getEmployerIdToEmployerUrlCache(Map<Long, String> employerIdToEmployerHHUrlMap, HttpClient client) {

        employerIdToEmployerHHUrlMap.forEach((key, value) -> {
            if (!employerIdToEmployerUrlCache.containsKey(key)) {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .headers("User-Agent", "HH-User-Agent")
                        .uri(URI.create(value))
                        .build();
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
}
