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
    private TableColumn<Enseignants, String> colPrenom;
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

    private final ObservableList<Enseignants> listeEnseignants = FXCollections.observableArrayList();
    private final EnseignantsDAO enseignantsDAO = new EnseignantsDAO();
    private FilteredList<Enseignants> donneesFiltrees;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        chargerMatieresDepuisBDD();

        donneesFiltrees = new FilteredList<>(listeEnseignants, p -> true);

        txtRechercher.textProperty().addListener((observable, oldValue, newValue) -> {
            appliquerFiltre(newValue, comboMatieres.getValue());
        });

        comboMatieres.valueProperty().addListener((observable, oldValue, newValue) -> {
            appliquerFiltre(txtRechercher.getText(), newValue);
        });

        SortedList<Enseignants> donneesTriees = new SortedList<>(donneesFiltrees);
        donneesTriees.comparatorProperty().bind(tableEnseignants.comparatorProperty());
        
        tableEnseignants.setItems(donneesTriees);
        tableEnseignants.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ajouterBoutonsActions();
        chargerDonneesDepuisBDD(); 
    }

    private void appliquerFiltre(String texteRecherche, String matiereSelectionnee) {
        donneesFiltrees.setPredicate(enseignant -> {
            boolean correspondTexte = true;
            if (texteRecherche != null && !texteRecherche.isEmpty()) {
                String minuscules = texteRecherche.toLowerCase();
                correspondTexte = (enseignant.getNom() != null && enseignant.getNom().toLowerCase().contains(minuscules))
                        || (enseignant.getPrenom() != null && enseignant.getPrenom().toLowerCase().contains(minuscules))
                        || (enseignant.getEmail() != null && enseignant.getEmail().toLowerCase().contains(minuscules));
            }

            boolean correspondMatiere = true;
            if (matiereSelectionnee != null && !matiereSelectionnee.equals("Toutes")) {
                correspondMatiere = matiereSelectionnee.equals(enseignant.getMatiere());
            }

            return correspondTexte && correspondMatiere;
        });
    }

    private void ajouterBoutonsActions() {
        Callback<TableColumn<Enseignants, Void>, TableCell<Enseignants, Void>> cellFactory = (param) -> {
            return new TableCell<Enseignants, Void>() {
                
                private final Button btnEdit = new Button("Modifier");
                private final Button btnDelete = new Button("Supprimer");
                private final HBox managebtn = new HBox(btnEdit, btnDelete);

                {
                    btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
                    btnDelete.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
                    
                    managebtn.setStyle("-fx-alignment: center;");
                    managebtn.setSpacing(10);

                    // Action : MODIFIER
                    btnEdit.setOnAction(event -> {
                        Enseignants e = getTableRow().getItem();
                        if (e != null) {
                            // Clonage pour éviter les modifications live parasites
                            Enseignants copieEnseignant = new Enseignants();
                            copieEnseignant.setId(e.getId());
                            copieEnseignant.setNom(e.getNom());
                            copieEnseignant.setPrenom(e.getPrenom());
                            copieEnseignant.setEmail(e.getEmail());
                            copieEnseignant.setTelephone(e.getTelephone());
                            copieEnseignant.setMatiere(e.getMatiere());
                            copieEnseignant.setClasse(e.getClasse());

                            Optional<Enseignants> resultat = afficherFormulaireEnseignant(copieEnseignant);
                            resultat.ifPresent(enseignantModifie -> {
                                boolean misAJour = enseignantsDAO.update(enseignantModifie);
                                if (misAJour) {
                                    chargerDonneesDepuisBDD(); 
                                    System.out.println("Enseignant mis à jour : " + enseignantModifie.getNom());
                                } else {
                                    Alert errorAlert = new Alert(AlertType.ERROR, "Impossible de modifier cet enseignant.", ButtonType.OK);
                                    errorAlert.showAndWait();
                                }
                            });
                        }
                    });

                    // Action : SUPPRIMER
                    btnDelete.setOnAction(event -> {
                        Enseignants e = getTableRow().getItem();
                        if (e == null) return; 
                        
                        Alert alert = new Alert(AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation de suppression");
                        alert.setHeaderText("Retirer l'affectation de classe ?");
                        alert.setContentText("Voulez-vous retirer " + e.getNom().toUpperCase() + " " + e.getPrenom() + " de la classe " + e.getClasse() + " ?");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            boolean confirmationSuppression = enseignantsDAO.deleteAffectation(e.getId(), e.getClasse());
                            if (confirmationSuppression) {
                                chargerDonneesDepuisBDD(); 
                            } else {
                                Alert errorAlert = new Alert(AlertType.ERROR, "Impossible de supprimer cette affectation.", ButtonType.OK);
                                errorAlert.showAndWait();
                            }
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        setGraphic(managebtn);
                    }
                    setText(null);
                }
            };
        };
        colActions.setCellFactory(cellFactory);
    }

    private Optional<Enseignants> afficherFormulaireEnseignant(Enseignants enseignantAModifier) {
        boolean estModification = (enseignantAModifier != null);
        
        Dialog<Enseignants> dialog = new Dialog<>();
        dialog.setTitle(estModification ? "Modifier l'enseignant" : "Ajouter une affectation");

        ButtonType boutonTypeSauvegarder = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(boutonTypeSauvegarder, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField txtNom = new TextField();
        TextField txtPrenom = new TextField();
        TextField txtEmail = new TextField();
        TextField txtTelephone = new TextField();
        
        ComboBox<String> comboMatiereForm = new ComboBox<>();
        ObservableList<String> matieresForm = FXCollections.observableArrayList(comboMatieres.getItems());
        matieresForm.remove("Toutes");
        comboMatiereForm.setItems(matieresForm);

        ComboBox<String> comboClasseForm = new ComboBox<>();
        comboClasseForm.setItems(obtenirListeClassesBDD());

        grid.add(new Label("Nom :"), 0, 0); grid.add(txtNom, 1, 0);
        grid.add(new Label("Prénom :"), 0, 1); grid.add(txtPrenom, 1, 1);
        grid.add(new Label("Matière assignée :"), 0, 2); grid.add(comboMatiereForm, 1, 2);
        grid.add(new Label("Classe assignée :"), 0, 3); grid.add(comboClasseForm, 1, 3);
        grid.add(new Label("Email :"), 0, 4); grid.add(txtEmail, 1, 4);
        grid.add(new Label("Téléphone :"), 0, 5); grid.add(txtTelephone, 1, 5);

        if (estModification) {
            txtNom.setText(enseignantAModifier.getNom()); 
            txtPrenom.setText(enseignantAModifier.getPrenom()); 
            txtEmail.setText(enseignantAModifier.getEmail());
            txtTelephone.setText(enseignantAModifier.getTelephone());
            comboMatiereForm.setValue(enseignantAModifier.getMatiere());
            comboClasseForm.setValue(enseignantAModifier.getClasse());
            comboClasseForm.setDisable(true); 
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == boutonTypeSauvegarder) {
                Enseignants e = estModification ? enseignantAModifier : new Enseignants();
                e.setNom(txtNom.getText());
                e.setPrenom(txtPrenom.getText());
                e.setMatiere(comboMatiereForm.getValue());
                e.setClasse(comboClasseForm.getValue()); 
                e.setEmail(txtEmail.getText());
                e.setTelephone(txtTelephone.getText());
                return e;
            }
            return null;
        });

        return dialog.showAndWait();
    }

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
                System.err.println("Erreur chargement classes : " + ex.getMessage());
            }
        }
        return liste;
    }

    private void chargerDonneesDepuisBDD() {
        listeEnseignants.clear();
        listeEnseignants.addAll(enseignantsDAO.getAll());
    }

    @FXML
    private void AjouterEnseignant(ActionEvent event) {
        Optional<Enseignants> resultat = afficherFormulaireEnseignant(null);
        resultat.ifPresent(nouvelEnseignant -> {
            boolean insere = enseignantsDAO.ajoute(nouvelEnseignant); 
            if (insere) {
                chargerDonneesDepuisBDD(); 
            } else {
                Alert errorAlert = new Alert(AlertType.ERROR, "Erreur lors de l'affectation (L'enseignant fait déjà cours dans cette classe).", ButtonType.OK);
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
        if (conn == null) return;

        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                optionsMatieres.add(rs.getString("nom"));
            }
            comboMatieres.setItems(optionsMatieres);
            comboMatieres.setValue("Toutes");
        } catch (java.sql.SQLException ex) {
            System.err.println("Erreur chargement matières : " + ex.getMessage());
        }
    }
}