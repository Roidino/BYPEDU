package tg.univlome.epl.bypedu.Controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import tg.univlome.epl.bypedu.models.Enseignants;

public class EnseignantsController implements Initializable {

    @FXML private TextField txtRechercher;
    @FXML private ComboBox<String> comboMatieres;
    @FXML private TableView<Enseignants> tableEnseignants; 
    @FXML private TableColumn<Enseignants, String> colNom;
    @FXML private TableColumn<Enseignants, String> colMatiere;
    @FXML private TableColumn<Enseignants, String> colClasse;
    @FXML private TableColumn<Enseignants, String> colEmail;
    @FXML private TableColumn<Enseignants, String> colTelephone;
    @FXML private TableColumn<Enseignants, Void> colActions;

    // Liste observable qui contient les données du tableau
    private final ObservableList<Enseignants> listeEnseignants = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Liaison des colonnes avec les attributs du modèle Enseignants
        // (Attention : les chaînes "nom", "matiere", etc. doivent correspondre EXACTEMENT aux getters de ton modèle)
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // 2. Remplir la ComboBox des matières
        comboMatieres.setItems(FXCollections.observableArrayList("Toutes", "Mathématiques", "Informatique", "Physique", "Anglais"));
        comboMatieres.setValue("Toutes");

        // 3. Charger quelques données de test pour voir si ça s'affiche
        chargerDonneesTest();
        
        // 4. Associer la liste au TableView
        tableEnseignants.setItems(listeEnseignants);
        tableEnseignants.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        System.out.println("Contrôleur Enseignants initialisé avec succès !");
    }

    private void chargerDonneesTest() {
        listeEnseignants.clear();
        // Ajuste ce constructeur selon les paramètres réels de ta classe Enseignants
        listeEnseignants.add(new Enseignants("Dr. KOFFI K. Mawussé", "Informatique", "Génie Logiciel", "koffi@epl.lome", "+228 90 00 00 01"));
        listeEnseignants.add(new Enseignants("Mme. AMEDEE Ayélé", "Mathématiques", "Tronc Commun", "amedee@epl.lome", "+228 91 22 33 44"));
    }

    @FXML
    private void AjouterEnseignant(ActionEvent event) {
        System.out.println("Clic détecté sur le bouton Ajouter !");
        // Ta logique pour ouvrir un pop-up d'ajout par exemple
    }

    @FXML
    private void Recherche(ActionEvent event) {
        String recherche = txtRechercher.getText().toLowerCase();
        System.out.println("Recherche demandée pour : " + recherche);
        // Logique de filtrage de la liste ici
    }
}