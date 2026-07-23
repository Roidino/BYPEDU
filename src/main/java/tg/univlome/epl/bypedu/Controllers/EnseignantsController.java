package tg.univlome.epl.bypedu.Controllers;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import javafx.util.Callback;
import tg.univlome.epl.bypedu.Controllers.DialogUtils.TypeAlerte;
import tg.univlome.epl.bypedu.DAOs.EnseignantsDAO;
import tg.univlome.epl.bypedu.models.Enseignants;

/**
 * Contrôleur de la page "Gestion des Enseignants".
 * Le formulaire d'ajout / modification utilise {@link FormulaireShell} afin
 * de garder exactement le même style visuel que "Ajouter un étudiant".
 */
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

    private Window fenetre() {
        return tableEnseignants.getScene() != null ? tableEnseignants.getScene().getWindow() : null;
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
                            boolean sauvegarde = afficherFormulaireEnseignant(e);
                            if (sauvegarde) {
                                chargerDonneesDepuisBDD();
                            }
                        }
                    });

                    // Action : SUPPRIMER
                    btnDelete.setOnAction(event -> {
                        Enseignants e = getTableRow().getItem();
                        if (e == null) return;

                        boolean confirme = DialogUtils.confirmerSuppression(fenetre(),
                            "Vous allez retirer l'affectation de :",
                            e.getNom().toUpperCase() + " " + e.getPrenom());

                        if (confirme) {
                            boolean confirmationSuppression = enseignantsDAO.delete(e.getId());
                            if (confirmationSuppression) {
                                chargerDonneesDepuisBDD();
                                DialogUtils.afficherAlerte(fenetre(), "Succès",
                                    "Affectation retirée avec succès.", TypeAlerte.SUCCES);
                            } else {
                                DialogUtils.afficherAlerte(fenetre(), "Erreur",
                                    "Impossible de supprimer cette affectation.", TypeAlerte.ERREUR);
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

    /**
     * Affiche le formulaire d'ajout / modification d'un enseignant, avec le
     * même style visuel que le formulaire "Ajouter un étudiant".
     * Retourne {@code true} si l'enregistrement a réussi.
     */
    private boolean afficherFormulaireEnseignant(Enseignants enseignantAModifier) {
        boolean estModification = (enseignantAModifier != null);

        FormulaireShell shell = new FormulaireShell(
            estModification ? "Modifier l'enseignant" : "Ajouter un enseignant",
            estModification ? "Modifiez les informations de l'enseignant" : "Remplissez les informations de l'enseignant",
            estModification ? "Modifier" : "Sauvegarder");

        TextField txtNom = new TextField();
        txtNom.setPromptText("Ex: Fontaine");
        TextField txtPrenom = new TextField();
        txtPrenom.setPromptText("Ex: Michel");
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Ex: michel.fontaine@school.com");
        TextField txtTelephone = new TextField();
        txtTelephone.setPromptText("Ex: 90 00 00 00");

        ComboBox<String> comboMatiereForm = new ComboBox<>();
        ObservableList<String> matieresForm = FXCollections.observableArrayList(comboMatieres.getItems());
        matieresForm.remove("Toutes");
        comboMatiereForm.setItems(matieresForm);
        comboMatiereForm.setPromptText("Choisir une matière");

        shell.champsSurLigne("Nom *", txtNom, "Prénom *", txtPrenom);
        shell.champ("Spécialité *", comboMatiereForm);
        shell.champsSurLigne("Email", txtEmail, "Téléphone", txtTelephone);

        if (estModification) {
            txtNom.setText(enseignantAModifier.getNom());
            txtPrenom.setText(enseignantAModifier.getPrenom());
            txtEmail.setText(enseignantAModifier.getEmail());
            txtTelephone.setText(enseignantAModifier.getTelephone());
            comboMatiereForm.setValue(enseignantAModifier.getMatiere());
        }

        shell.getBtnSauvegarder().setOnAction(e -> {
            shell.masquerErreur();
            if (txtNom.getText() == null || txtNom.getText().isBlank()) {
                shell.afficherErreur("Le nom est obligatoire.");
                return;
            }
            if (txtPrenom.getText() == null || txtPrenom.getText().isBlank()) {
                shell.afficherErreur("Le prénom est obligatoire.");
                return;
            }
            if (comboMatiereForm.getValue() == null) {
                shell.afficherErreur("La matière est obligatoire.");
                return;
            }

            Enseignants ent = estModification ? enseignantAModifier : new Enseignants();
            ent.setNom(txtNom.getText().trim());
            ent.setPrenom(txtPrenom.getText().trim());
            ent.setMatiere(comboMatiereForm.getValue());
            ent.setEmail(txtEmail.getText() != null ? txtEmail.getText().trim() : null);
            ent.setTelephone(txtTelephone.getText() != null ? txtTelephone.getText().trim() : null);

            boolean ok = estModification ? enseignantsDAO.update(ent) : enseignantsDAO.ajoute(ent);
            if (!ok) {
                shell.afficherErreur(estModification
                    ? "Impossible de modifier cet enseignant (email déjà utilisé ?)."
                    : "Impossible d'ajouter cet enseignant (l'enseignant fait déjà cours dans cette classe, ou l'email est déjà utilisé).");
                return;
            }

            shell.marquerSauvegarde();
            shell.fermer();
        });

        return shell.afficherEtAttendre(fenetre());
    }


    private void chargerDonneesDepuisBDD() {
        listeEnseignants.clear();
        listeEnseignants.addAll(enseignantsDAO.getAll());
    }

    @FXML
    private void AjouterEnseignant(ActionEvent event) {
        afficherFormulaireEnseignant(null);
        chargerDonneesDepuisBDD();
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
