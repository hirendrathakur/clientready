package utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public enum JSONObjectMapper {
    INSTANCE;

    private ObjectMapper objectMapper;
    private ObjectMapper yamlMapper;

    JSONObjectMapper() {
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ObjectMapper getMapper() {
        return objectMapper;
    }
    public ObjectMapper getYamlMapper() {
        return yamlMapper;
    }
}
