package com.gomes.assistant.util;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomes.assistant.dto.ChatResponse;
import com.gomes.assistant.dto.TimeRange;

public class DataUtils {
	
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
	
	public static String convertMapToJsonString(Map<String, Object> map) throws Exception {
		return objectMapper.writeValueAsString(map);
	}

	public static TimeRange extractTimeRange(ChatResponse chatResponse) {
        return new TimeRange(chatResponse.dataInitial(), chatResponse.dataFinal());
    }
	
    public static <T> T parse(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    public static <T> List<T> parseList(String json, TypeReference<List<T>> typeReference) throws JsonProcessingException {
        return objectMapper.readValue(json, typeReference);
    }

    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static String extractField(String json, String fieldName) throws Exception {
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode fieldNode = rootNode.get(fieldName);
        if (fieldNode == null) {
            throw new IllegalArgumentException("Field '" + fieldName + "' not found in JSON");
        }
        return fieldNode.toString();
    }

}
