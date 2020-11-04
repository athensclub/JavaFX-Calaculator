package athensclub.calculator.views;

import athensclub.calculator.controllers.MainViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class MainView extends BorderPane {

    private MainViewController controller;

    public MainView(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main_view.fxml"));
            loader.setRoot(this);
            loader.load();
            controller = loader.getController();
            getStylesheets().add(getClass().getClassLoader().getResource("main_style.css").toExternalForm());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MainViewController getController() {
        return controller;
    }
}
