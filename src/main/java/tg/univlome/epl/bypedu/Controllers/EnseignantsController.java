package tg.univlome.epl.bypedu.Controllers;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import tg.univlome.epl.bypedu.DAOs.EnseignantsDAO;
import tg.univlome.epl.bypedu.models.Enseignants;

public class EnseignantsController implements Initializable {

    @FXML
    private TextField txtRechercher;
    @FXML
    private ComboBox<String> comboMatieres;
    @FXML
    private TableView<Enseignants> tableEnseignants;
    @FXML
    private TableColumn<Enseignants, String> colNom;
    @FXML
    private TableColumn<Enseignants, String> colPrenom; // Ajouté pour séparer le prénom
    @FXML
    private TableColumn<Enseignants, String> colMatiere;
    @FXML
    private TableColumn<Enseignants, String> colClasse;
    @FXML
    private TableColumn<Enseignants, String> colEmail;
    @FXML
    private TableColumn<Enseignants, String> colTelephone;
    @FXML
    private TableColumn<Enseignants, Void> colActions;

    // 1. La liste source (Master Data)
    private final ObservableList<Enseignants> listeEnseignants = FXCollections.observableArrayList();
    private final EnseignantsDAO enseignantsDAO = new EnseignantsDAO();

    // 2. La liste filtrée qui va envelopper la liste source
    private FilteredList<Enseignants> donneesFiltrees;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Configuration des colonnes (Nom et Prénom séparés)
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // 2. Remplir la ComboBox dynamiquement depuis la BDD (Connexion sécurisée)
        chargerMatieresDepuisBDD();

        // 3. Initialisation indispensable de la liste filtrée
        donneesFiltrees = new FilteredList<>(listeEnseignants, p -> true);

        // 4. Écouter les changements dans le champ de recherche de texte
        txtRechercher.textProperty().addListener((observable, oldValue, newValue) -> {
            appliquerFiltre(newValue, comboMatieres.getValue());
        });

        // 5. Écouter les changements de sélection dans la ComboBox
        comboMatieres.valueProperty().addListener((observable, oldValue, newValue) -> {
            appliquerFiltre(txtRechercher.getText(), newValue);
        });

        // 6. Préparer le pipeline de tri
        SortedList<Enseignants> donneesTriees = new SortedList<>(donneesFiltrees);
        donneesTriees.comparatorProperty().bind(tableEnseignants.comparatorProperty());
        
        // 7. Lier le tableau aux données triées et filtrées
        tableEnseignants.setItems(donneesTriees);
        tableEnseignants.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 8. Ajouter les boutons d'action (Modifier / Supprimer)
        ajouterBoutonsActions();

