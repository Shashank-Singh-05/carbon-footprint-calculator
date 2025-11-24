package com.carbon.recommender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendationEngine {

    /**
     * Generates simple suggestion texts based on monthly totals.
     * monthlyTotals keys: energy, transport, food
     * natAvg: national average kg/month
     */
    public List<String> generateText(Map<String, Double> monthlyTotals, double natAvg) {
        List<String> out = new ArrayList<>();
        double transport = monthlyTotals.getOrDefault("transport", 0.0);
        double energy = monthlyTotals.getOrDefault("energy", 0.0);
        double food = monthlyTotals.getOrDefault("food", 0.0);
        double total = transport + energy + food;

        if (transport > 150) {
            double est = (transport - 150) * 0.1;
            out.add(String.format("High transport emissions. Replace short car trips with biking/walking or public transport. Est. saving: %.1f kg/month", est));
        }
        if (energy > 100) {
            double est = (energy - 100) * 0.05;
            out.add(String.format("High electricity usage. Use LEDs/unplug devices. Est. saving: %.1f kg/month", est));
        }
        if (food > 120) {
            out.add("Consider reducing meat meals (e.g., 1 less meat meal/week) to reduce food emissions.");
        }
        if (out.isEmpty()) {
            out.add(String.format("Your monthly footprint (%.1f kg) looks reasonable. National average: %.0f kg/month", total, natAvg));
        }
        return out;
    }
}
