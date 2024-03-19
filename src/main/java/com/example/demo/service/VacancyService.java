package com.example.demo.service;

import com.example.demo.dto.VacancyDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class VacancyService {
    Map<Long, String> employerIdToEmployerUrlCache = new HashMap<>();

    private final ExcelService excelService;

    public VacancyService(ExcelService excelService) {
        this.excelService = excelService;
    }

    public void getAllVacancies() {
        Map<Long, VacancyDto> vacancyDtoMap = new HashMap<>();
        Map<Long, String> employerIdToEmployerHHUrlMap = new HashMap<>();

        try (HttpClient client = HttpClient.newHttpClient()) {
            for (int j = 0; j < 1; j++) {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("https://api.hh.ru/vacancies/?per_page=100&page=" + j))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject jsonObject = new JSONObject(response.body());
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject vacancyObject = jsonArray.getJSONObject(i);
                    VacancyDto vacancyDto = new VacancyDto();
                    vacancyDto.setId(vacancyObject.getLong("id"));
                    vacancyDto.setName(vacancyObject.getString("name"));
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

            getEmployerUrl(employerIdToEmployerHHUrlMap, vacancyDtoMap, client);
            excelService.generateExcelFile(vacancyDtoMap);
            System.out.println();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void getEmployerUrl(Map<Long, String> mapEmployerIdToEmployerHHUrl,
                                Map<Long, VacancyDto> vacancyDtoMap,
                                HttpClient client) {
        Long start = System.currentTimeMillis();

        Map<Long, String> employerIdToEmployerUrlMap = getEmployerIdToEmployerUrlMap(mapEmployerIdToEmployerHHUrl, client);

        System.out.println(System.currentTimeMillis() - start);

        vacancyDtoMap.forEach((key, value) ->
                value.setEmployerUrl(employerIdToEmployerUrlMap.getOrDefault(value.getEmployerId(), ""))
        );
        System.out.println();
    }


    private Map<Long, String> getEmployerIdToEmployerUrlMap(Map<Long, String> employerIdToEmployerHHUrlMap, HttpClient client) {

        employerIdToEmployerHHUrlMap.forEach((key, value) -> {
            if (!employerIdToEmployerUrlCache.containsKey(key)) {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(value))
                        .build();
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
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
