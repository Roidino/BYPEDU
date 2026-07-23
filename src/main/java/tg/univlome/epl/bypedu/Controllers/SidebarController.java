/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univlome.epl.bypedu.Controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import tg.univlome.epl.bypedu.App;

/**
 *
 * @author Savastano
 */
public class SidebarController implements Initializable {
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        switch(App.page){
            case "dashboard": 
                dashboard.getStyleClass().remove("nav-btn"); 
                dashboard.getStyleClass().add("nav-btn-active"); 
                break ;
            case "etudiants": 
                etudiants.getStyleClass().remove("nav-btn");
                etudiants.getStyleClass().add("nav-btn-active");
                break ;
            case "enseignants": 
                enseignants.getStyleClass().remove("nav-btn");
                enseignants.getStyleClass().add("nav-btn-active");
                break ;
            case "classes":
                classes.getStyleClass().remove("nav-btn");
                classes.getStyleClass().add("nav-btn-active");
                break ;
            case "cours": 
                cours.getStyleClass().remove("nav-btn");
                cours.getStyleClass().add("nav-btn-active");
                break ;
            case "notes": 
                notes.getStyleClass().remove("nav-btn");
                notes.getStyleClass().add("nav-btn-active");
                break ;
            case "bulletins":
                bulletins.getStyleClass().remove("nav-btn");
                bulletins.getStyleClass().add("nav-btn-active");
                break ;
            case "emploi":
                emploi.getStyleClass().remove("nav-btn");
                emploi.getStyleClass().add("nav-btn-active");
                break ;
        }
    }
    
    @FXML public Button dashboard;
    @FXML public Button etudiants;
    @FXML public Button enseignants;
    @FXML public Button classes;
    @FXML public Button cours;
    @FXML public Button notes;
    @FXML public Button bulletins;
    @FXML public Button emploi;
    
    @FXML public void dashboardLoad(){
        navigate("dashboard");
    }
    
    @FXML
    public void etudiantsLoad(){
        navigate("etudiants");
    }
    
    @FXML
    public void enseignantsLoad(){
        navigate("enseignants");
    }

    @FXML
    public void classesLoad(){
        navigate("classes");
    }
    
    @FXML
    public void coursLoad(){
        navigate("cours");
    }
    
    @FXML
    public void notesLoad(){
        navigate("notes");
    }

    @FXML
    public void bulletinsLoad(){
        navigate("bulletins");
    }
    
    @FXML
    public void emploiLoad(){
        navigate("emploi");
    }

    private void navigate(String fxml){
        App.page = fxml;
        App.Loader(fxml);
    }
}
