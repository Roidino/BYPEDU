/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univlome.epl.bypedu.Controllers;

/**
 *
 * @author Savastano
 */

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tg.univlome.epl.bypedu.DAOs.ClasseDAO;
import tg.univlome.epl.bypedu.DAOs.EtudiantDAO;
import tg.univlome.epl.bypedu.models.Classe;
import tg.univlome.epl.bypedu.models.Etudiant;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.HBox;

public class FormulaireEtudiantController implements Initializable {

    @FXML private Label    titreFormulaire;
    @FXML private TextField champNom;
    @FXML private TextField champPrenom;
    @FXML private DatePicker champDateNaissance;
    @FXML private TextField champTelephone;
    @FXML private ComboBox<String> champClasse;
    @FXML private ComboBox<String> champStatut;
    @FXML private Label messageErreur;
    @FXML private Button btnSauvegarder;
    @FXML private HBox errorMessage;

    private Etudiant etudiant;    
    private boolean sauvegarde = false;
    private List<Classe> classes; 

    private final EtudiantDAO etudiantDAO = new EtudiantDAO();
    private final ClasseDAO   classeDAO   = new ClasseDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        classes = classeDAO.getAll();
        if (classes == null) {
            classes = new ArrayList<>();
            System.out.println("ERREUR: ClasseDAO.getAll() a retourné null");
        }

        champClasse.setItems(FXCollections.observableArrayList(
            classes.stream()
                   .map(Classe::getNom)
                   .collect(Collectors.toList())
        ));

        champStatut.setItems(FXCollections.observableArrayList("ACTIF", "INACTIF"));
        champStatut.setValue("ACTIF");
    }

    public void setEtudiant(Etudiant e) {
        this.etudiant = e;
        if (e != null) {
            titreFormulaire.setText("Modifier l'étudiant");
            btnSauvegarder.setText("Modifier");
            champNom.setText(e.getNom());
            champPrenom.setText(e.getPrenom());
            champTelephone.setText(e.getTelephone());
            champDateNaissance.setValue(e.getDate_naissance());
            champClasse.setValue(e.getClasse());
            champStatut.setValue(e.getStatus());
        } else {
            titreFormulaire.setText("Ajouter un étudiant");
            btnSauvegarder.setText("Sauvegarder");
        }
    }

    @FXML
    private void sauvegarder() {
        if (champNom.getText().isBlank()) {
            errorMessage.setVisible(true);
            errorMessage.setManaged(true);
            messageErreur.setText("Le nom est obligatoire.");
            return;
        }
        if (champPrenom.getText().isBlank()) {
            errorMessage.setVisible(true);
            errorMessage.setManaged(true);
            messageErreur.setText("Le prénom est obligatoire.");
            return;
        }
        if (champClasse.getValue() == null) {
            messageErreur.setText("La classe est obligatoire.");
            return;
        }
        if (champDateNaissance.getValue() == null) {
            errorMessage.setVisible(true);
            errorMessage.setManaged(true);
            messageErreur.setText("La date de naissance est obligatoire.");
            return;
        }
        int classeId = classes.stream()
            .filter(c -> c.getNom().equals(champClasse.getValue()))
            .findFirst()
            .map(Classe::getId)
            .orElse(0);
        if (etudiant == null) {
            Etudiant nouveau = new Etudiant();
            nouveau.setNom(champNom.getText().trim());
            nouveau.setPrenom(champPrenom.getText().trim());
            nouveau.setTelephone(champTelephone.getText().trim());
            nouveau.setDate_naissance(champDateNaissance.getValue());
            nouveau.setClasse_id(classeId);
            nouveau.setStatus(champStatut.getValue());
            boolean ok = etudiantDAO.ajoute(nouveau);
            if (!ok) {
                errorMessage.setVisible(true);
                errorMessage.setManaged(true);
                messageErreur.setText("Erreur lors de l'ajout.");
                return;
            }
        } else {
            etudiant.setNom(champNom.getText().trim());
            etudiant.setPrenom(champPrenom.getText().trim());
            etudiant.setTelephone(champTelephone.getText().trim());
            etudiant.setDate_naissance(champDateNaissance.getValue());
            etudiant.setClasse_id(classeId);
            etudiant.setStatus(champStatut.getValue());
            boolean ok = etudiantDAO.update(etudiant);
            if (!ok) {
                errorMessage.setVisible(true);
                errorMessage.setManaged(true);
                messageErreur.setText("Erreur lors de la modification.");
                return;
            }
        }
        sauvegarde = true;
        fermerStage();
    }

    @FXML
    private void annuler() {
        fermerStage();
    }

    private void fermerStage() {
        Stage stage = (Stage) champNom.getScene().getWindow();
        stage.close();
    }

    public boolean isSauvegarde() {
        return sauvegarde;
    }
}
