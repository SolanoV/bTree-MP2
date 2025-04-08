package com.example.btreemp2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class twoThreeTreeController extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    public void start(Stage stage) throws IOException {
        Parent root= FXMLLoader.load(getClass().getResource("twoThreeTree.fxml"));
        Scene scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
