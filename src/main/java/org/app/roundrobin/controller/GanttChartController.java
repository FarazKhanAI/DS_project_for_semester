package org.app.roundrobin.controller;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.FontWeight;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import org.app.roundrobin.model.GanttEntry;
import org.app.roundrobin.utils.Constants;

import java.util.List;
import java.util.ArrayList;

public class GanttChartController {

    private Canvas canvas;
    private ScrollPane scrollPane;
    private VBox container;
    private List<GanttEntry> currentGanttEntries;

    private static final int CANVAS_MARGIN = 80;
    private static final int BAR_HEIGHT = 50;
    private static final int BAR_SPACING = 15;
    private static final int TIME_MARK_INTERVAL = 1;
    private static final int TEXT_OFFSET = 20;
    private static final int LEGEND_WIDTH = 200;
    private static final int MAX_CANVAS_WIDTH = 3000;
    private static final int MAX_CANVAS_HEIGHT = 2000;
    private static final int HEADER_HEIGHT = 120; // Increased header height
    private static final int GRAPH_START_OFFSET = 100; // Space between header and graph

    public GanttChartController() {
        initializeComponents();
    }

    private void initializeComponents() {
        canvas = new Canvas(Constants.CANVAS_WIDTH, Constants.CANVAS_HEIGHT);
        canvas.setStyle("-fx-background-color: #ffffff; -fx-border-color: #2c3e50; -fx-border-width: 2; -fx-border-radius: 5;");

        scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportWidth(Constants.CANVAS_WIDTH);
        scrollPane.setStyle("-fx-background: #ecf0f1; -fx-border-color: #bdc3c7;");
        scrollPane.setPrefHeight(300);

        container = new VBox();
        container.getChildren().add(scrollPane);
        container.setStyle("-fx-padding: 10; -fx-background-color: #ecf0f1;");

        currentGanttEntries = new ArrayList<>();
    }

    public Node createGanttChartNode() {
        return container;
    }

    public void drawGanttChart(List<GanttEntry> ganttEntries) {
        this.currentGanttEntries = new ArrayList<>(ganttEntries);

        if (ganttEntries == null || ganttEntries.isEmpty()) {
            clearChart();
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        clearCanvas(gc);

        int totalTime = ganttEntries.get(ganttEntries.size() - 1).getEndTime();
        int canvasWidth = calculateCanvasWidth(totalTime);
        int canvasHeight = calculateCanvasHeight(ganttEntries);

        // Limit canvas size to prevent rendering issues
        canvasWidth = Math.min(canvasWidth, MAX_CANVAS_WIDTH);
        canvasHeight = Math.min(canvasHeight, MAX_CANVAS_HEIGHT);

        canvas.setWidth(canvasWidth);
        canvas.setHeight(canvasHeight);

        drawChart(gc, ganttEntries, canvasWidth, canvasHeight, totalTime);
    }

    private int calculateCanvasWidth(int totalTime) {
        int minWidth = Constants.CANVAS_WIDTH;
        int calculatedWidth = Math.max(minWidth, totalTime * 60 + 2 * CANVAS_MARGIN);
        return calculatedWidth;
    }

    private int calculateCanvasHeight(List<GanttEntry> entries) {
        long uniqueProcesses = entries.stream()
                .map(GanttEntry::getProcessId)
                .distinct()
                .count();

        // Calculate height: Header + Graph offset + Process bars + Timeline space
        int headerSpace = HEADER_HEIGHT;
        int graphOffset = GRAPH_START_OFFSET;
        int processBarsHeight = (int) (uniqueProcesses * (BAR_HEIGHT + BAR_SPACING));
        int timelineSpace = 100; // Space for timeline and labels

        int totalHeight = headerSpace + graphOffset + processBarsHeight + timelineSpace;

        return Math.max(totalHeight, Constants.CANVAS_HEIGHT);
    }

    private void drawChart(GraphicsContext gc, List<GanttEntry> entries, int canvasWidth, int canvasHeight, int totalTime) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);