        // 9. Charger les lignes de données depuis SQLite en tout dernier
        chargerDonneesDepuisBDD(); 
    }

    /**
     * Centralise la logique de filtrage combinée (Texte + ComboBox)
     */
    private void appliquerFiltre(String texteRecherche, String matiereSelectionnee) {
        donneesFiltrees.setPredicate(enseignant -> {

            // Étape A : Filtrage par texte (Nom, Prénom ou Email)
            boolean correspondTexte = true;
            if (texteRecherche != null && !texteRecherche.isEmpty()) {
                String minuscules = texteRecherche.toLowerCase();
                correspondTexte = (enseignant.getNom() != null && enseignant.getNom().toLowerCase().contains(minuscules))
                        || (enseignant.getPrenom() != null && enseignant.getPrenom().toLowerCase().contains(minuscules))
                        || (enseignant.getEmail() != null && enseignant.getEmail().toLowerCase().contains(minuscules));
            }

            // Étape B : Filtrage par matière (ComboBox)
            boolean correspondMatiere = true;
            if (matiereSelectionnee != null && !matiereSelectionnee.equals("Toutes")) {
                correspondMatiere = matiereSelectionnee.equals(enseignant.getMatiere());
            }

            // L'enseignant est affiché uniquement s'il valide les deux critères
            return correspondTexte && correspondMatiere;
        });
    }

    /**
     * Ajoute dynamiquement les boutons Modifier et Supprimer dans la colonne Actions
     */
    private void ajouterBoutonsActions() {
        Callback<TableColumn<Enseignants, Void>, TableCell<Enseignants, Void>> cellFactory = (param) -> {
            return new TableCell<Enseignants, Void>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Button btnEdit = new Button("Modifier");
                        btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");

                        Button btnDelete = new Button("Supprimer");
                        btnDelete.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");

                        // Action : MODIFIER
                        btnEdit.setOnAction(event -> {
                            Enseignants e = getTableView().getItems().get(getIndex());
                            Optional<Enseignants> resultat = afficherFormulaireEnseignant(e);
                            
                            resultat.ifPresent(enseignantModifie -> {
                                boolean misAJour = enseignantsDAO.update(enseignantModifie);
                                if (misAJour) {
                                    chargerDonneesDepuisBDD();
                                    System.out.println("Enseignant mis à jour : " + enseignantModifie.getNom());
                                } else {
                                    Alert errorAlert = new Alert(AlertType.ERROR, "Impossible de modifier cet enseignant en BDD.", ButtonType.OK);
                                    errorAlert.showAndWait();
                                }
                            });
                        });

                        // Action : SUPPRIMER
                        btnDelete.setOnAction(event -> {
                            Enseignants e = getTableView().getItems().get(getIndex());
                            
                            Alert alert = new Alert(AlertType.CONFIRMATION);
                            alert.setTitle("Confirmation de suppression");
                            alert.setHeaderText("Supprimer l'enseignant : " + e.getNom() + " " + e.getPrenom() + " ?");
                            alert.setContentText("Cette action supprimera également ses affectations de classes. Continuer ?");

                            Optional<ButtonType> result = alert.showAndWait();
                            
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                boolean confirmationSuppression = enseignantsDAO.delete(e.getId());
                                
                                if (confirmationSuppression) {
                                    listeEnseignants.remove(e);
                                    System.out.println("Enseignant supprimé : " + e.getNom());
                                } else {
                                    Alert errorAlert = new Alert(AlertType.ERROR);
                                    errorAlert.setTitle("Erreur");
                                    errorAlert.setHeaderText("Erreur de suppression");
                                    errorAlert.setContentText("Impossible de supprimer cet enseignant. Vérifiez les contraintes de la base de données.");
                                    errorAlert.showAndWait();
                                }
                            }
                        });

                        HBox managebtn = new HBox(btnEdit, btnDelete);
                        managebtn.setStyle("-fx-alignment: center;");
                        managebtn.setSpacing(10);
                        setGraphic(managebtn);
                    }
                    setText(null);
                }
            };
        };
        colActions.setCellFactory(cellFactory);
    }

    /**
     * Génère un formulaire dialog dynamique sans FXML (Nom, Prénom et Matière gérés proprement)
     */
