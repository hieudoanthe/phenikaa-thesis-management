package com.phenikaa.evalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RadarLoadResponse {
    private List<String> labels; // e.g., ["Vai trò 1", ...]
    private List<Double> seriesA; // current
    private List<Double> seriesB; // previous/plan
}
