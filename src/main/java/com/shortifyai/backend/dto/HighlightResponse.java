package com.shortifyai.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class HighlightResponse {
    private List<Integer> timestamps;
}