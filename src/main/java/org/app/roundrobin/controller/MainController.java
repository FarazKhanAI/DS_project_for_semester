package org.app.roundrobin.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.app.roundrobin.algorithm.RoundRobinScheduler;
import org.app.roundrobin.model.Process;

import org.app.roundrobin.model.Metrics;
import org.app.roundrobin.utils.Constants;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private TextField processIdField;
    @FXML private TextField arrivalTimeField;
    @FXML private TextField burstTimeField;
    @FXML private TextField quantumField;

    @FXML private TableView<Process> processTable;
    @FXML private TableColumn<Process, String> pidColumn;
    @FXML private TableColumn<Process, Integer> arrivalColumn;
    @FXML private TableColumn<Process, Integer> burstColumn;
    @FXML private TableColumn<Process, Integer> completionColumn;
    @FXML private TableColumn<Process, Integer> turnaroundColumn;
    @FXML private TableColumn<Process, Integer> waitingColumn;

    @FXML private VBox ganttChartContainer;

    @FXML private Label avgWaitingTimeLabel;
    @FXML private Label avgTurnaroundTimeLabel;
    @FXML private Label cpuUtilizationLabel;
    @FXML private Label throughputLabel;
    @FXML private Label totalProcessesLabel;
    @FXML private Label totalTimeLabel;

    private ObservableList<Process> processes;
    private GanttChartController ganttChartController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTable();
        initializeInputs();
        processes = FXCollections.observableArrayList();
        processTable.setItems(processes);

        // Initialize Gantt chart
        ganttChartController = new GanttChartController();
        ganttChartContainer.getChildren().add(ganttChartController.createGanttChartNode());
    }

    private void initializeTable() {
        pidColumn.setCellValueFactory(new PropertyValueFactory<>("processId"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        burstColumn.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        completionColumn.setCellValueFactory(new PropertyValueFactory<>("completionTime"));
        turnaroundColumn.setCellValueFactory(new PropertyValueFactory<>("turnaroundTime"));
        waitingColumn.setCellValueFactory(new PropertyValueFactory<>("waitingTime"));

        // Set cell factories for better formatting
        setIntegerCellFactory(arrivalColumn);
        setIntegerCellFactory(burstColumn);
        setIntegerCellFactory(completionColumn);
        setIntegerCellFactory(turnaroundColumn);
        setIntegerCellFactory(waitingColumn);
    }

    private void setIntegerCellFactory(TableColumn<Process, Integer> column) {
        column.setCellFactory(tc -> new TableCell<Process, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    private void initializeInputs() {
        quantumField.setText(String.valueOf(Constants.DEFAULT_QUANTUM));

        // Set input validation
        setNumericInputFilter(arrivalTimeField);
        setNumericInputFilter(burstTimeField);
        setNumericInputFilter(quantumField);
    }

    private void setNumericInputFilter(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void handleAddProcess() {
        try {
            String processId = processIdField.getText().trim();
            String arrivalTimeStr = arrivalTimeField.getText().trim();
            String burstTimeStr = burstTimeField.getText().trim();

            if (processId.isEmpty() || arrivalTimeStr.isEmpty() || burstTimeStr.isEmpty()) {
                showAlert("Input Error", "Please fill in all process fields.");
                return;
            }

            int arrivalTime = Integer.parseInt(arrivalTimeStr);
            int burstTime = Integer.parseInt(burstTimeStr);

            if (arrivalTime < 0 || burstTime <= 0) {
                showAlert("Input Error", "Arrival time must be >= 0 and Burst time must be > 0.");
                return;
            }

            // Check for duplicate process ID
            boolean duplicateExists = processes.stream()
                    .anyMatch(p -> p.getProcessId().equals(processId));
            if (duplicateExists) {
                showAlert("Input Error", "Process ID must be unique.");
                return;
            }

            Process process = new Process(processId, arrivalTime, burstTime);
            processes.add(process);

            // Clear input fields
            processIdField.clear();
            arrivalTimeField.clear();
            burstTimeField.clear();
            processIdField.requestFocus();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for arrival and burst time.");
        }
    }

    @FXML
    private void handleRunSimulation() {
        if (processes.isEmpty()) {
            showAlert("Simulation Error", "Please add at least one process.");
            return;
        }

        try {
            int quantum = Integer.parseInt(quantumField.getText().trim());
            if (quantum <= 0) {
                showAlert("Input Error", "Quantum must be greater than 0.");
                return;
            }

            // Run Round Robin scheduling
            RoundRobinScheduler.SimulationResult result =
                    RoundRobinScheduler.schedule(processes, quantum);

            // Update table with results
            updateProcessTable(result.getProcesses());

            // Draw Gantt chart
            ganttChartController.drawGanttChart(result.getGanttChart());

            // Update metrics
            updateMetrics(result.getMetrics());

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid quantum value.");
        } catch (Exception e) {
            showAlert("Simulation Error", "An error occurred during simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        processes.clear();
        processIdField.clear();
        arrivalTimeField.clear();
        burstTimeField.clear();
        quantumField.setText(String.valueOf(Constants.DEFAULT_QUANTUM));
        ganttChartController.clearChart();
        clearMetrics();
        processIdField.requestFocus();
    }

    @FXML
    private void handleExportCSV() {
        if (processes.isEmpty()) {
            showAlert("Export Error", "No data to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Results to CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("roundrobin_results.csv");

        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write header
                writer.println("Process ID,Arrival Time,Burst Time,Completion Time,Turnaround Time,Waiting Time");

                // Write data
                for (Process process : processes) {
                    writer.printf("%s,%d,%d,%d,%d,%d%n",
                            process.getProcessId(),
                            process.getArrivalTime(),
                            process.getBurstTime(),
                            process.getCompletionTime(),
                            process.getTurnaroundTime(),
                            process.getWaitingTime());
                }

                showAlert("Export Successful", "Results exported to: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Export Error", "Failed to export results: " + e.getMessage());
            }
        }
    }



    @FXML
    private void handleSaveGanttChart() {
        if (ganttChartController.isChartEmpty()) {
            showAlert("Save Error", "No Gantt chart to save. Please run a simulation first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Gantt Chart as Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );
        fileChooser.setInitialFileName("roundrobin_gantt_chart.png");

        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            boolean success = ganttChartController.saveAsImage(file);
            if (success) {
                showAlert("Save Successful", "Gantt chart saved to: " + file.getAbsolutePath());
            } else {
                showAlert("Save Error", "Failed to save Gantt chart. Please try again.");
            }
        }
    }

    private void updateProcessTable(List<Process> updatedProcesses) {
        processes.clear();
        processes.addAll(updatedProcesses);
    }

    private void updateMetrics(Metrics metrics) {
        avgWaitingTimeLabel.setText(String.format("Avg Waiting Time: %.2f", metrics.getAvgWaitingTime()));
        avgTurnaroundTimeLabel.setText(String.format("Avg Turnaround Time: %.2f", metrics.getAvgTurnaroundTime()));
        cpuUtilizationLabel.setText(String.format("CPU Utilization: %.2f%%", metrics.getCpuUtilization()));
        throughputLabel.setText(String.format("Throughput: %.4f processes/unit time", metrics.getThroughput()));
        totalProcessesLabel.setText(String.format("Total Processes: %d", metrics.getTotalProcesses()));
        totalTimeLabel.setText(String.format("Total Time: %d", metrics.getTotalTime()));
    }

    private void clearMetrics() {
        avgWaitingTimeLabel.setText("Avg Waiting Time: -");
        avgTurnaroundTimeLabel.setText("Avg Turnaround Time: -");
        cpuUtilizationLabel.setText("CPU Utilization: -");
        throughputLabel.setText("Throughput: -");
        totalProcessesLabel.setText("Total Processes: -");
        totalTimeLabel.setText("Total Time: -");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Stage getStage() {
        return (Stage) processTable.getScene().getWindow();
    }

}