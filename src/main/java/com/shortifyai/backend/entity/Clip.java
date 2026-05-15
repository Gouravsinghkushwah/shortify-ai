package com.shortifyai.backend.entity;

import lombok.Data;

@Data
public class Clip {

    private String url;       // final accessible link
    private int startTime;
    private int duration;
}