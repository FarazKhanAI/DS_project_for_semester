# Round Robin CPU Scheduling Simulator 🚦

**Version:** 1.0.0

**Author:** Faraz Khan

**Repository:** [https://github.com/FarazKhanAI/DS_project_for_semester.git](https://github.com/FarazKhanAI/DS_project_for_semester.git)

---

## 🧭 Project Summary

A concise JavaFX desktop application that implements the **Round Robin** CPU scheduling algorithm. It provides an FXML-driven GUI to add processes (arrival & burst times), run simulations with a configurable quantum, visualize execution using a dynamic Gantt chart, and export results.

---

## ⚙️ Technologies

* **Language:** Java 21
* **Framework:** JavaFX 21
* **GUI:** FXML + CSS
* **Build Tool:** Maven
* **Architecture:** MVC

---

## ✨ Key Features

* Interactive JavaFX GUI
* Round Robin scheduling with configurable quantum
* Dynamic Gantt chart visualization
* Performance metrics (waiting/turnaround time, CPU utilization, throughput)
* Export: CSV (process data) and PNG (Gantt chart)

---

## 🗂️ Project Structure

```
src/main/java/org/app/roundrobin/
├── MainApp.java
├── controller/
│   ├── MainController.java
│   └── GanttChartController.java
├── model/
│   ├── Process.java
│   ├── GanttEntry.java
│   └── Metrics.java
└── algorithm/
    └── RoundRobinScheduler.java

src/main/resources/org/app/roundrobin/
├── main.fxml
├── main.css
└── icon.png

pom.xml
```

---

## 💻 Installation

**Prerequisites:** Java JDK 21+, Maven 3.6+, Git

```bash
git clone https://github.com/FarazKhanAI/DS_project_for_semester.git
cd DS_project_for_semester
mvn clean compile
mvn package
mvn javafx:run
# OR
java -jar target/RoundRobin-1.0.0.jar
```

---

## ▶️ Usage (brief)

* Add processes with **Process ID**, **Arrival Time**, **Burst Time** → Click **Add Process**
* Set **Quantum** value
* Click **Run Simulation** → View Gantt chart and metrics
* Export results as CSV or save Gantt as PNG

---

## 🔁 Round Robin (overview)

Assigns fixed time quanta to processes in FIFO order; preempted processes rejoin the ready queue with remaining burst time. The simulator records execution segments to render the Gantt chart and compute metrics.

---

## 📊 Performance Metrics

* Average Waiting Time
* Average Turnaround Time
* CPU Utilization
* Throughput
* Total Processes
* Total Simulation Time

---

## 📦 Dependencies

Managed in `pom.xml` (JavaFX 21 modules: controls, fxml, graphics, base).

---

## 📝 Notes for Developers

* UI: `main.fxml` + `main.css` (controllers handle interactions).
* Scheduling logic is isolated in `RoundRobinScheduler.java` for easy testing.

---

<!-- Cleaned and focused README — irrelevant/extra sections removed -->
