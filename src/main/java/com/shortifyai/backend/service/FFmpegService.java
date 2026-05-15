package com.shortifyai.backend.service;

import java.util.List;

public interface FFmpegService {
    List<String> generateClips(String inputPath, String outputDir, List<Integer> timestamps);
}