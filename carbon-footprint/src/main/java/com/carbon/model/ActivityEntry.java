package com.carbon.model;

import java.time.LocalDate;

public class ActivityEntry {
    private long id;
    private LocalDate date;
    private String category;
    private String subtype;
    private double value;
    private String unit;
    private String notes;

    public ActivityEntry() {}

    public ActivityEntry(long id, LocalDate date, String category, String subtype, double value, String unit, String notes) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.subtype = subtype;
        this.value = value;
        this.unit = unit;
        this.notes = notes;
    }

    public long getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }
    public String getSubtype() { return subtype; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public String getNotes() { return notes; }

    public void setId(long id) { this.id = id; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setCategory(String category) { this.category = category; }
    public void setSubtype(String subtype) { this.subtype = subtype; }
    public void setValue(double value) { this.value = value; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setNotes(String notes) { this.notes = notes; }
}
