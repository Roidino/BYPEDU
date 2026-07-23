package tg.univlome.epl.bypedu.Controllers;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
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
import javafx.stage.Window;
import javafx.util.Callback;
import tg.univlome.epl.bypedu.Controllers.DialogUtils.TypeAlerte;
import tg.univlome.epl.bypedu.DAOs.NoteDAO;
import tg.univlome.epl.bypedu.models.Note;

/**
 * Contrôleur de la page "Gestion des Notes".
 * <p>
 * Le formulaire d'ajout / modification utilise {@link FormulaireShell} afin
 * de garder exactement le même style visuel que "Ajouter un étudiant".
 * <p>
 * Les cartes de résumé (Meilleure Moyenne, Moyenne de Classe, Taux de
 * Réussite) sont désormais calculées à partir des notes actuellement
 * affichées dans le tableau (donc respectent la recherche, le filtre de
 * matière et le filtre de trimestre), au lieu d'une moyenne globale tous
 * trimestres et toutes matières confondus qui ne correspondait pas à ce que
 * l'utilisateur voyait à l'écran.
 *
 * @author BYPEDU
 */
public class NotesController implements Initializable {

    @FXML private TextField txtRechercher;
    @FXML private ComboBox<String> comboMatieres;
    @FXML private ComboBox<String> comboClasses;
    @FXML private ComboBox<String> comboTrimestres;
    @FXML private TableView<Note> tableNotes;
    @FXML private TableColumn<Note, String> colEtudiant;
    @FXML private TableColumn<Note, String> colClasse;
    @FXML private TableColumn<Note, String> colMatiere;
    @FXML private TableColumn<Note, Double> colNote;
    @FXML private TableColumn<Note, String> colTrimestre;
    @FXML private TableColumn<Note, Void> colActions;

    @FXML private Label lblMeilleureMoyenne;
    @FXML private Label lblMeilleureMoyenneNom;
    @FXML private Label lblMoyenneClasse;
    @FXML private Label lblTauxReussite;

    private final ObservableList<Note> listeNotes = FXCollections.observableArrayList();
    private final NoteDAO noteDAO = new NoteDAO();
    private FilteredList<Note> donneesFiltrees;

    private static final String[] TRIMESTRES = {"Trimestre 1", "Trimestre 2", "Trimestre 3"};

    /**
     * La colonne {@code trimestre} de la table {@code notes} est contrainte par
     * un CHECK à n'accepter que les codes 'T1', 'T2', 'T3' — pas les libellés
     * affichés à l'utilisateur ("Trimestre 1", ...). Sans cette conversion,
     * tout ajout ou modification de note échouait avec
     * "[SQLITE_CONSTRAINT_CHECK] A CHECK constraint failed".
     */
    private static final java.util.Map<String, String> TRIMESTRE_LABEL_VERS_CODE = java.util.Map.of(
        "Trimestre 1", "T1", "Trimestre 2", "T2", "Trimestre 3", "T3");
    private static final java.util.Map<String, String> TRIMESTRE_CODE_VERS_LABEL = java.util.Map.of(
        "T1", "Trimestre 1", "T2", "Trimestre 2", "T3", "Trimestre 3");

    /** Convertit un code stocké en base ("T1") en libellé affiché ("Trimestre 1"), inchangé si déjà un libellé. */
    private static String trimestreVersLabel(String code) {
        if (code == null) return null;
        return TRIMESTRE_CODE_VERS_LABEL.getOrDefault(code, code);
    }

    /** Convertit un libellé affiché ("Trimestre 1") en code stocké en base ("T1"), inchangé si déjà un code. */
    private static String trimestreVersCode(String label) {
        if (label == null) return null;
        return TRIMESTRE_LABEL_VERS_CODE.getOrDefault(label, label);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("etudiant"));
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colTrimestre.setCellValueFactory(new PropertyValueFactory<>("trimestre"));

