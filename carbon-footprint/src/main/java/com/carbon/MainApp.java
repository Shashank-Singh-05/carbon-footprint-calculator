package com.carbon;

import com.carbon.calculator.EmissionCalculator;
import com.carbon.ingestion.CSVImporter;
import com.carbon.ingestion.FactorLoader;
import com.carbon.model.ActivityEntry;
import com.carbon.persistence.ActivityDAO;
import com.carbon.persistence.DatabaseManager;
import com.carbon.recommender.RecommendationEngine;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MainApp extends Application {

    private DatabaseManager db;
    private ActivityDAO dao;
    private FactorLoader factorLoader;
    private EmissionCalculator calculator;
    private RecommendationEngine recommender;

    private Label totalLabel = new Label("Monthly total: 0 kg CO₂e");
    private Label compareLabel = new Label("");
    private PieChart pieChart = new PieChart();
    private ListView<String> recList = new ListView<>();

    @Override
    public void start(Stage stage) throws Exception {

        db = new DatabaseManager();
        dao = new ActivityDAO(db.getConn());
        factorLoader = new FactorLoader();
        factorLoader.load(Paths.get("config/emission_factors.json"));
        calculator = new EmissionCalculator(factorLoader.getFactors());
        recommender = new RecommendationEngine();

        stage.setTitle("Carbon Footprint Calculator");

        VBox left = buildAddActivityPane(stage);
        VBox right = buildDashboardPane();

        HBox root = new HBox(12, left, right);
        root.setPadding(new Insets(12));

        stage.setScene(new Scene(root, 900, 520));
        stage.show();

        refreshDashboard();
    }

    private VBox buildAddActivityPane(Stage stage) {

        VBox box = new VBox(10);
        box.setPrefWidth(360);

        Label heading = new Label("Add Activity");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> categoryBox =
                new ComboBox<>(FXCollections.observableArrayList("energy", "transport", "food"));
        categoryBox.getSelectionModel().selectFirst();

        ComboBox<String> subtypeBox = new ComboBox<>();
        updateSubtypeOptions("energy", subtypeBox);
        categoryBox.setOnAction(e -> updateSubtypeOptions(categoryBox.getValue(), subtypeBox));

        TextField valueField = new TextField();
        valueField.setPromptText("numeric value (e.g., 12.5)");

        TextField unitField = new TextField();
        unitField.setPromptText("unit (kWh, km, meal)");

        TextField notesField = new TextField();
        notesField.setPromptText("notes (optional)");

        Button saveBtn = new Button("Save Activity");
        saveBtn.setOnAction(e -> {
            try {
                LocalDate date = datePicker.getValue();
                String cat = categoryBox.getValue();
                String sub = subtypeBox.getValue();
                double val = Double.parseDouble(valueField.getText().trim());
                String unit = unitField.getText().trim().isEmpty()
                        ? guessUnit(cat)
                        : unitField.getText().trim();
                String notes = notesField.getText().trim();

                ActivityEntry entry =
                        new ActivityEntry(0, date, cat, sub, val, unit, notes);

                dao.insert(entry);
                showInfo("Saved", "Activity saved.");
                valueField.clear();
                notesField.clear();

                refreshDashboard();

            } catch (Exception ex) {
                showError("Error saving", ex.getMessage());
            }
        });

        Button importBtn = new Button("Import CSV");
        importBtn.setOnAction(e -> {
            try {
                FileChooser fc = new FileChooser();
                fc.setTitle("Open CSV file");
                fc.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

                File file = fc.showOpenDialog(stage);
                if (file != null) {
                    CSVImporter importer = new CSVImporter(dao);
                    importer.importCSV(file.toPath());
                    showInfo("Imported", "CSV imported successfully.");
                    refreshDashboard();
                }

            } catch (Exception ex) {
                showError("Import failed", ex.getMessage());
            }
        });

        Button importBundled = new Button("Import sample data");
        importBundled.setOnAction(e -> {
            try {
                CSVImporter importer = new CSVImporter(dao);
                importer.importCSV(Paths.get("data/sample_activities.csv"));
                showInfo("Imported", "Sample dataset imported.");
                refreshDashboard();

            } catch (Exception ex) {
                showError("Import failed", ex.getMessage());
            }
        });

        box.getChildren().addAll(
                heading,
                new Label("Date:"), datePicker,
                new Label("Category:"), categoryBox,
                new Label("Subtype:"), subtypeBox,
                new Label("Value:"), valueField,
                new Label("Unit:"), unitField,
                new Label("Notes:"), notesField,
                saveBtn,
                importBtn,
                importBundled
        );

        return box;
    }

    private void updateSubtypeOptions(String category, ComboBox<String> subtype) {

        subtype.getItems().clear();

        switch (category) {
            case "energy":
                subtype.getItems().add("electricity_kwh");
                break;

            case "transport":
                subtype.getItems().addAll(
                        "car_km_petrol", "bus_km", "train_km", "bicycle_km");
                break;

            case "food":
                subtype.getItems().addAll(
                        "meat_meal", "vegetarian_meal", "vegan_meal");
                break;
        }

        subtype.getSelectionModel().selectFirst();
    }

    private String guessUnit(String cat) {
        if (cat.equals("energy")) return "kWh";
        if (cat.equals("transport")) return "km";
        return "meal";
    }

    private VBox buildDashboardPane() {

        VBox box = new VBox(10);
        box.setPrefWidth(520);

        Label h = new Label("Dashboard");
        h.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        totalLabel.setStyle("-fx-font-size: 14px;");
        compareLabel.setStyle("-fx-font-size: 12px;");

        pieChart.setTitle("Category share (this month)");
        pieChart.setLabelsVisible(true);

        Button recBtn = new Button("Get Suggestions");
        recBtn.setOnAction(e -> {
            try {
                Map<String, Double> monthly = computeMonthlyTotals();
                double nat = getNationalAverage();

                List<String> suggestions =
                        recommender.generateText(monthly, nat);

                recList.getItems().setAll(suggestions);

            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Error", "Could not compute suggestions: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(
                h,
                totalLabel,
                compareLabel,
                pieChart,
                new Label("Recommendations:"),
                recList,
                recBtn
        );

        return box;
    }

    private void refreshDashboard() {
        try {
            Map<String, Double> monthly = computeMonthlyTotals();
            double total = monthly.values().stream().mapToDouble(Double::doubleValue).sum();
            totalLabel.setText(String.format("Monthly total: %.2f kg CO₂e", total));

            double nat = getNationalAverage();
            double pct = (total / nat) * 100.0;

            compareLabel.setText(String.format(
                    "Compared to national average: %.1f%% (national = %.0f kg/month)", pct, nat));

            pieChart.getData().clear();
            monthly.forEach((cat, val) -> {
                if (val > 0) pieChart.getData().add(new PieChart.Data(cat, val));
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Map<String, Double> computeMonthlyTotals() throws Exception {

        List<ActivityEntry> all = dao.listAll();
        LocalDate now = LocalDate.now();

        List<ActivityEntry> month = all.stream()
                .filter(a -> a.getDate().getYear() == now.getYear()
                        && a.getDate().getMonthValue() == now.getMonthValue())
                .collect(Collectors.toList());

        Map<String, Double> totals = new HashMap<>();

        for (ActivityEntry e : month) {
            double kg = calculator.calculate(e);
            totals.put(e.getCategory(),
                    totals.getOrDefault(e.getCategory(), 0.0) + kg);
        }

        totals.putIfAbsent("energy", totals.getOrDefault("energy", 0.0));
        totals.putIfAbsent("transport", totals.getOrDefault("transport", 0.0));
        totals.putIfAbsent("food", totals.getOrDefault("food", 0.0));

        return totals;
    }

    private double getNationalAverage() {
        Object nat = factorLoader.getFactors().get("national_average");
        if (nat instanceof Map) {
            Object v = ((Map<?, ?>) nat).get("monthly_kgCO2e_per_person");
            if (v instanceof Number) return ((Number) v).doubleValue();
        }
        return 0.0;
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    @Override
    public void stop() throws Exception {
        if (db != null) db.close();
    }

    public static void main(String[] args) {
        launch();
    }
}
