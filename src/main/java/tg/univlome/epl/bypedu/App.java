package tg.univlome.epl.bypedu;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    Connection connection = DatabaseConnection.getDatabase();
    public static String page = "accueil";
    
    @Override
    public void start(Stage stage) throws IOException{
        BorderPane root = FXMLLoader.load(getClass().getResource("accueil.fxml"));
        scene = new Scene(root, 900, 480);
        stage.setScene(scene);
        stage.show();
    }
   
    public static void Loader(String fxml) {
        BorderPane root = null;
        try {
            root = FXMLLoader.load(App.class.getResource(fxml + ".fxml"));
        } catch (IOException ex) {
            System.out.println("Erreur de chargement de " + fxml);
        }
        scene.setRoot(root);
    }
    
    public static void main(String[] args){
        launch();
    }

}