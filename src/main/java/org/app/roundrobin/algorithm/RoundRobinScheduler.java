package org.app.roundrobin.algorithm;

import org.app.roundrobin.model.Process;
import org.app.roundrobin.model.GanttEntry;
import org.app.roundrobin.model.Metrics;

import java.util.*;
import java.util.stream.Collectors;

public class RoundRobinScheduler {

    public static SimulationResult schedule(List<Process> processes, int quantum) {
        if (processes == null || processes.isEmpty() || quantum <= 0) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        // Create working copies of processes
        List<Process> workingProcesses = processes.stream()
                .map(p -> new Process(p.getProcessId(), p.getArrivalTime(), p.getBurstTime()))
                .collect(Collectors.toList());

        // Sort processes by arrival time
        workingProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));

        List<GanttEntry> ganttChart = new ArrayList<>();
        Queue<Process> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int completedProcesses = 0;
        int totalProcesses = workingProcesses.size();

        // Initialize remaining time
        for (Process p : workingProcesses) {
            p.setRemainingTime(p.getBurstTime());
        }

        // Add first arriving processes to ready queue
        int nextProcessIndex = 0;
        while (nextProcessIndex < totalProcesses &&
                workingProcesses.get(nextProcessIndex).getArrivalTime() <= currentTime) {
            readyQueue.add(workingProcesses.get(nextProcessIndex));
            nextProcessIndex++;
        }

        // Main scheduling loop
        while (completedProcesses < totalProcesses) {
            if (readyQueue.isEmpty()) {
                // CPU is idle
                int nextArrivalTime = workingProcesses.get(nextProcessIndex).getArrivalTime();
                ganttChart.add(new GanttEntry("IDLE", currentTime, nextArrivalTime));
                currentTime = nextArrivalTime;

                // Add arriving processes
                while (nextProcessIndex < totalProcesses &&
                        workingProcesses.get(nextProcessIndex).getArrivalTime() <= currentTime) {
                    readyQueue.add(workingProcesses.get(nextProcessIndex));
                    nextProcessIndex++;
                }
                continue;
            }

            Process currentProcess = readyQueue.poll();
            int executionTime = Math.min(currentProcess.getRemainingTime(), quantum);
            int startTime = currentTime;
            currentTime += executionTime;
            currentProcess.setRemainingTime(currentProcess.getRemainingTime() - executionTime);

            // Record Gantt chart entry
            ganttChart.add(new GanttEntry(currentProcess.getProcessId(), startTime, currentTime));

            // Add newly arrived processes to ready queue
            while (nextProcessIndex < totalProcesses &&
                    workingProcesses.get(nextProcessIndex).getArrivalTime() <= currentTime) {
                Process newProcess = workingProcesses.get(nextProcessIndex);
                if (!readyQueue.contains(newProcess) && newProcess.getRemainingTime() > 0) {
                    readyQueue.add(newProcess);
                }
                nextProcessIndex++;
            }

            // Check if process completed
            if (currentProcess.getRemainingTime() == 0) {
                completedProcesses++;
                currentProcess.setCompletionTime(currentTime);
                currentProcess.setTurnaroundTime(currentProcess.getCompletionTime() - currentProcess.getArrivalTime());
                currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
            } else {
                // Process not completed, add back to ready queue
                readyQueue.add(currentProcess);
            }
        }

        // Calculate metrics
        Metrics metrics = calculateMetrics(workingProcesses, ganttChart);

        return new SimulationResult(workingProcesses, ganttChart, metrics);
    }

    private static Metrics calculateMetrics(List<Process> processes, List<GanttEntry> ganttChart) {
        double totalWaitingTime = processes.stream().mapToInt(Process::getWaitingTime).sum();
        double totalTurnaroundTime = processes.stream().mapToInt(Process::getTurnaroundTime).sum();
        double avgWaitingTime = totalWaitingTime / processes.size();
        double avgTurnaroundTime = totalTurnaroundTime / processes.size();

        // Calculate CPU utilization
        int totalBusyTime = ganttChart.stream()
                .filter(entry -> !entry.getProcessId().equals("IDLE"))
                .mapToInt(GanttEntry::getDuration)
                .sum();
        int totalTime = ganttChart.get(ganttChart.size() - 1).getEndTime();
        double cpuUtilization = (double) totalBusyTime / totalTime * 100;

        // Calculate throughput
        double throughput = (double) processes.size() / totalTime;

        return new Metrics(avgWaitingTime, avgTurnaroundTime, cpuUtilization,
                throughput, processes.size(), totalTime);
    }

    public static class SimulationResult {
        private final List<Process> processes;
        private final List<GanttEntry> ganttChart;
        private final Metrics metrics;

        public SimulationResult(List<Process> processes, List<GanttEntry> ganttChart, Metrics metrics) {
            this.processes = processes;
            this.ganttChart = ganttChart;
            this.metrics = metrics;
        }

        public List<Process> getProcesses() { return processes; }
        public List<GanttEntry> getGanttChart() { return ganttChart; }
        public Metrics getMetrics() { return metrics; }
    }
}