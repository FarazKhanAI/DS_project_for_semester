package org.app.roundrobin.utils;

import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final int DEFAULT_QUANTUM = 2;
    public static final int CANVAS_WIDTH = 1000;
    public static final int CANVAS_HEIGHT = 600; // Increased default height

    // Improved color palette for Gantt chart
    private static final String[] PROCESS_COLORS = {
            "#1abc9c", "#3498db", "#9b59b6", "#e74c3c", "#f39c12",
            "#2ecc71", "#e67e22", "#34495e", "#16a085", "#27ae60",
            "#2980b9", "#8e44ad", "#c0392b", "#d35400", "#7f8c8d"
    };

    private static final Map<String, String> COLOR_MAP = new HashMap<>();

    static {
        // Predefined colors for common process IDs
        COLOR_MAP.put("IDLE", "#95a5a6");
        COLOR_MAP.put("P1", PROCESS_COLORS[0]);
        COLOR_MAP.put("P2", PROCESS_COLORS[1]);
        COLOR_MAP.put("P3", PROCESS_COLORS[2]);
        COLOR_MAP.put("P4", PROCESS_COLORS[3]);
        COLOR_MAP.put("P5", PROCESS_COLORS[4]);
        COLOR_MAP.put("P6", PROCESS_COLORS[5]);
        COLOR_MAP.put("P7", PROCESS_COLORS[6]);
        COLOR_MAP.put("P8", PROCESS_COLORS[7]);
        COLOR_MAP.put("P9", PROCESS_COLORS[8]);
        COLOR_MAP.put("P10", PROCESS_COLORS[9]);
    }

    public static String getProcessColor(String processId) {
        return COLOR_MAP.computeIfAbsent(processId,
                id -> PROCESS_COLORS[Math.abs(id.hashCode()) % PROCESS_COLORS.length]);
    }

    public static Color hexToColor(String hex) {
        return Color.web(hex);
    }
}