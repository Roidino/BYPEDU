
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
    Connection connection = DatabaseConnection.getDatabase();
    public static String page = "dashboard";
    
    @Override
    public void start(Stage stage) throws IOException{
        BorderPane root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
        scene = new Scene(root, 980, 620);
        stage.setTitle("BYPEDU - Gestion Scolaire");
        stage.setScene(scene);
        stage.show();
    }
   
    public static void Loader(String fxml) {
        BorderPane root = null;
        try {
            root = FXMLLoader.load(App.class.getResource(fxml + ".fxml"));
        } catch (IOException ex) {
            System.out.println("Erreur de chargement de " + fxml);
            return;
        }
        scene.setRoot(root);
    }
    
    public static void main(String[] args){
        launch();
    }

}