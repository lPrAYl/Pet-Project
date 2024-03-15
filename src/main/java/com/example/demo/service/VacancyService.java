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


    public static void getAllVacancies() {

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("https://api.hh.ru/vacancies"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject jsonObject = new JSONObject(response.body());
            JSONArray jsonArray = jsonObject.getJSONArray("items");

//            jsonArray.iterato

            Map<Long, String> mapEmployerIdToEmployerHHUrl = new HashMap<>();
            Map<Long, VacancyDto> vacancyDtoMap = new HashMap<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject vacancyObject = jsonArray.getJSONObject(i);
                VacancyDto vacancyDto = new VacancyDto();
                vacancyDto.setId(vacancyObject.getLong("id"));
                vacancyDto.setName(vacancyObject.getString("name"));
                JSONObject employerObject = vacancyObject.getJSONObject("employer");
                vacancyDto.setEmployerId(employerObject.getLong("id"));
                vacancyDto.setEmployerName(employerObject.getString("name"));
//                vacancyDto.setEmployerUrl(employerObject.getString("url"));

                vacancyDtoMap.put(vacancyDto.getId(), vacancyDto);
                mapEmployerIdToEmployerHHUrl.put(employerObject.getLong("id"), employerObject.getString("url"));
                System.out.println();
            }

            getEmployerUrl(mapEmployerIdToEmployerHHUrl, vacancyDtoMap);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getEmployerUrl(Map<Long, String> mapEmployerIdToEmployerHHUrl, Map<Long, VacancyDto> vacancyDtoMap) {
        Map<Long, String> mapEmployerIdToEmployerUrl = new HashMap<>();


        mapEmployerIdToEmployerHHUrl.entrySet().stream().map(
                entry -> {
                    try (HttpClient client = HttpClient.newHttpClient()) {
                        HttpRequest request = HttpRequest.newBuilder()
                                .GET()
                                .uri(URI.create(entry.getValue()))
                                .build();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                        JSONObject jsonObject = new JSONObject(response.body());
                        mapEmployerIdToEmployerUrl.put(entry.getKey(), jsonObject.getString("site_url"));

                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


}
