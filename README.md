Carbon Footprint Calculator
A Java + JavaFX desktop application that helps users track daily activities and estimate their monthly carbon emissions. The app supports manual entry, CSV import, monthly dashboards, and personalized recommendations based on energy, transport, and food usage. All data is stored locally using SQLite, and emission factors are fully configurable via JSON.

Features
•	Add activities with date, category, subtype, value, unit, and notes
•	Import activity data from CSV (user-selected or bundled sample)
•	Dashboard with monthly totals, pie chart, and national comparison
•	Recommendation engine providing actionable emission-reduction tips
•	SQLite database for local data storage
•	Fully configurable emission factors via JSON
Technologies
•	Java 17
•	JavaFX
•	Maven
•	SQLite (JDBC)
•	Jackson Databind

How to Run
Navigate to the project folder (where pom.xml is located) and run:
mvn clean compile javafx:run
This compiles the project and launches the JavaFX application.
