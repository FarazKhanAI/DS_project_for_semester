package org.app.roundrobin.model;

public class Metrics {
    private final double avgWaitingTime;
    private final double avgTurnaroundTime;
    private final double cpuUtilization;
    private final double throughput;
    private final int totalProcesses;
    private final int totalTime;

    public Metrics(double avgWaitingTime, double avgTurnaroundTime,
                   double cpuUtilization, double throughput,
                   int totalProcesses, int totalTime) {
        this.avgWaitingTime = avgWaitingTime;
        this.avgTurnaroundTime = avgTurnaroundTime;
        this.cpuUtilization = cpuUtilization;
        this.throughput = throughput;
        this.totalProcesses = totalProcesses;
        this.totalTime = totalTime;
    }

    // Getters
    public double getAvgWaitingTime() { return avgWaitingTime; }
    public double getAvgTurnaroundTime() { return avgTurnaroundTime; }
    public double getCpuUtilization() { return cpuUtilization; }
    public double getThroughput() { return throughput; }
    public int getTotalProcesses() { return totalProcesses; }
    public int getTotalTime() { return totalTime; }

    @Override
    public String toString() {
        return String.format(
                "Metrics{AvgWT=%.2f, AvgTAT=%.2f, CPUUtil=%.2f%%, Throughput=%.2f}",
                avgWaitingTime, avgTurnaroundTime, cpuUtilization, throughput);
    }
}