package com.smartpos;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartPosApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(SmartPosApplication.class).run();
    }

    @Override
    public void start(Stage stage) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/login_layout.fxml"));
            loader.setControllerFactory(context::getBean);
            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            stage.setTitle("SmartPOS - Ultra Extreme Edition");

            // Add Application Icon
            try {
                stage.getIcons().add(new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/icons/app_icon.png")));
            } catch (Exception e) {
                System.out.println("⚠️ Icon not found: /icons/app_icon.png");
            }

            stage.setScene(scene);
            stage.setWidth(1024);
            stage.setHeight(768);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
