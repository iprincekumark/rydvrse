package com.rydvrse.common.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ObjectMapperFactory {

    private static final ObjectMapper INSTANCE = new ObjectMapper();

    private ObjectMapperFactory() {
    }

    public static ObjectMapper instance() {
        return INSTANCE;
    }
}
