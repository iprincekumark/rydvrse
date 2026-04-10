package com.rydvrse.common.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.stereotype.Component;

@Component
public class JsonNodeUtils {

    private final ObjectMapper objectMapper;

    public JsonNodeUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode toJsonNode(Object value) {
        if (value == null) {
            return JsonNodeFactory.instance.objectNode();
        }
        return objectMapper.valueToTree(value);
    }
}
