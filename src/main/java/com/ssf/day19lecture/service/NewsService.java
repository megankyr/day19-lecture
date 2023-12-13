package com.ssf.day19lecture.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.ssf.day19lecture.Utils;
import com.ssf.day19lecture.model.CountryCode;
import com.ssf.day19lecture.model.News;
import com.ssf.day19lecture.repo.NewsRepo;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

@Service
public class NewsService {

    @Value("${newsapi.key}")
    private String apiKey;

    @Autowired
    private NewsRepo newsRepo;

    @Autowired
    @Qualifier("newsCache")
    private RedisTemplate<String, String> template;

    private List<CountryCode> codes = null;

    public List<News> getNews(String country, String category) {

        Optional<String> opt = newsRepo.getNews(country, category);

        if (opt.isPresent()) {
            System.out.println("From cache:");
            String payload = opt.get();
            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonArray articles = reader.readArray();

            return processArticles(articles);
        }

        try {
            String url = UriComponentsBuilder
                    .fromUriString("https://newsapi.org/v2/top-headlines")
                    .queryParam("country", country)
                    .queryParam("category", category)
                    .toUriString();

            RequestEntity<Void> req = RequestEntity.get(url)
                    .header("X-Api-Key", apiKey)
                    .build();

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> resp = template.exchange(req, String.class);

            String payload = resp.getBody();
            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject result = reader.readObject();
            JsonArray articles = result.getJsonArray("articles");

            newsRepo.cacheNews(country, category, articles);

            return processArticles(articles);

        } catch (Exception e) {
            e.printStackTrace();
            return new LinkedList<>();
        }
    }

    private List<News> processArticles(JsonArray articles) {
        List<News> newsList = new ArrayList<>();

        for (JsonValue articleValue : articles) {
            JsonObject articleObject = (JsonObject) articleValue;

            // retrieves the values from the fields

            String author = articleObject.getString("author", "Anonymous");
            String title = articleObject.getString("title");
            String description = articleObject.getString("description", "No description");
            String newsUrl = articleObject.getString("url");
            String image = articleObject.getString("urlToImage",
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/Placeholder_view_vector.svg/310px-Placeholder_view_vector.svg.png");
            String publish = articleObject.getString("publishedAt");

            News news = new News(title, author, description, newsUrl, image, publish);
            newsList.add(news);
        }

        return newsList;

    }

    public List<CountryCode> getCountryCode() {

        if (codes == null) {
            String url = UriComponentsBuilder
                    .fromUriString("https://restcountries.com/v3.1/alpha")
                    .queryParam("codes", Utils.getCodeAsCSV())
                    .toUriString();

            RequestEntity<Void> req = RequestEntity
                    .get(url).build();

            RestTemplate template = new RestTemplate();

            ResponseEntity<String> resp = template.exchange(req, String.class);
            String payload = resp.getBody();

            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonArray arr = reader.readArray();

            List<CountryCode> countryCodes = new ArrayList<>();
            for (JsonValue jsonValue : arr) {
                JsonObject jsonObject = jsonValue.asJsonObject();
                // process data from the array
                String code = jsonObject.getString("cca2").toLowerCase();
                String name = jsonObject.getJsonObject("name").getString("common");
                countryCodes.add(new CountryCode(code, name));

            }

            Collections.sort(countryCodes, Comparator.comparing(CountryCode::name));
            codes = countryCodes;

        }

        return codes;

    }

}
