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
public class MonthlySeriesDto {
    private List<Integer> monthly; // size 12, index 0 -> January
}


