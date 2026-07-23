package tg.univlome.epl.bypedu.Controllers;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import tg.univlome.epl.bypedu.DAOs.CoursDAO;
import tg.univlome.epl.bypedu.DAOs.ClasseDAO;
import tg.univlome.epl.bypedu.DAOs.EnseignantsDAO;
import tg.univlome.epl.bypedu.DAOs.EtudiantDAO;
import tg.univlome.epl.bypedu.models.Enseignants;
import tg.univlome.epl.bypedu.models.Etudiant;
import tg.univlome.epl.bypedu.models.InscriptionRow;

/**
 * Contrôleur de la page d'accueil / tableau de bord.
 * Affiche des statistiques globales et les inscriptions récentes,
 * calculées à partir de la base de données.
 *
 * @author BYPEDU
 */
public class DashboardController implements Initializable {

    @FXML private Label lblNbEtudiants;
    @FXML private Label lblNbEnseignants;
    @FXML private Label lblNbCours;
    @FXML private Label lblNbClasses;
    @FXML private Label lblMoyenneGenerale;

    @FXML private TableView<InscriptionRow> tableInscriptions;
    @FXML private TableColumn<InscriptionRow, String> colNom;
    @FXML private TableColumn<InscriptionRow, String> colClasse;
    @FXML private TableColumn<InscriptionRow, String> colRole;
    @FXML private TableColumn<InscriptionRow, String> colDate;
    @FXML private TableColumn<InscriptionRow, String> colStatut;

    private final EtudiantDAO etudiantDAO = new EtudiantDAO();
    private final EnseignantsDAO enseignantsDAO = new EnseignantsDAO();
    private final CoursDAO coursDAO = new CoursDAO();
    private final ClasseDAO classeDAO = new ClasseDAO();

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<Etudiant> etudiants = etudiantDAO.getAll();
        List<Enseignants> enseignants = enseignantsDAO.getAll();

        if (lblNbEtudiants != null) lblNbEtudiants.setText(String.valueOf(etudiants.size()));
        if (lblNbEnseignants != null) lblNbEnseignants.setText(String.valueOf(enseignants.size()));
        if (lblNbCours != null) lblNbCours.setText(String.valueOf(coursDAO.getAll().size()));
        if (lblNbClasses != null) lblNbClasses.setText(String.valueOf(classeDAO.getAll().size()));

        if (lblMoyenneGenerale != null) {
            double moyenne = etudiants.stream()
                .mapToDouble(Etudiant::getMoyenne)
                .filter(m -> m > 0)
                .average()
                .orElse(0.0);
            lblMoyenneGenerale.setText(String.format("%.2f/20", moyenne));
        }

        initInscriptionsRecentes(etudiants);
    }

    private void initInscriptionsRecentes(List<Etudiant> etudiants) {
        if (tableInscriptions == null) return;

        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(statut.toUpperCase());
                boolean actif = statut.equalsIgnoreCase("ACTIF");
                badge.getStyleClass().addAll("badge", actif ? "badge-actif" : "badge-inactif");
                setGraphic(badge);
                setText(null);
            }
        });

        ObservableList<InscriptionRow> rows = FXCollections.observableArrayList();
        etudiants.stream()
            .filter(e -> e.getDate_inscription() != null)
            .sorted(Comparator.comparing(Etudiant::getDate_inscription).reversed())
            .limit(5)
            .forEach(e -> rows.add(new InscriptionRow(
                e.getNom() + " " + e.getPrenom(),
                e.getClasse() != null ? e.getClasse() : "Non affecté",
                "Élève",
                e.getDate_inscription().format(DATE_FORMAT),
                e.getStatus() != null ? e.getStatus() : "ACTIF"
            )));

        tableInscriptions.setItems(rows);
        if (rows.isEmpty()) {
            tableInscriptions.setPlaceholder(new Label("Aucune inscription récente."));
        }
    }
}