        colNote.setCellFactory(col -> new TableCell<Note, Double>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(val + "/20");
                    String couleur = val >= 15 ? "#16A34A" : val >= 10 ? "#2563EB" : "#DC2626";
                    setStyle("-fx-text-fill:" + couleur + "; -fx-font-weight:bold;");
                }
            }
        });

        colTrimestre.setCellFactory(col -> new TableCell<Note, String>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : trimestreVersLabel(val));
            }
        });

        comboMatieres.getItems().add("Toutes");
        comboMatieres.getItems().addAll(noteDAO.getAllMatieres());
        comboMatieres.setValue("Toutes");

        comboClasses.getItems().add("Toutes les classes");
        comboClasses.getItems().addAll(noteDAO.getAllClasses());
        comboClasses.setValue("Toutes les classes");

        comboTrimestres.getItems().add("Tous");
        comboTrimestres.getItems().addAll(TRIMESTRES);
        comboTrimestres.setValue("Tous");

        donneesFiltrees = new FilteredList<>(listeNotes, n -> true);

        txtRechercher.textProperty().addListener((obs, oldV, newV) -> appliquerFiltre());
        comboMatieres.valueProperty().addListener((obs, oldV, newV) -> appliquerFiltre());
        comboClasses.valueProperty().addListener((obs, oldV, newV) -> appliquerFiltre());
        comboTrimestres.valueProperty().addListener((obs, oldV, newV) -> appliquerFiltre());

        SortedList<Note> donneesTriees = new SortedList<>(donneesFiltrees);
        donneesTriees.comparatorProperty().bind(tableNotes.comparatorProperty());
        tableNotes.setItems(donneesTriees);
        tableNotes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ajouterBoutonsActions();
        chargerDonnees();
    }

    private Window fenetre() {
        return tableNotes.getScene() != null ? tableNotes.getScene().getWindow() : null;
    }

    /**
     * Recalcule les cartes de résumé à partir des notes actuellement visibles
     * dans le tableau (donc en tenant compte de la recherche et des filtres
     * de matière / trimestre), pour que les chiffres affichés correspondent
     * toujours à ce que l'utilisateur voit dans le tableau juste au-dessus.
     */
    private void chargerResume() {
        List<Note> notesVisibles = donneesFiltrees;

        if (notesVisibles.isEmpty()) {
            lblMeilleureMoyenne.setText("0.0/20");
            lblMeilleureMoyenneNom.setText("Aucune donnée");
            lblMoyenneClasse.setText("0.0/20");
            lblTauxReussite.setText("0%");
            return;
        }

        // Moyenne par étudiant (sur l'ensemble des notes actuellement filtrées)
        Map<String, List<Note>> parEtudiant = notesVisibles.stream()
            .collect(Collectors.groupingBy(Note::getEtudiant));

        record MoyenneEtudiant(String etudiant, String classe, double moyenne) {}

        List<MoyenneEtudiant> moyennes = parEtudiant.entrySet().stream()
            .map(entree -> {
                double moyenne = entree.getValue().stream()
                    .mapToDouble(Note::getNote)
                    .average()
                    .orElse(0.0);
                String classe = entree.getValue().get(0).getClasse();
                return new MoyenneEtudiant(entree.getKey(), classe, moyenne);
            })
            .collect(Collectors.toList());

        MoyenneEtudiant meilleur = moyennes.stream()
            .max(Comparator.comparingDouble(MoyenneEtudiant::moyenne))
            .orElse(null);

        if (meilleur != null) {
            lblMeilleureMoyenne.setText(String.format("%.1f/20", meilleur.moyenne()));
            lblMeilleureMoyenneNom.setText(meilleur.etudiant()
                + (meilleur.classe() != null ? " - " + meilleur.classe() : ""));
        }

        double moyenneClasse = moyennes.stream()
            .mapToDouble(MoyenneEtudiant::moyenne)
            .average()
            .orElse(0.0);
        lblMoyenneClasse.setText(String.format("%.1f/20", moyenneClasse));

        long total = moyennes.size();
        long reussite = moyennes.stream().filter(m -> m.moyenne() >= 10).count();
        int taux = total > 0 ? (int) Math.round((reussite * 100.0) / total) : 0;
        lblTauxReussite.setText(taux + "%");
    }

    private void appliquerFiltre() {
        String texte = txtRechercher.getText();
        String matiere = comboMatieres.getValue();
        String classe = comboClasses.getValue();
        String trimestre = comboTrimestres.getValue();

        donneesFiltrees.setPredicate(n -> {
            boolean correspondTexte = true;
            if (texte != null && !texte.isEmpty()) {
                String f = texte.toLowerCase();
                correspondTexte = (n.getEtudiant() != null && n.getEtudiant().toLowerCase().contains(f))
                        || (n.getClasse() != null && n.getClasse().toLowerCase().contains(f));
            }
            boolean correspondMatiere = matiere == null || matiere.equals("Toutes") || matiere.equals(n.getMatiere());
            boolean correspondClasse = classe == null || classe.equals("Toutes les classes") || classe.equals(n.getClasse());
            boolean correspondTrimestre = trimestre == null || trimestre.equals("Tous")
                || trimestreVersCode(trimestre).equals(n.getTrimestre());
            return correspondTexte && correspondMatiere && correspondClasse && correspondTrimestre;
        });

        chargerResume();
    }

    private void chargerDonnees() {
        listeNotes.clear();
        listeNotes.addAll(noteDAO.getAll());
        chargerResume();
    }

    @FXML
    private void Recherche(ActionEvent event) {
        appliquerFiltre();
    }

    @FXML
    private void AjouterNote(ActionEvent event) {
        boolean ok = afficherFormulaireNote(null);
        if (ok) {
            chargerDonnees();
        }
    }

    private void ajouterBoutonsActions() {
        Callback<TableColumn<Note, Void>, TableCell<Note, Void>> cellFactory = (param) -> new TableCell<Note, Void>() {

            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox box = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
                box.setStyle("-fx-alignment: center;");

                btnEdit.setOnAction(event -> {
                    Note n = getTableRow().getItem();
                    if (n == null) return;
                    boolean ok = afficherFormulaireNote(n);
                    if (ok) {
                        chargerDonnees();
                    }
                });

                btnDelete.setOnAction(event -> {
                    Note n = getTableRow().getItem();
                    if (n == null) return;

                    boolean confirme = DialogUtils.confirmerSuppression(fenetre(),
                        "Vous allez supprimer la note de :",
                        n.getEtudiant() + " en " + n.getMatiere() + " (" + trimestreVersLabel(n.getTrimestre()) + ")");

                    if (confirme) {
                        if (noteDAO.delete(n.getId())) {
                            chargerDonnees();
                            DialogUtils.afficherAlerte(fenetre(), "Succès", "Note supprimée avec succès.", TypeAlerte.SUCCES);
                        } else {
                            DialogUtils.afficherAlerte(fenetre(), "Erreur", "Impossible de supprimer cette note.", TypeAlerte.ERREUR);
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
                    setGraphic(box);
                }
                setText(null);
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    /**
     * Affiche le formulaire d'ajout / modification d'une note, avec le même
     * style visuel que le formulaire "Ajouter un étudiant".
     */
    private boolean afficherFormulaireNote(Note noteExistante) {
        boolean estModification = (noteExistante != null);

        FormulaireShell shell = new FormulaireShell(
            estModification ? "Modifier la note" : "Ajouter une note",
            estModification ? "Modifiez la note de l'étudiant" : "Renseignez la note de l'étudiant",
            estModification ? "Modifier" : "Sauvegarder");

        ComboBox<String> cbEtudiant = new ComboBox<>();
        cbEtudiant.getItems().addAll(noteDAO.getAllEtudiants());
        cbEtudiant.setPromptText("Choisir un étudiant");

        ComboBox<String> cbMatiere = new ComboBox<>();
        cbMatiere.getItems().addAll(noteDAO.getAllMatieres());
        cbMatiere.setPromptText("Choisir une matière");

        TextField tfNote = new TextField();
        tfNote.setPromptText("Ex: 15.5");

        ComboBox<String> cbTrimestre = new ComboBox<>();
        cbTrimestre.getItems().addAll(TRIMESTRES);
        cbTrimestre.setPromptText("Choisir un trimestre");


        shell.champ("Étudiant *", cbEtudiant);
        shell.champsSurLigne("Matière *", cbMatiere, "Note /20 *", tfNote);
        shell.champ("Trimestre *", cbTrimestre);

        if (estModification) {
            cbEtudiant.setValue(noteExistante.getEtudiant());
            cbEtudiant.setDisable(true);
            cbMatiere.setValue(noteExistante.getMatiere());
            tfNote.setText(String.valueOf(noteExistante.getNote()));
            cbTrimestre.setValue(trimestreVersLabel(noteExistante.getTrimestre()));
        }

        shell.getBtnSauvegarder().setOnAction(e -> {
            shell.masquerErreur();

            if (cbEtudiant.getValue() == null) {
                shell.afficherErreur("L'étudiant est obligatoire.");
                return;
            }
            if (cbMatiere.getValue() == null) {
                shell.afficherErreur("La matière est obligatoire.");
                return;
            }
            if (cbTrimestre.getValue() == null) {
                shell.afficherErreur("Le trimestre est obligatoire.");
                return;
            }
            if (tfNote.getText() == null || tfNote.getText().isBlank()) {
                shell.afficherErreur("La note est obligatoire.");
                return;
            }

            double valeurNote;
            try {
                valeurNote = Double.parseDouble(tfNote.getText().replace(",", "."));
            } catch (NumberFormatException ex) {
                shell.afficherErreur("La note doit être un nombre.");
                return;
            }
            if (valeurNote < 0 || valeurNote > 20) {
                shell.afficherErreur("La note doit être comprise entre 0 et 20.");
                return;
            }

            Note n = estModification ? noteExistante : new Note();
            n.setEtudiantId(noteDAO.getEtudiantIdParNomComplet(cbEtudiant.getValue()));
            n.setEtudiant(cbEtudiant.getValue());
            n.setMatiereId(noteDAO.getMatiereIdParNom(cbMatiere.getValue()));
            n.setMatiere(cbMatiere.getValue());
            n.setNote(valeurNote);
            n.setTrimestre(trimestreVersCode(cbTrimestre.getValue()));

            boolean ok = estModification ? noteDAO.update(n) : noteDAO.ajoute(n);
            if (!ok) {
                shell.afficherErreur(estModification
                    ? "Impossible de modifier cette note."
                    : "Impossible d'ajouter cette note.");
                return;
            }

            shell.marquerSauvegarde();
            shell.fermer();
        });

        return shell.afficherEtAttendre(fenetre());
    }
}
