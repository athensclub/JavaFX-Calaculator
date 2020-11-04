module athensclub.calculator {
    requires java.logging;
    requires javafx.fxml;
    requires javafx.controls;
    requires com.jfoenix;
    requires ch.obermuhlner.math.big;

    opens athensclub.calculator to javafx.graphics;
    opens athensclub.calculator.controllers to javafx.fxml;
    opens athensclub.calculator.views to javafx.fxml;
}