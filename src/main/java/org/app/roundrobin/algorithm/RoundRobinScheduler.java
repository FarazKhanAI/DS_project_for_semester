package org.app.roundrobin.algorithm;

import org.app.roundrobin.model.Process;
import org.app.roundrobin.model.GanttEntry;
import org.app.roundrobin.model.Metrics;

import java.util.*;
import java.util.stream.Collectors;

public class RoundRobinScheduler {

    /**
     * Public API unchanged: schedules the given processes using Round Robin with the given quantum.
     */
    public static SimulationResult schedule(List<Process> processes, int quantum) {
        if (processes == null || processes.isEmpty() || quantum <= 0) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        // Create working copies of processes (so caller objects are not mutated)
        List<Process> workingProcesses = processes.stream()
                .map(p -> new Process(p.getProcessId(), p.getArrivalTime(), p.getBurstTime()))
                .collect(Collectors.toList());

        // Sort processes by arrival time
        workingProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));

        List<GanttEntry> ganttChart = new ArrayList<>();
        CircularProcessQueue readyQueue = new CircularProcessQueue();
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
                // If there's no process ready but there are still processes to arrive -> CPU idle
                if (nextProcessIndex < totalProcesses) {
                    int nextArrivalTime = workingProcesses.get(nextProcessIndex).getArrivalTime();
                    if (nextArrivalTime > currentTime) {
                        ganttChart.add(new GanttEntry("IDLE", currentTime, nextArrivalTime));
                        currentTime = nextArrivalTime;
                    }
                    // Add arriving processes
                    while (nextProcessIndex < totalProcesses &&
                            workingProcesses.get(nextProcessIndex).getArrivalTime() <= currentTime) {
                        readyQueue.add(workingProcesses.get(nextProcessIndex));
                        nextProcessIndex++;
                    }
                    continue;
                } else {
                    // Shouldn't normally happen: no ready processes and no future arrivals,
                    // but break to avoid infinite loop.
                    break;
                }
            }

            Process currentProcess = readyQueue.poll();
            if (currentProcess == null) {
                // Defensive: if poll returned null, continue loop
                continue;
            }

            int executionTime = Math.min(currentProcess.getRemainingTime(), quantum);
            int startTime = currentTime;
            currentTime += executionTime;
            currentProcess.setRemainingTime(currentProcess.getRemainingTime() - executionTime);

            // Record Gantt chart entry
            ganttChart.add(new GanttEntry(currentProcess.getProcessId(), startTime, currentTime));

            // Add newly arrived processes to ready queue (only those that arrived up to currentTime)
            while (nextProcessIndex < totalProcesses &&
                    workingProcesses.get(nextProcessIndex).getArrivalTime() <= currentTime) {
                Process newProcess = workingProcesses.get(nextProcessIndex);
                if (!readyQueue.contains(newProcess) && newProcess.getRemainingTime() > 0) {
                    readyQueue.add(newProcess);
                }
                nextProcessIndex++;
            }

            // Check if current process completed
            if (currentProcess.getRemainingTime() == 0) {
                completedProcesses++;
                currentProcess.setCompletionTime(currentTime);
                currentProcess.setTurnaroundTime(currentProcess.getCompletionTime() - currentProcess.getArrivalTime());
                currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
            } else {
                // Process not completed, add back to ready queue (circular behavior maintained)
                if (!readyQueue.contains(currentProcess)) {
                    readyQueue.add(currentProcess);
                } else {
                    // If it's already somehow in the queue (defensive), rotate queue to maintain order.
                    readyQueue.add(currentProcess);
                }
            }
        }

        // Calculate metrics
        Metrics metrics = calculateMetrics(workingProcesses, ganttChart);

        return new SimulationResult(workingProcesses, ganttChart, metrics);
    }

    /**
     * Calculate performance metrics (keeps same semantics as original).
     */
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

        int totalTime = ganttChart.isEmpty() ? 0 : ganttChart.get(ganttChart.size() - 1).getEndTime();
        double cpuUtilization = totalTime > 0 ? (double) totalBusyTime / totalTime * 100 : 0.0;

        // Calculate throughput
        double throughput = totalTime > 0 ? (double) processes.size() / totalTime : 0.0;

        return new Metrics(avgWaitingTime, avgTurnaroundTime, cpuUtilization,
                throughput, processes.size(), totalTime);
    }

    /**
     * Simulation result inner class - unchanged public API.
     */
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

    // -----------------------------
    // Private helper: Circular linked list queue for Process
    // -----------------------------
    private static class CircularProcessQueue {
        private Node tail; // tail.next == head

        private static class Node {
            Process process;
            Node next;
            Node(Process p) { this.process = p; }
        }

        boolean isEmpty() {
            return tail == null;
        }

        void add(Process p) {
            if (p == null) return;
            Node node = new Node(p);
            if (tail == null) {
                tail = node;
                tail.next = tail;
            } else {
                node.next = tail.next; // node -> head
                tail.next = node;
                tail = node; // new tail
            }
        }

        /**
         * Remove and return head (the process at front), or null if empty.
         */
        Process poll() {
            if (tail == null) return null;
            Node head = tail.next;
            Process p = head.process;
            if (head == tail) { // only one element
                tail = null;
            } else {
                tail.next = head.next; // remove head
            }
            return p;
        }

        /**
         * Return true if a process with same processId exists in queue.
         */
        boolean contains(Process p) {
            if (p == null || tail == null) return false;
            String pid = p.getProcessId();
            Node cur = tail.next;
            do {
                if (cur.process != null && pid != null && pid.equals(cur.process.getProcessId())) {
                    return true;
                }
                cur = cur.next;
            } while (cur != tail.next);
            return false;
        }

        /**
         * Clear queue.
         */
        void clear() {
            tail = null;
        }

        /**
         * Count nodes in queue.
         */
        int size() {
            if (tail == null) return 0;
            int cnt = 0;
            Node cur = tail.next;
            do {
                cnt++;
                cur = cur.next;
            } while (cur != tail.next);
            return cnt;
        }
    }
}
