package com.carbon.calculator;

import com.carbon.model.ActivityEntry;

import java.util.Map;

public class EmissionCalculator {
    private final Map<String,Object> factors;

    public EmissionCalculator(Map<String,Object> factors) {
        this.factors = factors;
    }

    @SuppressWarnings("unchecked")
    public double calculate(ActivityEntry e) {
        if (factors == null) return 0.0;
        Object catObj = factors.get(e.getCategory());
        if (!(catObj instanceof Map)) return 0.0;
        Map<String, Object> catMap = (Map<String, Object>) catObj;
        Object f = catMap.get(e.getSubtype());
        if (f instanceof Number) {
            double factor = ((Number) f).doubleValue();
            return factor * e.getValue();
        }
        // fallback: 0
        return 0.0;
    }
}
