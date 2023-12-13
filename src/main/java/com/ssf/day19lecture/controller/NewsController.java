package com.ssf.day19lecture.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ssf.day19lecture.Utils;
import com.ssf.day19lecture.model.News;
import com.ssf.day19lecture.service.NewsService;

@Controller
@RequestMapping
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping(path = { "/", "/index.html" })
    public ModelAndView getIndex() {
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("categories", Utils.CATEGORY);
        mav.addObject("codes", newsService.getCountryCode());
        return mav;
    }

    @GetMapping("/news")
    public ModelAndView getNewsData(@RequestParam MultiValueMap<String, String> queryParams) {
        ModelAndView mav = new ModelAndView("news");
        String country = queryParams.getFirst("country");
        String category = queryParams.getFirst("category");

        List<News> news = newsService.getNews(country, category);

        mav.addObject("country", country);
        mav.addObject("category", category);
        mav.addObject("news", news);

        return mav;
    }

}