package com.phenikaa.communicationservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Component
public class PeriodServiceClient {

    private final WebClient webClient;

    public PeriodServiceClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8082")
                .build();
    }

    public static class PeriodTimes {
        public final LocalDateTime startDate;
        public final LocalDateTime endDate;
        public final String status;
		public final String name;

		public PeriodTimes(LocalDateTime startDate, LocalDateTime endDate, String status, String name) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
			this.name = name;
        }
    }

    public Mono<PeriodTimes> getPeriodTimes(Long periodId) {
        return webClient.get()
                .uri("/internal/periods/{periodId}", periodId)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(wrapper -> {
                    Object data = wrapper.get("data");
                    if (data instanceof Map<?,?> dataMap) {
                        return Mono.just(parseTimesFromMap((Map<?,?>) dataMap));
                    }
                    return Mono.empty();
                })
                .onErrorResume(err -> Mono.empty());
    }

	private PeriodTimes parseTimesFromMap(Map<?,?> map) {
        Object startObj = firstNonNull(map.get("startDate"), map.get("start"), map.get("from"));
        Object endObj = firstNonNull(map.get("endDate"), map.get("end"), map.get("to"));
        Object statusObj = firstNonNull(map.get("status"), map.get("state"));
		Object nameObj = firstNonNull(map.get("name"), map.get("periodName"), map.get("title"));

        LocalDateTime start = parseDateTime(startObj);
        LocalDateTime end = parseDateTime(endObj);
        String status = statusObj == null ? null : statusObj.toString();
		String name = nameObj == null ? null : nameObj.toString();
		return new PeriodTimes(start, end, status, name);
    }

    private Object firstNonNull(Object... vals) {
        for (Object v : vals) {
            if (v != null) return v;
        }
        return null;
    }

    private LocalDateTime parseDateTime(Object val) {
        if (val == null) return null;
        String s = val.toString();
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException ignore) {
            try {
                // Try trimming Z or offset if present
                if (s.endsWith("Z")) {
                    return LocalDateTime.parse(s.substring(0, s.length() - 1));
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }
}


