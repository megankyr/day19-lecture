package com.ssf.day19lecture.model;

    public record News(String title, String author, String description, String url, String image, String publish){
        // record classes automatically generate getters, setters and constructor
    }