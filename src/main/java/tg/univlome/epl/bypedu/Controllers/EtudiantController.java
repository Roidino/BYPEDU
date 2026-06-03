package tg.univlome.epl.bypedu.Controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import tg.univlome.epl.bypedu.DAOs.EtudiantDAO;
import tg.univlome.epl.bypedu.models.Etudiant;

public class EtudiantController implements Initializable {

    @FXML private TableView<Etudiant>              tableau;
    @FXML private TableColumn<Etudiant, String>    colonneNom;
    @FXML private TableColumn<Etudiant, String>    colonnePrenom;
    @FXML private TableColumn<Etudiant, Integer>   colonneAge;
    @FXML private TableColumn<Etudiant, String>    colonneClasse;
    @FXML private TableColumn<Etudiant, Double>    colonneMoyenne;
    @FXML private TableColumn<Etudiant, String>    colonneNumero;
    @FXML private TableColumn<Etudiant, Void>      colonneActions;
    @FXML private TextField                        champRecherche;
    @FXML private ChoiceBox<String>                filtreClasse;

    private final EtudiantDAO dao = new EtudiantDAO();
    private ObservableList<Etudiant> tousLesEtudiants;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();
        chargerDonnees();
        configurerRecherche();
        configurerFiltreClasse();
    }

    private void configurerColonnes() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonnePrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colonneAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colonneClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colonneNumero.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colonneMoyenne.setCellValueFactory(new PropertyValueFactory<>("moyenne"));

        colonneMoyenne.setCellFactory(col -> new TableCell<Etudiant, Double>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(val + "/20");
                    String couleur = val >= 15 ? "#16A34A"
                                   : val >= 10 ? "#2563EB"
                                   : "#DC2626";
                    setStyle("-fx-text-fill:" + couleur + "; -fx-font-weight:bold;");
                }
            }
        });

        colonneActions.setCellFactory(col -> new TableCell<Etudiant, Void>() {

            private final Button btnEdit = new Button("✏ Modifier");
            private final Button btnDel  = new Button("🗑 Supprimer");

            {
                btnEdit.setStyle(
                    "-fx-background-color:#2563EB;" +
                    "-fx-text-fill:white;" +
                    "-fx-background-radius:6;" +
                    "-fx-cursor:hand;" +
                    "-fx-padding:5 8;");

                btnDel.setStyle(
                    "-fx-background-color:#EF4444;" +
                    "-fx-text-fill:white;" +
                    "-fx-background-radius:6;" +
                    "-fx-cursor:hand;" +
                    "-fx-padding:5 8;");
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    btnEdit.setOnAction(e -> {
                        int i = getIndex();
                        if (i >= 0 && i < getTableView().getItems().size())
                            ouvrirFormulaire(getTableView().getItems().get(i));
                    });
                    btnDel.setOnAction(e -> {
                        int i = getIndex();
                        if (i >= 0 && i < getTableView().getItems().size())
                            confirmerSuppression(getTableView().getItems().get(i));
                    });
                    HBox box = new HBox(6, btnEdit, btnDel);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void chargerDonnees() {
        List<Etudiant> liste = dao.getAll();
        tousLesEtudiants = FXCollections.observableArrayList(liste);
        tableau.setItems(tousLesEtudiants);
    }

    private void configurerRecherche() {
        if (champRecherche == null) return;

        FilteredList<Etudiant> listeFiltree =
            new FilteredList<>(tousLesEtudiants, e -> true);

        champRecherche.textProperty().addListener((obs, ancien, nouveau) -> {
            listeFiltree.setPredicate(etudiant -> {
                if (nouveau == null || nouveau.isBlank()) return true;
                String f = nouveau.toLowerCase();
                return etudiant.getNom().toLowerCase().contains(f)
                    || etudiant.getPrenom().toLowerCase().contains(f);
            });
        });

        tableau.setItems(listeFiltree);
    }

    private void configurerFiltreClasse() {
        if (filtreClasse == null) return;

        ObservableList<String> classes =
            FXCollections.observableArrayList("Toutes les classes");

        tousLesEtudiants.stream()
            .map(Etudiant::getClasse)
            .filter(c -> c != null && !c.isBlank())
            .distinct()
            .sorted()
            .forEach(classes::add);

        filtreClasse.setItems(classes);
        filtreClasse.setValue("Toutes les classes");

        filtreClasse.valueProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau == null || nouveau.equals("Toutes les classes")) {
                tableau.setItems(tousLesEtudiants);
            } else {
                tableau.setItems(
                    tousLesEtudiants.filtered(e -> nouveau.equals(e.getClasse())));
            }
        });
    }

    @FXML
    public void ajouterEtudiant() {
        ouvrirFormulaire(null);
    }

    private void ouvrirFormulaire(Etudiant etudiant) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/tg/univlome/epl/bypedu/formulaireEtudiant.fxml"));

            javafx.scene.Parent root = loader.load();

            FormulaireEtudiantController ctrl = loader.getController();
            ctrl.setEtudiant(etudiant);

            Stage stage = new Stage();
            stage.setTitle(etudiant == null
                ? "Ajouter un étudiant"
                : "Modifier " + etudiant.getNom());
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            if (ctrl.isSauvegarde()) {
                chargerDonnees();
                configurerRecherche();
                configurerFiltreClasse();
            }

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void confirmerSuppression(Etudiant etudiant) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer " + etudiant.getNom()
            + " " + etudiant.getPrenom() + " ?");
        confirm.setContentText(
            "Cette action est irréversible.\n" +
            "Toutes les notes associées seront également supprimées.");

        ButtonType btnOui = new ButtonType("Oui, supprimer",
                                           ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNon = new ButtonType("Annuler",
                                           ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnOui, btnNon);

        confirm.showAndWait().ifPresent(reponse -> {
            if (reponse == btnOui) {
                boolean ok = dao.delete(etudiant.getId());
                if (ok) {
                    tousLesEtudiants.remove(etudiant);
                    showAlert("Succès",
                        etudiant.getNom() + " supprimé avec succès.",
                        Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur",
                        "Impossible de supprimer cet étudiant.",
                        Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String titre, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(titre);
        a.setContentText(msg);
        a.showAndWait();
    }
}