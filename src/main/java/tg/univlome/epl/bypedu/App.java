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
        getAll();
        BorderPane root = FXMLLoader.load(getClass().getResource("accueil.fxml"));
        scene = new Scene(root, 720, 480);
        stage.setScene(scene);
        stage.show();
    }
    
    public void getAll(){
        
        String sql = "SELECT * FROM etudiants";
        PreparedStatement exec;
        ResultSet result;
        try {
            exec = connection.prepareStatement(sql);
            System.out.println("connection executer avec succes");
            result = exec.executeQuery();
            while(result.next()){
                System.out.println(result.getString(2));
            }
        } catch (SQLException ex) {
            System.getLogger(App.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
   public static void Loader(String fxml) {
    try {
        var url = App.class.getResource(fxml + ".fxml");
        System.out.println(">>> URL cours.fxml : " + url);
        if (url == null) {
            System.out.println(">>> FICHIER NULL - non trouvé dans le classpath");
            return; // évite le crash
        }
        BorderPane root = FXMLLoader.load(url);
        scene.setRoot(root);
    } catch (Exception ex) {
        System.out.println(">>> EXCEPTION : " + ex.getMessage());
        ex.printStackTrace();
    }
}
    
    public static void main(String[] args){
        launch();
    }

}