        // Adjust scale if canvas is too wide
        double availableWidth = Math.min(canvasWidth - 2 * CANVAS_MARGIN - LEGEND_WIDTH, MAX_CANVAS_WIDTH - 2 * CANVAS_MARGIN - LEGEND_WIDTH);
        double scale = availableWidth / (double) totalTime;

        // Draw header at the top with plenty of space
        drawHeader(gc, canvasWidth);

        // Start the graph well below the header
        int graphStartY = HEADER_HEIGHT + GRAPH_START_OFFSET;

        drawTimeline(gc, totalTime, scale, graphStartY, canvasWidth);
        drawGanttBars(gc, entries, scale, graphStartY, canvasWidth, canvasHeight);
        drawTimeMarks(gc, totalTime, scale, graphStartY, canvasWidth);
        drawLegend(gc, entries, canvasWidth, canvasHeight);
        drawAxisLabels(gc, canvasWidth, canvasHeight, graphStartY);
    }

    // Separate method for drawing charts for image saving with better layout
    private void drawChartForImage(GraphicsContext gc, List<GanttEntry> entries, int canvasWidth, int canvasHeight, int totalTime) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18)); // Larger font for image
        gc.setTextAlign(TextAlignment.CENTER);

        // More conservative scaling for image
        double availableWidth = canvasWidth - 2 * CANVAS_MARGIN - LEGEND_WIDTH;
        double scale = availableWidth / (double) totalTime;

        // Draw header with plenty of space at the top
        drawHeaderForImage(gc, canvasWidth);

        // Start the graph well below the header
        int graphStartY = HEADER_HEIGHT + GRAPH_START_OFFSET + 30; // Extra space for images

        drawTimelineForImage(gc, totalTime, scale, graphStartY, canvasWidth);
        drawGanttBarsForImage(gc, entries, scale, graphStartY, canvasWidth, canvasHeight);
        drawTimeMarksForImage(gc, totalTime, scale, graphStartY, canvasWidth);
        drawLegendForImage(gc, entries, canvasWidth, canvasHeight);
        drawAxisLabelsForImage(gc, canvasWidth, canvasHeight, graphStartY);
    }

    private void drawHeader(GraphicsContext gc, int canvasWidth) {
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("ROUND ROBIN SCHEDULING - GANTT CHART", canvasWidth / 2, 50);

        // Add subtitle with simulation info
        if (!currentGanttEntries.isEmpty()) {
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            gc.setFill(Color.web("#7f8c8d"));
            gc.fillText("Process execution timeline with time units", canvasWidth / 2, 70);
        }
    }

    private void drawHeaderForImage(GraphicsContext gc, int canvasWidth) {
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.fillText("ROUND ROBIN SCHEDULING - GANTT CHART", canvasWidth / 2, 60);

        // Add subtitle with simulation info
        if (!currentGanttEntries.isEmpty()) {
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            gc.setFill(Color.web("#7f8c8d"));
            gc.fillText("Process execution timeline with time units", canvasWidth / 2, 85);
        }
    }

    private void drawTimeline(GraphicsContext gc, int totalTime, double scale, int graphStartY, int canvasWidth) {
        gc.setStroke(Color.web("#34495e"));
        gc.setLineWidth(3);
        int timelineY = graphStartY + BAR_HEIGHT + 40;
        double timelineEnd = CANVAS_MARGIN + totalTime * scale;
        gc.strokeLine(CANVAS_MARGIN, timelineY, timelineEnd, timelineY);
    }

    private void drawTimelineForImage(GraphicsContext gc, int totalTime, double scale, int graphStartY, int canvasWidth) {
        gc.setStroke(Color.web("#34495e"));
        gc.setLineWidth(4); // Thicker line for image
        int timelineY = graphStartY + BAR_HEIGHT + 50; // More space for image
        double timelineEnd = CANVAS_MARGIN + totalTime * scale;
        gc.strokeLine(CANVAS_MARGIN, timelineY, timelineEnd, timelineY);
    }

    private void drawGanttBars(GraphicsContext gc, List<GanttEntry> entries, double scale, int graphStartY, int canvasWidth, int canvasHeight) {
        List<String> uniqueProcesses = entries.stream()
                .map(GanttEntry::getProcessId)
                .distinct()
                .toList();

        int timelineY = graphStartY + BAR_HEIGHT + 40;

        // Check if we have enough space below header
        if (timelineY < HEADER_HEIGHT + GRAPH_START_OFFSET + 50) {
            timelineY = HEADER_HEIGHT + GRAPH_START_OFFSET + 50;
        }

        for (GanttEntry entry : entries) {
            int processIndex = uniqueProcesses.indexOf(entry.getProcessId());
            int barY = timelineY - BAR_HEIGHT - 10 - (processIndex * (BAR_HEIGHT + BAR_SPACING));

            // Ensure bars don't go above the header
            if (barY < HEADER_HEIGHT + 20) {
                barY = HEADER_HEIGHT + 20;
            }

            double startX = CANVAS_MARGIN + entry.getStartTime() * scale;
            double endX = CANVAS_MARGIN + entry.getEndTime() * scale;
            double width = Math.max(endX - startX, 4);

            String colorHex = Constants.getProcessColor(entry.getProcessId());
            Color barColor = Constants.hexToColor(colorHex);
            Color darkerColor = barColor.darker();

            gc.setFill(barColor);
            gc.fillRoundRect(startX, barY, width, BAR_HEIGHT, 10, 10);

            gc.setStroke(darkerColor);
            gc.setLineWidth(2);
            gc.strokeRoundRect(startX, barY, width, BAR_HEIGHT, 10, 10);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            double textX = startX + width / 2;
            double textY = barY + BAR_HEIGHT / 2 + 4;

            if (width < 25) {
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
            }

            gc.fillText(entry.getProcessId(), textX, textY);

            if (width > 30) {
                gc.setFill(Color.web("#2c3e50"));
                gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
                gc.fillText(String.valueOf(entry.getDuration()), textX, barY - 5);
            }

            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            if (entry == entries.get(0)) {
                drawTimeLabel(gc, entry.getStartTime(), startX, timelineY + TEXT_OFFSET);
            }
            drawTimeLabel(gc, entry.getEndTime(), endX, timelineY + TEXT_OFFSET);
        }

        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.RIGHT);

        for (int i = 0; i < uniqueProcesses.size(); i++) {
            String processId = uniqueProcesses.get(i);
            int barY = timelineY - BAR_HEIGHT - 10 - (i * (BAR_HEIGHT + BAR_SPACING));
            int textY = barY + BAR_HEIGHT / 2 + 4;

            // Ensure process labels don't overlap with header
            if (barY >= HEADER_HEIGHT + 20) {
                gc.fillText("Process " + processId, CANVAS_MARGIN - 10, textY);
            }
        }

        gc.setTextAlign(TextAlignment.CENTER);
    }

    private void drawGanttBarsForImage(GraphicsContext gc, List<GanttEntry> entries, double scale, int graphStartY, int canvasWidth, int canvasHeight) {
        List<String> uniqueProcesses = entries.stream()
                .map(GanttEntry::getProcessId)
                .distinct()
                .toList();

        int timelineY = graphStartY + BAR_HEIGHT + 50;

        for (GanttEntry entry : entries) {
            int processIndex = uniqueProcesses.indexOf(entry.getProcessId());
            int barY = timelineY - BAR_HEIGHT - 15 - (processIndex * (BAR_HEIGHT + BAR_SPACING));

            // Ensure bars don't go above the header
            if (barY < HEADER_HEIGHT + 30) {
                barY = HEADER_HEIGHT + 30;
            }

            double startX = CANVAS_MARGIN + entry.getStartTime() * scale;
            double endX = CANVAS_MARGIN + entry.getEndTime() * scale;
            double width = Math.max(endX - startX, 6);

            String colorHex = Constants.getProcessColor(entry.getProcessId());
            Color barColor = Constants.hexToColor(colorHex);
            Color darkerColor = barColor.darker();

            // Draw bar with better visibility for image
            gc.setFill(barColor);
            gc.fillRoundRect(startX, barY, width, BAR_HEIGHT, 12, 12);

            gc.setStroke(darkerColor);
            gc.setLineWidth(2);
            gc.strokeRoundRect(startX, barY, width, BAR_HEIGHT, 12, 12);

            // Better text visibility for image
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            double textX = startX + width / 2;
            double textY = barY + BAR_HEIGHT / 2 + 5;

            if (width < 30) {
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            }

            gc.fillText(entry.getProcessId(), textX, textY);

            // Duration label with better positioning
            if (width > 40) {
                gc.setFill(Color.web("#2c3e50"));
                gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
                gc.fillText(String.valueOf(entry.getDuration()), textX, barY - 8);
            }

            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            // Time labels with better spacing
            if (entry == entries.get(0)) {
                drawTimeLabelForImage(gc, entry.getStartTime(), startX, timelineY + TEXT_OFFSET + 5);
            }
            drawTimeLabelForImage(gc, entry.getEndTime(), endX, timelineY + TEXT_OFFSET + 5);
        }

        // Process labels with better positioning
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setTextAlign(TextAlignment.RIGHT);

        for (int i = 0; i < uniqueProcesses.size(); i++) {
            String processId = uniqueProcesses.get(i);
            int barY = timelineY - BAR_HEIGHT - 15 - (i * (BAR_HEIGHT + BAR_SPACING));
            int textY = barY + BAR_HEIGHT / 2 + 5;

            // Ensure process labels don't overlap with header
            if (barY >= HEADER_HEIGHT + 30) {
                gc.fillText("Process " + processId, CANVAS_MARGIN - 15, textY);
            }
        }

        gc.setTextAlign(TextAlignment.CENTER);
    }

    private void drawTimeMarks(GraphicsContext gc, int totalTime, double scale, int graphStartY, int canvasWidth) {
        gc.setStroke(Color.web("#7f8c8d"));
        gc.setLineWidth(1);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));

        int timelineY = graphStartY + BAR_HEIGHT + 40;

        for (int time = 0; time <= totalTime; time += TIME_MARK_INTERVAL) {
            double x = CANVAS_MARGIN + time * scale;
            gc.strokeLine(x, timelineY - 5, x, timelineY + 5);

            if (time % 2 == 0 || time == 0 || time == totalTime) {
                drawTimeLabel(gc, time, x, timelineY + TEXT_OFFSET);
            }
        }
    }

    private void drawTimeMarksForImage(GraphicsContext gc, int totalTime, double scale, int graphStartY, int canvasWidth) {
        gc.setStroke(Color.web("#7f8c8d"));
        gc.setLineWidth(1.5);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));

        int timelineY = graphStartY + BAR_HEIGHT + 50;

        for (int time = 0; time <= totalTime; time += TIME_MARK_INTERVAL) {
            double x = CANVAS_MARGIN + time * scale;
            gc.strokeLine(x, timelineY - 6, x, timelineY + 6);

            if (time % 2 == 0 || time == 0 || time == totalTime) {
                drawTimeLabelForImage(gc, time, x, timelineY + TEXT_OFFSET + 5);
            }
        }
    }

    private void drawLegend(GraphicsContext gc, List<GanttEntry> entries, int canvasWidth, int canvasHeight) {
        List<String> uniqueProcesses = entries.stream()
                .map(GanttEntry::getProcessId)
                .distinct()
                .filter(id -> !id.equals("IDLE"))
                .toList();

        if (!uniqueProcesses.isEmpty()) {
            int legendX = canvasWidth - LEGEND_WIDTH + 20;
            int legendY = HEADER_HEIGHT + 20; // Position below header

            drawLegendBox(gc, uniqueProcesses, legendX, legendY);
        }
    }

    private void drawLegendForImage(GraphicsContext gc, List<GanttEntry> entries, int canvasWidth, int canvasHeight) {
        List<String> uniqueProcesses = entries.stream()
                .map(GanttEntry::getProcessId)
                .distinct()
                .filter(id -> !id.equals("IDLE"))
                .toList();

        if (!uniqueProcesses.isEmpty()) {
            int legendX = canvasWidth - LEGEND_WIDTH + 20;
            int legendY = HEADER_HEIGHT + 30; // Position below header with more space

            drawLegendBoxForImage(gc, uniqueProcesses, legendX, legendY);
        }
    }

    private void drawLegendBox(GraphicsContext gc, List<String> uniqueProcesses, int legendX, int legendY) {
        int boxSize = 20; // Proper size for visibility
        int itemHeight = 25; // Height per legend item
        int verticalSpacing = 5; // Space between items
        int padding = 20; // Padding around the content
        int titleHeight = 25; // Space for the title

        // Calculate the total height needed for the legend box
        int totalItemsHeight = uniqueProcesses.size() * (itemHeight + verticalSpacing) - verticalSpacing;
        int totalBoxHeight = totalItemsHeight + padding * 2 + titleHeight;

        // Draw legend background with proper boundaries
        gc.setFill(Color.web("#ecf0f1"));
        gc.setStroke(Color.web("#bdc3c7"));
        gc.setLineWidth(1);
        gc.fillRoundRect(legendX - 10, legendY - 15, LEGEND_WIDTH - 20, totalBoxHeight, 10, 10);
        gc.strokeRoundRect(legendX - 10, legendY - 15, LEGEND_WIDTH - 20, totalBoxHeight, 10, 10);

        // Draw legend title
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("PROCESS LEGEND", legendX + (LEGEND_WIDTH - 40) / 2, legendY);

        // Draw legend items
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        for (int i = 0; i < uniqueProcesses.size(); i++) {
            String processId = uniqueProcesses.get(i);
            int yPos = legendY + titleHeight + (i * (itemHeight + verticalSpacing));

            // Draw color box
            String colorHex = Constants.getProcessColor(processId);
            Color boxColor = Constants.hexToColor(colorHex);
            gc.setFill(boxColor);
            gc.fillRect(legendX, yPos, boxSize, boxSize);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeRect(legendX, yPos, boxSize, boxSize);

            // Draw process text
            gc.setFill(Color.web("#2c3e50"));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText("Process " + processId, legendX + boxSize + 10, yPos + 14);
        }

        gc.setTextAlign(TextAlignment.CENTER);
    }

    private void drawLegendBoxForImage(GraphicsContext gc, List<String> uniqueProcesses, int legendX, int legendY) {
        int boxSize = 24; // Slightly larger for images
        int itemHeight = 28; // Height per legend item
        int verticalSpacing = 6; // Space between items
        int padding = 25; // Padding around the content
        int titleHeight = 30; // Space for the title

        // Calculate the total height needed for the legend box
        int totalItemsHeight = uniqueProcesses.size() * (itemHeight + verticalSpacing) - verticalSpacing;
        int totalBoxHeight = totalItemsHeight + padding * 2 + titleHeight;

        // Draw legend background with proper boundaries
        gc.setFill(Color.web("#f8f9fa"));
        gc.setStroke(Color.web("#95a5a6"));
        gc.setLineWidth(1.5);
        gc.fillRoundRect(legendX - 15, legendY - 20, LEGEND_WIDTH - 10, totalBoxHeight, 12, 12);
        gc.strokeRoundRect(legendX - 15, legendY - 20, LEGEND_WIDTH - 10, totalBoxHeight, 12, 12);

        // Draw legend title
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("PROCESS LEGEND", legendX + (LEGEND_WIDTH - 40) / 2, legendY);

        // Draw legend items
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

        for (int i = 0; i < uniqueProcesses.size(); i++) {
            String processId = uniqueProcesses.get(i);
            int yPos = legendY + titleHeight + (i * (itemHeight + verticalSpacing));

            // Draw color box
            String colorHex = Constants.getProcessColor(processId);
            Color boxColor = Constants.hexToColor(colorHex);
            gc.setFill(boxColor);
            gc.fillRect(legendX, yPos, boxSize, boxSize);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeRect(legendX, yPos, boxSize, boxSize);

            // Draw process text
            gc.setFill(Color.web("#2c3e50"));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText("Process " + processId, legendX + boxSize + 12, yPos + 16);
        }

        gc.setTextAlign(TextAlignment.CENTER);
    }

    private void drawAxisLabels(GraphicsContext gc, int canvasWidth, int canvasHeight, int graphStartY) {
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // Y-axis label (Processes)
        gc.save();
        gc.translate(30, graphStartY + (canvasHeight - HEADER_HEIGHT) / 2);
        gc.rotate(-90);
        gc.fillText("PROCESSES", 0, 0);
        gc.restore();

        // X-axis label (Time)
        gc.fillText("TIME (units)", canvasWidth / 2, canvasHeight - 20);
    }

    private void drawAxisLabelsForImage(GraphicsContext gc, int canvasWidth, int canvasHeight, int graphStartY) {
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Y-axis label (Processes)
        gc.save();
        gc.translate(35, graphStartY + (canvasHeight - HEADER_HEIGHT) / 2);
        gc.rotate(-90);
        gc.fillText("PROCESSES", 0, 0);
        gc.restore();

        // X-axis label (Time)
        gc.fillText("TIME (units)", canvasWidth / 2, canvasHeight - 25);
    }

    private void drawTimeLabel(GraphicsContext gc, int time, double x, double y) {
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        gc.fillText(String.valueOf(time), x, y);
    }

    private void drawTimeLabelForImage(GraphicsContext gc, int time, double x, double y) {
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        gc.fillText(String.valueOf(time), x, y);
    }

    private void clearCanvas(GraphicsContext gc) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void clearChart() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        clearCanvas(gc);
        canvas.setWidth(Constants.CANVAS_WIDTH);
        canvas.setHeight(Constants.CANVAS_HEIGHT);
        currentGanttEntries.clear();
    }

    public boolean isChartEmpty() {
        return currentGanttEntries.isEmpty();
    }

    public boolean saveAsImage(File file) {
        try {
            if (isChartEmpty()) {
                return false;
            }

            // Create optimal size for saved image
            int totalTime = currentGanttEntries.get(currentGanttEntries.size() - 1).getEndTime();
            int saveWidth = Math.min(2500, Math.max(1200, totalTime * 40 + 2 * CANVAS_MARGIN + LEGEND_WIDTH));
            int saveHeight = calculateCanvasHeight(currentGanttEntries) + 100; // Extra space

            // Limit dimensions
            saveWidth = Math.min(saveWidth, 3000);
            saveHeight = Math.min(saveHeight, 2000);

            Canvas saveCanvas = new Canvas(saveWidth, saveHeight);
            GraphicsContext saveGc = saveCanvas.getGraphicsContext2D();

            // Clear and draw with image-optimized layout
            clearCanvas(saveGc);
            drawChartForImage(saveGc, currentGanttEntries, saveWidth, saveHeight, totalTime);

            // Take snapshot
            WritableImage writableImage = new WritableImage(saveWidth, saveHeight);
            saveCanvas.snapshot(null, writableImage);

            // Convert and save
            BufferedImage bufferedImage = convertToBufferedImage(writableImage);
            return ImageIO.write(bufferedImage, "png", file);

        } catch (Exception e) {
            System.err.println("Error saving Gantt chart image: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private BufferedImage convertToBufferedImage(WritableImage writableImage) {
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Convert each pixel
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                javafx.scene.paint.Color fxColor = writableImage.getPixelReader().getColor(x, y);
                int argb = (int) (fxColor.getOpacity() * 255) << 24 |
                        (int) (fxColor.getRed() * 255) << 16 |
                        (int) (fxColor.getGreen() * 255) << 8 |
                        (int) (fxColor.getBlue() * 255);
                bufferedImage.setRGB(x, y, argb);
            }
        }

        return bufferedImage;
    }
}