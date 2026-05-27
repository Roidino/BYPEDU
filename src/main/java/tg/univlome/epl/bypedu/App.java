package tg.univlome.epl.bypedu;

import java.io.IOException;
import java.sql.Connection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tg.univlome.epl.bypedu.DAOs.DatabaseConnection;


/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    
    @Override
    public void start(Stage stage) throws IOException{
        Connection connection = DatabaseConnection.getDatabase();
        BorderPane root = FXMLLoader.load(getClass().getResource("accueil.fxml"));
        scene = new Scene(root, 400, 400);
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args){
        launch();
    }

}