package tg.univlome.epl.bypedu.Controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import tg.univlome.epl.bypedu.DAOs.NotesDAO;
import tg.univlome.epl.bypedu.models.Notes;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la vue "Gestion des Notes".
 * IMPORTANT : tous les setCellValueFactory utilisent des lambdas directes
 * (SimpleStringProperty, SimpleObjectProperty) et NON PropertyValueFactory,
 * pour éviter les échecs silencieux liés au JPMS dans NetBeans/Maven.
 *
 * @author Terence PEKPELI
 */
public class NotesController implements Initializable {

    // ── Composants FXML ─────────────────────────────────────────────────────
    @FXML private ComboBox<String> cbTrimestre;
    @FXML private ComboBox<String> cbClasse;
    @FXML private TextField        txtRecherche;
    @FXML private TableView<Notes> tableNotes;

    @FXML private Label lblBestMoyenne;
    @FXML private Label lblBestEtudiant;
    @FXML private Label lblMoyenneClasse;
    @FXML private Label lblTauxReussite;

    // ── État interne ─────────────────────────────────────────────────────────
    private final NotesDAO               notesDAO     = new NotesDAO();
    private final ObservableList<Notes>  masterData   = FXCollections.observableArrayList();
    private       FilteredList<Notes>    filteredData;

