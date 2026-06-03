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

public class FormulaireEtudiantController implements Initializable {

    @FXML private Label    titreFormulaire;
    @FXML private TextField champNom;
    @FXML private TextField champPrenom;
    @FXML private DatePicker champDateNaissance;
    @FXML private TextField champTelephone;
    @FXML private ComboBox<String> champClasse;
    @FXML private ComboBox<String> champStatut;
    @FXML private Label    messageErreur;
    @FXML private Button   btnSauvegarder;

    private Etudiant etudiant;        // null = ajout, rempli = modification
    private boolean sauvegarde = false;
    private List<Classe> classes;     // pour récupérer l'id depuis le nom

    private final EtudiantDAO etudiantDAO = new EtudiantDAO();
    private final ClasseDAO   classeDAO   = new ClasseDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Charger les classes depuis la BDD
        classes = classeDAO.getAll();
        champClasse.setItems(FXCollections.observableArrayList(
            classes.stream()
       .map(Classe::getNom)
       .collect(Collectors.toList())
        ));

        // Statuts disponibles
        champStatut.setItems(FXCollections.observableArrayList("ACTIF", "INACTIF"));
        champStatut.setValue("ACTIF");
    }

    // Appelé depuis EtudiantController avant d'afficher le stage
    public void setEtudiant(Etudiant e) {
        this.etudiant = e;

        if (e != null) {
            // Mode modification — remplir les champs
            titreFormulaire.setText("Modifier l'étudiant");
            btnSauvegarder.setText("Modifier");
            champNom.setText(e.getNom());
            champPrenom.setText(e.getPrenom());
            champTelephone.setText(e.getTelephone());
            champDateNaissance.setValue(e.getDate_naissance());
            champClasse.setValue(e.getClasse());
            champStatut.setValue(e.getStatus());
        } else {
            // Mode ajout
            titreFormulaire.setText("Ajouter un étudiant");
            btnSauvegarder.setText("Sauvegarder");
        }
    }

    @FXML
    private void sauvegarder() {
        // Validation
        if (champNom.getText().isBlank()) {
            messageErreur.setText("Le nom est obligatoire.");
            return;
        }
        if (champPrenom.getText().isBlank()) {
            messageErreur.setText("Le prénom est obligatoire.");
            return;
        }
        if (champClasse.getValue() == null) {
            messageErreur.setText("La classe est obligatoire.");
            return;
        }
        if (champDateNaissance.getValue() == null) {
            messageErreur.setText("La date de naissance est obligatoire.");
            return;
        }

        // Trouver l'id de la classe sélectionnée
        int classeId = classes.stream()
            .filter(c -> c.getNom().equals(champClasse.getValue()))
            .findFirst()
            .map(Classe::getId)
            .orElse(0);

        if (etudiant == null) {
            // Mode ajout — créer un nouvel étudiant
            Etudiant nouveau = new Etudiant();
            nouveau.setNom(champNom.getText().trim());
            nouveau.setPrenom(champPrenom.getText().trim());
            nouveau.setTelephone(champTelephone.getText().trim());
            nouveau.setDate_naissance(champDateNaissance.getValue());
            nouveau.setClasse_id(classeId);
            nouveau.setStatus(champStatut.getValue());

            boolean ok = etudiantDAO.ajoute(nouveau);
            if (!ok) {
                messageErreur.setText("Erreur lors de l'ajout.");
                return;
            }
        } else {
            // Mode modification — mettre à jour
            etudiant.setNom(champNom.getText().trim());
            etudiant.setPrenom(champPrenom.getText().trim());
            etudiant.setTelephone(champTelephone.getText().trim());
            etudiant.setDate_naissance(champDateNaissance.getValue());
            etudiant.setClasse_id(classeId);
            etudiant.setStatus(champStatut.getValue());

            boolean ok = etudiantDAO.update(etudiant);
            if (!ok) {
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

    // EtudiantController vérifie si on a sauvegardé pour rafraîchir le tableau
    public boolean isSauvegarde() {
        return sauvegarde;
    }
}
