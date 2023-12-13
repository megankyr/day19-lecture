package com.ssf.day19lecture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Day19LectureApplication {

	public static void main(String[] args) {
		SpringApplication.run(Day19LectureApplication.class, args);

		String newsApiKey = System.getenv("NEWSAPI_KEY");
        System.out.println("NEWSAPI_KEY: " + newsApiKey);
	}

}