    // ── Initialisation ───────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initialiserFiltres();
        rafraichirDonnees();
    }

    // ── Colonnes dynamiques ──────────────────────────────────────────────────

    private void genererColonnesDynamiques() {
        tableNotes.getColumns().clear();

        // ── 1. ÉTUDIANT ──────────────────────────────────────────────────────
        TableColumn<Notes, String> colEtudiant = new TableColumn<>("ÉTUDIANT");
        colEtudiant.setPrefWidth(180);
        // Lambda directe → jamais null, indépendante du JPMS
        colEtudiant.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getNomEtudiant()));
        colEtudiant.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold;"
                           + " -fx-padding: 0 10 0 10;");
                }
            }
        });
        tableNotes.getColumns().add(colEtudiant);

        // ── 2. CLASSE ────────────────────────────────────────────────────────
        TableColumn<Notes, String> colClasse = new TableColumn<>("CLASSE");
        colClasse.setPrefWidth(130);
        colClasse.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getClasse()));
        colClasse.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    setStyle("-fx-text-fill: #64748b; -fx-padding: 0 10 0 10;");
                }
            }
        });
        tableNotes.getColumns().add(colClasse);

        // ── 3. Une colonne par matière ────────────────────────────────────────
        List<String> listeMatieres = notesDAO.getListeDesMatieres();
        for (String nomMatiere : listeMatieres) {
            TableColumn<Notes, Double> colMat = new TableColumn<>(nomMatiere.toUpperCase());
            colMat.setPrefWidth(110);

            colMat.setCellValueFactory(cd -> {
                Double note = cd.getValue().getNotesParCours().get(nomMatiere);
                return new SimpleObjectProperty<>(note);
            });

            colMat.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("–");
                        setStyle("-fx-text-fill: #94a3b8; -fx-alignment: CENTER;");
                    } else {
                        setText(String.format("%.0f", item));
                        if      (item >= 14) setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        else if (item >= 10) setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        else                 setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                }
            });
            tableNotes.getColumns().add(colMat);
        }

        // ── 4. MOYENNE ───────────────────────────────────────────────────────
        TableColumn<Notes, Double> colMoyenne = new TableColumn<>("MOYENNE");
        colMoyenne.setPrefWidth(110);
        colMoyenne.setCellValueFactory(cd ->
                new SimpleObjectProperty<>(cd.getValue().getMoyenne()));
        colMoyenne.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("–");
                    setStyle("-fx-text-fill: #94a3b8; -fx-alignment: CENTER;");
                } else {
                    setText(String.format("%.1f/20", item));
                    if      (item >= 14) setStyle("-fx-font-weight: bold; -fx-text-fill: #10b981; -fx-alignment: CENTER;");
                    else if (item >= 10) setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-alignment: CENTER;");
                    else                 setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-alignment: CENTER;");
                }
            }
        });
        tableNotes.getColumns().add(colMoyenne);

        // ── 5. TENDANCE ──────────────────────────────────────────────────────
        TableColumn<Notes, String> colTendance = new TableColumn<>("TENDANCE");
        colTendance.setPrefWidth(90);
        colTendance.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getTendance()));
        colTendance.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else switch (item) {
                    case "HAUSSE" -> { setText("↗"); setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 16px; -fx-alignment: CENTER;"); }
                    case "BAISSE" -> { setText("↘"); setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 16px; -fx-alignment: CENTER;"); }
                    default       -> { setText("→"); setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 16px; -fx-alignment: CENTER;"); }
                }
            }
        });
        tableNotes.getColumns().add(colTendance);
    }

    // ── Filtres et données ───────────────────────────────────────────────────

    private void initialiserFiltres() {
        cbTrimestre.getItems().addAll("Trimestre 1", "Trimestre 2", "Trimestre 3");
        cbTrimestre.setValue("Trimestre 1");

        cbClasse.getItems().setAll(notesDAO.getAllClasses());
        cbClasse.setValue("Toutes les classes");

        cbTrimestre.valueProperty().addListener((obs, o, n) -> rafraichirDonnees());
        cbClasse.valueProperty().addListener((obs, o, n) -> rafraichirDonnees());

        filteredData = new FilteredList<>(masterData, p -> true);
        txtRecherche.textProperty().addListener((obs, o, newVal) -> {
            filteredData.setPredicate(row -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return row.getNomEtudiant().toLowerCase().contains(newVal.toLowerCase());
            });
            calculerStatistiques(filteredData);
        });

        tableNotes.setItems(filteredData);
    }

    private void rafraichirDonnees() {
        String trimCode = switch (cbTrimestre.getValue() != null ? cbTrimestre.getValue() : "") {
            case "Trimestre 2" -> "T2";
            case "Trimestre 3" -> "T3";
            default            -> "T1";
        };

        genererColonnesDynamiques();

        List<Notes> rows = notesDAO.getNotesSynthese(trimCode, cbClasse.getValue());
        masterData.setAll(rows);
        calculerStatistiques(masterData);
    }

    // ── Statistiques ─────────────────────────────────────────────────────────

    private void calculerStatistiques(List<Notes> liste) {
        if (liste.isEmpty()) {
            lblBestMoyenne.setText("0.0/20");
            lblBestEtudiant.setText("–");
            lblMoyenneClasse.setText("0.0/20");
            lblTauxReussite.setText("0%");
            return;
        }
        double maxMoy = -1, sommeMoy = 0;
        String bestStudent = "";
        int admis = 0;

        for (Notes row : liste) {
            sommeMoy += row.getMoyenne();
            if (row.getMoyenne() > maxMoy) {
                maxMoy = row.getMoyenne();
                bestStudent = row.getNomEtudiant() + " – " + row.getClasse();
            }
            if (row.getMoyenne() >= 10.0) admis++;
        }

        lblBestMoyenne.setText(String.format("%.1f/20", maxMoy));
        lblBestEtudiant.setText(bestStudent);
        lblMoyenneClasse.setText(String.format("%.1f/20", sommeMoy / liste.size()));
        lblTauxReussite.setText(String.format("%.0f%%", (double) admis / liste.size() * 100));
    }

    // ── Export CSV/PDF ───────────────────────────────────────────────────────

    @FXML
    private void exporterPdf() {
        if (filteredData == null || filteredData.isEmpty()) {
            afficherAlerte("Aucune donnée à exporter.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport");
        fc.setInitialFileName("notes_" + LocalDate.now() + ".csv");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));

        File fichier = fc.showSaveDialog(tableNotes.getScene().getWindow());
        if (fichier == null) return;

        List<String> matieres = notesDAO.getListeDesMatieres();

        try (PrintWriter pw = new PrintWriter(new FileWriter(fichier))) {
            pw.print("Étudiant;Classe;");
            for (String m : matieres) pw.print(m + ";");
            pw.println("Moyenne;Tendance");

            for (Notes row : filteredData) {
                pw.print(row.getNomEtudiant() + ";");
                pw.print(row.getClasse() + ";");
                for (String m : matieres) {
                    Double val = row.getNotesParCours().get(m);
                    pw.print((val != null ? String.format("%.1f", val) : "–") + ";");
                }
                pw.print(String.format("%.2f", row.getMoyenne()) + ";");
                pw.println(row.getTendance());
            }
            afficherInfo("Export réussi !\n" + fichier.getAbsolutePath());
        } catch (IOException e) {
            afficherAlerte("Erreur lors de l'export : " + e.getMessage());
        }
    }

    // ── Utilitaires ──────────────────────────────────────────────────────────

    private void afficherAlerte(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("BYPEDU – Notes"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void afficherInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("BYPEDU – Notes"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}