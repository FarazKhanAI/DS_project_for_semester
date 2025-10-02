package org.app.roundrobin.model;

import javafx.beans.property.*;

public class Process {
    private final StringProperty processId;
    private final IntegerProperty arrivalTime;
    private final IntegerProperty burstTime;
    private final IntegerProperty completionTime;
    private final IntegerProperty turnaroundTime;
    private final IntegerProperty waitingTime;
    private final IntegerProperty remainingTime;

    public Process() {
        this.processId = new SimpleStringProperty("");
        this.arrivalTime = new SimpleIntegerProperty(0);
        this.burstTime = new SimpleIntegerProperty(0);
        this.completionTime = new SimpleIntegerProperty(0);
        this.turnaroundTime = new SimpleIntegerProperty(0);
        this.waitingTime = new SimpleIntegerProperty(0);
        this.remainingTime = new SimpleIntegerProperty(0);
    }

    public Process(String processId, int arrivalTime, int burstTime) {
        this.processId = new SimpleStringProperty(processId);
        this.arrivalTime = new SimpleIntegerProperty(arrivalTime);
        this.burstTime = new SimpleIntegerProperty(burstTime);
        this.completionTime = new SimpleIntegerProperty(0);
        this.turnaroundTime = new SimpleIntegerProperty(0);
        this.waitingTime = new SimpleIntegerProperty(0);
        this.remainingTime = new SimpleIntegerProperty(burstTime);
    }

    // Getters and Setters
    public String getProcessId() { return processId.get(); }
    public void setProcessId(String processId) { this.processId.set(processId); }
    public StringProperty processIdProperty() { return processId; }

    public int getArrivalTime() { return arrivalTime.get(); }
    public void setArrivalTime(int arrivalTime) { this.arrivalTime.set(arrivalTime); }
    public IntegerProperty arrivalTimeProperty() { return arrivalTime; }

    public int getBurstTime() { return burstTime.get(); }
    public void setBurstTime(int burstTime) { this.burstTime.set(burstTime); }
    public IntegerProperty burstTimeProperty() { return burstTime; }

    public int getCompletionTime() { return completionTime.get(); }
    public void setCompletionTime(int completionTime) { this.completionTime.set(completionTime); }
    public IntegerProperty completionTimeProperty() { return completionTime; }

    public int getTurnaroundTime() { return turnaroundTime.get(); }
    public void setTurnaroundTime(int turnaroundTime) { this.turnaroundTime.set(turnaroundTime); }
    public IntegerProperty turnaroundTimeProperty() { return turnaroundTime; }

    public int getWaitingTime() { return waitingTime.get(); }
    public void setWaitingTime(int waitingTime) { this.waitingTime.set(waitingTime); }
    public IntegerProperty waitingTimeProperty() { return waitingTime; }

    public int getRemainingTime() { return remainingTime.get(); }
    public void setRemainingTime(int remainingTime) { this.remainingTime.set(remainingTime); }
    public IntegerProperty remainingTimeProperty() { return remainingTime; }

    @Override
    public String toString() {
        return String.format("Process{ID=%s, Arrival=%d, Burst=%d, Completion=%d, TAT=%d, WT=%d}",
                getProcessId(), getArrivalTime(), getBurstTime(),
                getCompletionTime(), getTurnaroundTime(), getWaitingTime());
    }
}