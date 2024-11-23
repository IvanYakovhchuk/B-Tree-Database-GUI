module com.algorithms.gui {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.algorithms.gui to javafx.fxml;
    exports com.algorithms.gui;
}