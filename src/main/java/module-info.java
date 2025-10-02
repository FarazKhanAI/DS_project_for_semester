module org.app.roundrobin {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;

    opens org.app.roundrobin.controller to javafx.fxml;
    exports org.app.roundrobin;
    exports org.app.roundrobin.controller;
    exports org.app.roundrobin.model;
    exports org.app.roundrobin.algorithm;
}