package tg.univlome.epl.bypedu.Controllers;

import tg.univlome.epl.bypedu.models.Enseignant;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import tg.univlome.epl.bypedu.models.Enseignant; // Vérifie bien si ton package de modèle est celui-là

public class EnseignantsController {

    // Liens avec les composants du fichier FXML (fx:id)
    @FXML
    private TextField txtRecherche;

    @FXML
    private ComboBox<String> comboMatieres;

    @FXML
    private TableView<Enseignant> tableEnseignants; // Le tableau gère des objets de type Enseignant

    @FXML
    private TableColumn<Enseignant, String> colNom;

    @FXML
    private TableColumn<Enseignant, String> colMatiere;

    @FXML
    private TableColumn<Enseignant, String> colClasse;

    @FXML
    private TableColumn<Enseignant, String> colEmail;

    @FXML
    private TableColumn<Enseignant, String> colTelephone;

    @FXML
    private TableColumn<Enseignant, Void> colActions; // Colonne pour les boutons modifier/supprimer

    /**
     * Méthode appelée automatiquement par JavaFX lors du chargement de la page
     */
    @FXML
    public void initialize() {
        // C'est ici qu'on viendra configurer le tableau et charger les données plus tard
        System.out.println("Contrôleur Enseignants initialisé !");
    }

    /**
     * Méthode liée au bouton "+ Ajouter un enseignant"
     */
    @FXML
    private void onAjouterEnseignant() {
        System.out.println("Clic détecté sur le bouton Ajouter !");
        // C'est ici qu'on affichera le formulaire d'ajout
    }
}