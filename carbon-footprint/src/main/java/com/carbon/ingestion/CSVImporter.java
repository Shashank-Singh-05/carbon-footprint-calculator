package com.carbon.ingestion;

import com.carbon.model.ActivityEntry;
import com.carbon.persistence.ActivityDAO;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class CSVImporter {
    private final ActivityDAO dao;

    public CSVImporter(ActivityDAO dao) {
        this.dao = dao;
    }

    public void importCSV(Path csvPath) throws Exception {
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // simple CSV splitting - assumes no commas inside fields
                String[] p = line.split(",");
                // expected: date,category,subtype,value,unit,notes
                LocalDate d = LocalDate.parse(p[0].trim());
                String cat = p[1].trim();
                String sub = p[2].trim();
                double val = Double.parseDouble(p[3].trim());
                String unit = p[4].trim();
                String notes = p.length > 5 ? p[5].trim() : "";
                ActivityEntry e = new ActivityEntry(0, d, cat, sub, val, unit, notes);
                dao.insert(e);
            }
        }
    }
}