private Optional<Enseignants> afficherFormulaireEnseignant(Enseignants enseignantAModifier) {
    boolean estModification = (enseignantAModifier != null);
    
    Dialog<Enseignants> dialog = new Dialog<>();
    dialog.setTitle(estModification ? "Modifier l'enseignant" : "Ajouter un enseignant");

    ButtonType boutonTypeSauvegarder = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(boutonTypeSauvegarder, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setStyle("-fx-padding: 20;");

    TextField txtNom = new TextField();
    TextField txtPrenom = new TextField();
    TextField txtEmail = new TextField();
    TextField txtTelephone = new TextField();
    
    // 1. ComboBox pour la Matière
    ComboBox<String> comboMatiereForm = new ComboBox<>();
    ObservableList<String> matieresForm = FXCollections.observableArrayList(comboMatieres.getItems());
    matieresForm.remove("Toutes");
    comboMatiereForm.setItems(matieresForm);

    // 2. ComboBox pour la Classe (Nouveau !)
    ComboBox<String> comboClasseForm = new ComboBox<>();
    comboClasseForm.setItems(obtenirListeClassesBDD()); // Charge les classes de la BDD

    // Réalignement de la grille (6 lignes au total)
    grid.add(new Label("Nom :"), 0, 0);
    grid.add(txtNom, 1, 0);
    grid.add(new Label("Prénom :"), 0, 1);
    grid.add(txtPrenom, 1, 1);
    grid.add(new Label("Matière assignée :"), 0, 2);
    grid.add(comboMatiereForm, 1, 2);
    grid.add(new Label("Classe assignée :"), 0, 3); // Ajouté ici
    grid.add(comboClasseForm, 1, 3);
    grid.add(new Label("Email :"), 0, 4);
    grid.add(txtEmail, 1, 4);
    grid.add(new Label("Téléphone :"), 0, 5);
    grid.add(txtTelephone, 1, 5);

    // Remplissage en mode modification
    if (estModification) {
        txtNom.setText(enseignantAModifier.getNom()); 
        txtPrenom.setText(enseignantAModifier.getPrenom()); 
        txtEmail.setText(enseignantAModifier.getEmail());
        txtTelephone.setText(enseignantAModifier.getTelephone());
        comboMatiereForm.setValue(enseignantAModifier.getMatiere());
        comboClasseForm.setValue(enseignantAModifier.getClasse()); // Sélectionne sa classe actuelle
    }

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == boutonTypeSauvegarder) {
            Enseignants e = estModification ? enseignantAModifier : new Enseignants();
            e.setNom(txtNom.getText());
            e.setPrenom(txtPrenom.getText());
            e.setMatiere(comboMatiereForm.getValue());
            e.setClasse(comboClasseForm.getValue()); // Récupération de la classe
            e.setEmail(txtEmail.getText());
            e.setTelephone(txtTelephone.getText());
            return e;
        }
        return null;
    });

    return dialog.showAndWait();
}

/**
 * Petite méthode utilitaire pour charger les classes disponibles dans le formulaire
 */
private ObservableList<String> obtenirListeClassesBDD() {
    ObservableList<String> liste = FXCollections.observableArrayList();
    String sql = "SELECT nom FROM classes ORDER BY nom ASC";
    java.sql.Connection conn = tg.univlome.epl.bypedu.DAOs.DatabaseConnection.getDatabase();
    if (conn != null) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                liste.add(rs.getString("nom"));
            }
        } catch (SQLException ex) {
            System.err.println("Erreur chargement classes formulaire : " + ex.getMessage());
        }
    }
    return liste;
}

    private void chargerDonneesDepuisBDD() {
        listeEnseignants.clear();
        listeEnseignants.addAll(enseignantsDAO.getAll());
        System.out.println("Données chargées via EnseignantsDAO !");
    }

    @FXML
    private void AjouterEnseignant(ActionEvent event) {
        Optional<Enseignants> resultat = afficherFormulaireEnseignant(null);
        
        resultat.ifPresent(nouvelEnseignant -> {
            boolean insere = enseignantsDAO.ajoute(nouvelEnseignant); 
            if (insere) {
                chargerDonneesDepuisBDD(); 
                System.out.println("Nouvel enseignant ajouté avec succès !");
            } else {
                Alert errorAlert = new Alert(AlertType.ERROR, "Erreur lors de l'insertion en base de données.", ButtonType.OK);
                errorAlert.showAndWait();
            }
        });
    }

    @FXML
    private void Recherche(ActionEvent event) {
        appliquerFiltre(txtRechercher.getText(), comboMatieres.getValue());
    }

    private void chargerMatieresDepuisBDD() {
        ObservableList<String> optionsMatieres = FXCollections.observableArrayList();
        optionsMatieres.add("Toutes");

        String sql = "SELECT nom FROM matieres ORDER BY nom ASC";
        java.sql.Connection conn = tg.univlome.epl.bypedu.DAOs.DatabaseConnection.getDatabase();
        
        if (conn == null) {
            System.err.println("Échec d'accès à DatabaseConnection !");
            return;
        }

        // try-with-resources appliqué uniquement sur Statement et ResultSet pour laisser le Singleton ouvert
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                optionsMatieres.add(rs.getString("nom"));
            }
            comboMatieres.setItems(optionsMatieres);
            comboMatieres.setValue("Toutes");
            System.out.println("Matières chargées dynamiquement depuis SQLite !");

        } catch (java.sql.SQLException ex) {
            System.err.println("Erreur lors du chargement des matières : " + ex.getMessage());
        }
    }
}