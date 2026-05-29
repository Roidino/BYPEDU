/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univlome.epl.bypedu.Controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import tg.univlome.epl.bypedu.DAOs.EtudiantDAO;
import tg.univlome.epl.bypedu.models.Etudiant;

/**
 *
 * @author Savastano
 */
public class EtudiantController implements Initializable{
    
    @FXML TableView<Etudiant> tableau;
    @FXML TableColumn<Etudiant, String> colonneNom;
    @FXML TableColumn<Etudiant, String> colonnePrenom;
    @FXML TableColumn<Etudiant, Integer> colonneAge;
    @FXML TableColumn<Etudiant, String> colonneClasse;
    @FXML TableColumn<Etudiant, Double> colonneMoyenne;
    @FXML TableColumn<Etudiant, Integer> colonneNumero;
    
    
    private EtudiantDAO dao = new EtudiantDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colonneNom.setCellValueFactory( new PropertyValueFactory<>("nom"));
        colonnePrenom.setCellValueFactory( new PropertyValueFactory<>("prenom"));
        colonneAge.setCellValueFactory( new PropertyValueFactory<>("age"));
        colonneClasse.setCellValueFactory( new PropertyValueFactory<>("classe"));
        colonneMoyenne.setCellValueFactory( new PropertyValueFactory<>("moyenne"));
        colonneNumero.setCellValueFactory( new PropertyValueFactory<>("numero"));
        
        List<Etudiant> liste = dao.getAll();
        if (liste == null) liste = new ArrayList<>();
        tableau.setItems(
                FXCollections.observableArrayList(liste));
      
    }
    
    
}
