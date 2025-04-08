package com.example.btreemp2;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class twoThreeTreeController extends Application implements Initializable {
    @FXML private Button menuButton;
    @FXML private AnchorPane menu;

    public static void main(String[] args) {
        launch(args);
    }
    public void start(Stage stage) throws IOException {
        Parent root= FXMLLoader.load(getClass().getResource("twoThreeTree.fxml"));
        Scene scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void initialize(URL location, ResourceBundle resources) {
        menuButton.setOnMouseClicked(event -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.4));
            slide.setNode(menu);
            double buttonValue=menu.getTranslateX();
            if(buttonValue==500) {
                menu.setTranslateX(500);
                slide.setToX(0);
                slide.play();
            }
            else if(buttonValue==0) {
                menu.setTranslateX(0);
                slide.setToX(500);
                slide.play();
            }



        });
    }
}
