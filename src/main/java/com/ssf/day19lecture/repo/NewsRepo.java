package com.ssf.day19lecture.repo;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.json.JsonArray;

@Repository
public class NewsRepo {

    @Value("${newsapi.cache.timeout.mins}")
    long timeout;

    @Autowired
    @Qualifier("newsCache")
    private RedisTemplate<String, String> template;

    public void cacheNews(String country, String category, JsonArray news) {
        String key = mkKey(country, category);
        template.opsForValue()
                .set(key, news.toString(), timeout, TimeUnit.MINUTES);
    }

    public Optional<String> getNews(String country, String category) {
        String key = mkKey(country, category);
        String value = template.opsForValue().get(key);
        return Optional.ofNullable(value);

    }

    private String mkKey(String country, String category) {
        // concatenating both country and category to country-category to use as a key
        return "%s-%s".formatted(country, category);
    }

}
