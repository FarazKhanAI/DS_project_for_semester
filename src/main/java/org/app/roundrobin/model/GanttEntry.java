package org.app.roundrobin.model;

public class GanttEntry {
    private final String processId;
    private final int startTime;
    private final int endTime;

    public GanttEntry(String processId, int startTime, int endTime) {
        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getProcessId() { return processId; }
    public int getStartTime() { return startTime; }
    public int getEndTime() { return endTime; }
    public int getDuration() { return endTime - startTime; }

    @Override
    public String toString() {
        return String.format("GanttEntry{ID=%s, Start=%d, End=%d, Duration=%d}",
                processId, startTime, endTime, getDuration());
    }
}