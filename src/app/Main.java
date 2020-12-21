package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/Home.fxml"));
        Parent root = loader.load();
        HomeController controller = loader.getController();
        primaryStage.setOnHidden(e -> {
            System.out.println("closing");
            try {
                controller.cleanUp();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        primaryStage.setTitle("Library Manager");
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
