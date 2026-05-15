package com.shortifyai.backend.serviceImpl;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SmartTimestampFilterService {

    public List<Double> filter(List<Double> raw) {

        List<Double> result = new ArrayList<>();

        if (raw == null || raw.isEmpty()) return result;

        raw.sort(Double::compareTo);

        double last = -999;

        for (Double t : raw) {

            // RULE 1: ignore too-close timestamps
            if (t - last >= 7) {
                result.add(t);
                last = t;
            }
        }

        // RULE 2: limit max clips
        if (result.size() > 6) {
            return result.subList(0, 6);
        }

        return result;
    }
}