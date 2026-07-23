package tg.univlome.epl.bypedu.Controllers;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Window;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tg.univlome.epl.bypedu.DAOs.ClasseDAO;
import tg.univlome.epl.bypedu.models.Classe;

public class ClassesController implements Initializable {
    @FXML private TableView<Classe> tableClasses;
    @FXML private TableColumn<Classe, String> colNom;
    @FXML private TableColumn<Classe, String> colNiveau;
    @FXML private TableColumn<Classe, String> colAffectations;
    @FXML private TableColumn<Classe, Void> colActions;

    private final ClasseDAO classeDAO = new ClasseDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colAffectations.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            classeDAO.getNombreAffectations(cell.getValue().getId()) + " matière(s) affectée(s)"));
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button modifier = new Button("Modifier");
            private final Button affecter = new Button("Matières & profs");
            private final Button supprimer = new Button("Supprimer");
            private final HBox actions = new HBox(8, modifier, affecter, supprimer);
            {
                modifier.setOnAction(event -> ouvrirFormulaire(getTableRow().getItem()));
                affecter.setOnAction(event -> ouvrirAffectations(getTableRow().getItem()));
                supprimer.setOnAction(event -> supprimer(getTableRow().getItem()));
                modifier.getStyleClass().add("table-action-button");
                affecter.getStyleClass().add("table-action-button-primary");
                supprimer.getStyleClass().add("table-action-button-danger");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow().getItem() == null ? null : actions);
                setText(null);
            }
        });
        charger();
    }

    private void charger() {
        tableClasses.setItems(FXCollections.observableArrayList(classeDAO.getAll()));
        tableClasses.setRowFactory(table -> {
            javafx.scene.control.TableRow<Classe> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1 && !(event.getTarget() instanceof Button)) {
                    ouvrirDetails(row.getItem());
                }
            });
            return row;
        });
    }

    private Window fenetre() {
        return tableClasses.getScene() == null ? null : tableClasses.getScene().getWindow();
    }

    @FXML private void ajouterClasse() { ouvrirFormulaire(null); }

    private void ouvrirFormulaire(Classe classeExistante) {
        boolean modification = classeExistante != null;
        FormulaireShell shell = new FormulaireShell(
            modification ? "Modifier la classe" : "Créer une classe",
            "Renseignez le niveau et les matières enseignées",
            modification ? "Modifier" : "Créer");
        TextField nom = new TextField();
        nom.setPromptText("Ex: 3e A");
        TextField niveau = new TextField();
        niveau.setPromptText("Ex: Troisième");
        shell.champ("Nom de la classe *", nom);
        shell.champ("Niveau *", niveau);
        if (modification) {
            nom.setText(classeExistante.getNom());
            niveau.setText(classeExistante.getNiveau());
        }
        shell.getBtnSauvegarder().setOnAction(event -> {
            shell.masquerErreur();
            if (nom.getText().isBlank() || niveau.getText().isBlank()) {
                shell.afficherErreur("Le nom et le niveau sont obligatoires.");
                return;
            }
            Classe classe = modification ? classeExistante : new Classe(0, "", "");
            classe.setNom(nom.getText().trim());
            classe.setNiveau(niveau.getText().trim());
            boolean ok = modification ? classeDAO.update(classe) : classeDAO.ajoute(classe);
            if (!ok) {
                shell.afficherErreur("Impossible d'enregistrer la classe (nom déjà utilisé ?).");
                return;
            }
            if (classe.getId() <= 0) {
                shell.afficherErreur("La classe a été enregistrée, mais son identifiant est introuvable.");
                return;
            }
            shell.marquerSauvegarde();
            shell.fermer();

            // On attend que la fenêtre du formulaire soit bien fermée avant d'ouvrir
            // celle des affectations : ouvrir une nouvelle fenêtre modale dans le même
            // tour d'événement que la fermeture de la précédente peut la faire
            // apparaître vide/blanche tant qu'aucun redessin n'est déclenché.
            final int classeId = classe.getId();
            final Classe classeCreeeOuModifiee = classe;
            Platform.runLater(() -> {
                Classe classeEnregistree = classeDAO.getById(classeId);
                ouvrirAffectations(classeEnregistree == null ? classeCreeeOuModifiee : classeEnregistree);
                charger();
            });
        });
        shell.afficherEtAttendre(fenetre());
    }

    private void ouvrirAffectations(Classe classe) {
        if (classe == null || classe.getId() <= 0) {
            DialogUtils.afficherAlerte(fenetre(), "Erreur", "Impossible d'identifier la classe à modifier.", DialogUtils.TypeAlerte.ERREUR);
            return;
        }
        Stage stage = creerFenetre("Matières et professeurs - " + classe.getNom(), 650, 680);
        VBox contenu = new VBox(12);
        contenu.setPadding(new Insets(22));
        Label titre = new Label("Affecter les matières à " + classe.getNom());
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #102A43;");
        Label aide = new Label("Pour chaque matière, sélectionnez le professeur qui intervient dans cette classe.");
        aide.setWrapText(true);
        aide.setStyle("-fx-text-fill: #627D98; -fx-font-size: 13px;");
        contenu.getChildren().addAll(titre, aide, new Separator());
        Map<Integer, ComboBox<ClasseDAO.TeacherOption>> choix = new LinkedHashMap<>();
        Map<Integer, String> matieres = classeDAO.getMatieres();
        Map<Integer, Integer> existantes = classeDAO.getAffectations(classe.getId());
        if (matieres.isEmpty()) {
            Label aucuneMatiere = new Label("Aucune matière n'est disponible. Ajoutez d'abord une matière avant de l'affecter à cette classe.");
            aucuneMatiere.setWrapText(true);
            aucuneMatiere.setStyle("-fx-text-fill: #627D98; -fx-font-size: 13px; -fx-padding: 12 0 12 0;");
            contenu.getChildren().add(aucuneMatiere);
        }
        for (Map.Entry<Integer, String> matiere : matieres.entrySet()) {
            ComboBox<ClasseDAO.TeacherOption> enseignants = new ComboBox<>();
            enseignants.setPrefWidth(310);
            enseignants.setPromptText("Aucun professeur sélectionné");
            enseignants.getItems().addAll(classeDAO.getEnseignantsParMatiere(matiere.getKey()));
            Integer enseignantId = existantes.get(matiere.getKey());
            if (enseignantId != null) {
                enseignants.getItems().stream().filter(option -> option.id() == enseignantId).findFirst().ifPresent(enseignants::setValue);
            }
            Label nomMatiere = new Label(matiere.getValue());
            nomMatiere.setMinWidth(190);
            nomMatiere.setStyle("-fx-font-weight: bold; -fx-text-fill: #243B53;");
            HBox ligne = new HBox(14, nomMatiere, enseignants);
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.setPadding(new Insets(9, 12, 9, 12));
            ligne.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 7; -fx-border-color: #E2E8F0; -fx-border-radius: 7;");
            contenu.getChildren().add(ligne);
            choix.put(matiere.getKey(), enseignants);
        }
        Button enregistrer = new Button("Enregistrer les affectations");
        enregistrer.getStyleClass().add("primary-button");
        Button fermer = new Button("Fermer");
        fermer.getStyleClass().add("table-action-button");
        HBox actions = new HBox(10, fermer, enregistrer);
        actions.setAlignment(Pos.CENTER_RIGHT);
        contenu.getChildren().add(actions);
        fermer.setOnAction(event -> stage.close());
        enregistrer.setOnAction(event -> {
            Map<Integer, Integer> affectations = new LinkedHashMap<>();
            choix.forEach((matiereId, enseignants) -> {
                if (enseignants.getValue() != null) affectations.put(matiereId, enseignants.getValue().id());
            });
            if (!classeDAO.remplacerAffectations(classe.getId(), affectations)) {
                DialogUtils.afficherAlerte(stage, "Erreur", "Impossible d'enregistrer les affectations.", DialogUtils.TypeAlerte.ERREUR);
                return;
            }
            stage.close();
            // Même précaution que pour la création : on laisse cette fenêtre se fermer
            // avant d'en ouvrir une nouvelle, sinon celle-ci s'affiche blanche.
            Platform.runLater(() -> {
                charger();
                ouvrirDetails(classe);
            });
        });
        ScrollPane scroll = new ScrollPane(contenu);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F4F7FB;");
        Scene scene = new Scene(scroll);
        scene.getStylesheets().add(getClass().getResource("/tg/univlome/epl/bypedu/styles/accueil.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void ouvrirDetails(Classe classe) {
        if (classe == null) return;
        Stage stage = creerFenetre("Détails - " + classe.getNom(), 560, 520);
        VBox contenu = new VBox(14);
        contenu.setPadding(new Insets(24));
        Label titre = new Label(classe.getNom());
        titre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #102A43;");
        Label niveau = new Label("Niveau : " + classe.getNiveau());
        niveau.setStyle("-fx-text-fill: #627D98; -fx-font-size: 13px;");
        contenu.getChildren().addAll(titre, niveau, new Separator());
        List<ClasseDAO.Affectation> affectations = classeDAO.getAffectationsDetails(classe.getId());
        if (affectations.isEmpty()) {
            contenu.getChildren().add(new Label("Aucune matière n'est encore affectée à cette classe."));
        } else {
            for (ClasseDAO.Affectation affectation : affectations) {
                Label matiere = new Label(affectation.matiere());
                matiere.setStyle("-fx-font-weight: bold; -fx-text-fill: #243B53;");
                Label enseignant = new Label(affectation.enseignant() == null ? "Professeur non affecté" : affectation.enseignant());
                enseignant.setStyle("-fx-text-fill: #0B7285;");
                VBox bloc = new VBox(4, matiere, enseignant);
                bloc.setPadding(new Insets(11, 14, 11, 14));
                bloc.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 7; -fx-border-color: #E2E8F0; -fx-border-radius: 7;");
                contenu.getChildren().add(bloc);
            }
        }
        Button modifier = new Button("Modifier les matières et professeurs");
        modifier.getStyleClass().add("primary-button");
        Button fermer = new Button("Fermer");
        fermer.getStyleClass().add("table-action-button");
        HBox actions = new HBox(10, fermer, modifier);
        actions.setAlignment(Pos.CENTER_RIGHT);
        contenu.getChildren().add(actions);
        fermer.setOnAction(event -> stage.close());
        modifier.setOnAction(event -> {
            stage.close();
            // Idem : on attend la fermeture effective de la fenêtre de détails avant
            // d'ouvrir celle des affectations.
            Platform.runLater(() -> ouvrirAffectations(classe));
        });
        ScrollPane scroll = new ScrollPane(contenu);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F4F7FB;");
        Scene scene = new Scene(scroll);
        scene.getStylesheets().add(getClass().getResource("/tg/univlome/epl/bypedu/styles/accueil.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private Stage creerFenetre(String titre, double largeur, double hauteur) {
        Stage stage = new Stage();
        stage.setTitle(titre);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(fenetre());
        stage.setWidth(largeur);
        stage.setHeight(hauteur);
        stage.setMinWidth(520);
        stage.setMinHeight(400);
        return stage;
    }

    private void supprimer(Classe classe) {
        if (classe == null) return;
        if (DialogUtils.confirmerSuppression(fenetre(), "Vous allez supprimer la classe :", classe.getNom())) {
            if (classeDAO.delete(classe.getId())) {
                charger();
            } else {
                DialogUtils.afficherAlerte(fenetre(), "Suppression impossible",
                    "La classe n'a pas pu être supprimée. Les matières, professeurs et élèves sont conservés ; "
                    + "les élèves sont simplement détachés de cette classe.",
                    DialogUtils.TypeAlerte.ERREUR);
            }
        }
    }
}