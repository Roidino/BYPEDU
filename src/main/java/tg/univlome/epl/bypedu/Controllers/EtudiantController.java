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

            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel  = new Button("Supprimer");

            {
                btnEdit.setStyle(
                    "-fx-background-color:#2563EB;" +
                    "-fx-text-fill:white;" +
                    "-fx-background-radius:6;" +
                    "-fx-cursor:hand;" +
                    "-fx-padding:5 8;" +
                    "fx-wrap-text: false;" +
                    "-fx-text-overrun: clip;");

                btnDel.setStyle(
                    "-fx-background-color:#EF4444;" +
                    "-fx-text-fill:white;" +
                    "-fx-background-radius:6;" +
                    "-fx-cursor:hand;" +
                    "-fx-padding:5 8;" +
                     "fx-wrap-text: false;" +
                    "-fx-text-overrun: clip;");
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
                getClass().getResource("/tg/univlome/epl/bypedu/formulaireEtudiant.fxml"));

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
        try {
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle("Confirmation");

            Label icone = new Label("⚠");
            icone.setStyle(
                "-fx-font-size: 36px;" +
                "-fx-text-fill: #F59E0B;");

            Label titre = new Label("Confirmer la suppression");
            titre.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #111827;");

            Label message = new Label(
                "Vous allez supprimer l'étudiant :");
            message.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #6B7280;");

            Label nomEtudiant = new Label(
                etudiant.getNom() + " " + etudiant.getPrenom());
            nomEtudiant.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #111827;");

            Label avertissement = new Label(
                "Cette action est irréversible.");
            avertissement.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #DC2626;" +
                "-fx-background-color: #FEF2F2;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: #FECACA;" +
                "-fx-border-radius: 6;" +
                "-fx-padding: 8 12;");

            Button btnAnnuler = new Button("Annuler");
            btnAnnuler.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #374151;" +
                "-fx-border-color: #D1D5DB;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 10 24;" +
                "-fx-font-size: 13px;");
            btnAnnuler.setOnAction(e -> stage.close());

            Button btnSupprimer = new Button("Oui, supprimer");
            btnSupprimer.setStyle(
                "-fx-background-color: #DC2626;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 24;" +
                "-fx-font-size: 13px;");

            btnSupprimer.setOnAction(e -> {
                stage.close();
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
            });

            btnSupprimer.setOnMouseEntered(e ->
                btnSupprimer.setStyle(
                    "-fx-background-color: #B91C1C;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 10 24;" +
                    "-fx-font-size: 13px;"));
            btnSupprimer.setOnMouseExited(e ->
                btnSupprimer.setStyle(
                    "-fx-background-color: #DC2626;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 10 24;" +
                    "-fx-font-size: 13px;"));

            HBox boutons = new HBox(12, btnAnnuler, btnSupprimer);
            boutons.setAlignment(Pos.CENTER_RIGHT);
            boutons.setStyle("-fx-padding: 8 0 0 0;");

            javafx.scene.control.Separator sep =
                new javafx.scene.control.Separator();
            sep.setStyle("-fx-background-color: #E5E7EB;");

            javafx.scene.layout.VBox layout =
                new javafx.scene.layout.VBox(16);
            layout.setStyle(
                "-fx-background-color: white;" +
                "-fx-padding: 30;");
            layout.setAlignment(Pos.CENTER);
            layout.setPrefWidth(400);

            javafx.scene.layout.VBox header =
                new javafx.scene.layout.VBox(8);
            header.setAlignment(Pos.CENTER);
            header.setStyle(
                "-fx-background-color: #FEF2F2;" +
                "-fx-padding: 20;" +
                "-fx-background-radius: 10;");
            header.getChildren().addAll(icone, titre);

            layout.getChildren().addAll(
                header,
                message,
                nomEtudiant,
                avertissement,
                sep,
                boutons
            );

            stage.setScene(new javafx.scene.Scene(layout));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String titre, String msg, Alert.AlertType type) {
        Stage owner = (Stage) tableau.getScene().getWindow();

        Stage stage = new Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setResizable(false);

        String icone, couleurFond, couleurTexte, couleurBouton;
        if (type == Alert.AlertType.INFORMATION) {
            icone         = "✓";
            couleurFond   = "#F0FDF4";
            couleurTexte  = "#166534";
            couleurBouton = "#16A34A";
        } else if (type == Alert.AlertType.ERROR) {
            icone         = "✕";
            couleurFond   = "#FEF2F2";
            couleurTexte  = "#991B1B";
            couleurBouton = "#DC2626";
        } else {
            icone         = "ℹ";
            couleurFond   = "#EFF6FF";
            couleurTexte  = "#1E40AF";
            couleurBouton = "#2563EB";
        }

        Label lblIcone = new Label(icone);
        lblIcone.setStyle(
            "-fx-font-size: 32px;" +
            "-fx-text-fill:" + couleurTexte + ";");

        Label lblTitre = new Label(titre);
        lblTitre.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #111827;");

        Label lblMsg = new Label(msg);
        lblMsg.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #6B7280;");
        lblMsg.setWrapText(true);
        lblMsg.setMaxWidth(300);
        lblMsg.setAlignment(Pos.CENTER);

        javafx.scene.layout.VBox header =
            new javafx.scene.layout.VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setMinWidth(340);
        header.setStyle(
            "-fx-background-color:" + couleurFond + ";" +
            "-fx-padding: 24;" +
            "-fx-background-radius: 10;");
        header.getChildren().addAll(lblIcone, lblTitre);

        Button btnOk = new Button("OK");
        btnOk.setMinWidth(120);
        btnOk.setStyle(
            "-fx-background-color:" + couleurBouton + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 40;" +
            "-fx-font-size: 13px;");
        btnOk.setOnAction(e -> stage.close());

        javafx.scene.layout.VBox layout =
            new javafx.scene.layout.VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setMinWidth(380);
        layout.setMinHeight(220);
        layout.setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 30;");
        layout.getChildren().addAll(header, lblMsg, btnOk);

        javafx.scene.Scene scene = new javafx.scene.Scene(layout);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.showAndWait();
    }
}