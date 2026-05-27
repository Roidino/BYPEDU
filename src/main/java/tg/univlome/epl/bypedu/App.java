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
    
    @Override
    public void start(Stage stage) throws IOException{
        
        getAll();
        BorderPane root = FXMLLoader.load(getClass().getResource("accueil.fxml"));
        scene = new Scene(root, 400, 400);
        stage.setScene(scene);
        stage.show();
    }
    
    public void getAll(){
        
        String sql = "SELECT * FROM Etudiant";
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
    
    public static void main(String[] args){
        launch();
    }

}