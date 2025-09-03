package com.phenikaa.communicationservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

@Getter
public final class JsonMapperProvider {
    private final ObjectMapper mapper;

    private JsonMapperProvider() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private static class Holder {
        private static final JsonMapperProvider INSTANCE = new JsonMapperProvider();
    }

    public static JsonMapperProvider getInstance() {
        return Holder.INSTANCE;
    }

}