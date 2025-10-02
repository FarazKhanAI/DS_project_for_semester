package org.app.roundrobin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/app/roundrobin/main.fxml")));

        Scene scene = new Scene(root, 1200, 650);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/org/app/roundrobin/main.css")).toExternalForm());

        primaryStage.setTitle("Round Robin CPU Scheduling Simulator");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        // Set to full screen
        primaryStage.setMaximized(true); // This maximizes the window to fill the screen
        // OR use true fullscreen mode (removes window borders):
        // primaryStage.setFullScreen(true);

        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/app/roundrobin/icon.png")));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Icon not found, using default icon");
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}