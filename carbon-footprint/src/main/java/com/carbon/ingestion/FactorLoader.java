package com.carbon.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Map;

public class FactorLoader {
    private Map<String,Object> factors;

    public void load(Path path) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        factors = mapper.readValue(path.toFile(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
    }

    public Map<String,Object> getFactors() { return factors; }
}
