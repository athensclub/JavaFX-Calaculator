package athensclub.calculator;

import athensclub.calculator.views.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainView mainView = new MainView();
        Scene mainScene = new Scene(mainView);
        mainScene.addEventHandler(KeyEvent.ANY, mainView.getController().createKeyBindHandler());
        primaryStage.setTitle("Calculator - By Kawin R.");
        primaryStage.setScene(mainScene);
        primaryStage.show();
        primaryStage.setMinWidth(mainView.getPrefWidth());
        primaryStage.setMinHeight(mainView.getPrefHeight());

    }
}
