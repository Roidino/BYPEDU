package tg.univlome.epl.bypedu.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import tg.univlome.epl.bypedu.Controllers.DialogUtils.TypeAlerte;
import tg.univlome.epl.bypedu.DAOs.CoursDAO;
import tg.univlome.epl.bypedu.models.Cours;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la page "Gestion des Cours".
 * Le formulaire d'ajout / modification utilise {@link FormulaireShell} afin
 * de garder exactement le même style visuel que "Ajouter un étudiant", et
 * les confirmations / alertes utilisent {@link DialogUtils}.
 */
public class CoursController implements Initializable {

    @FXML private FlowPane coursContainer;
    @FXML private Button btnAjouter;

    private CoursDAO coursDAO = new CoursDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerCours();
    }

    private Window fenetre() {
        return coursContainer.getScene() != null ? coursContainer.getScene().getWindow() : null;
    }

    private void chargerCours() {
        coursContainer.getChildren().clear();
        List<Cours> liste = coursDAO.getAll();
        for (Cours c : liste) {
            coursContainer.getChildren().add(creerCartesCours(c));
        }
    }

    private VBox creerCartesCours(Cours cours) {
        VBox carte = new VBox(8);
        carte.setPrefWidth(280);
        carte.setPadding(new Insets(15));
        carte.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);
        """);

        // En-tête : nom + coef
        HBox header = new HBox();
        header.setSpacing(8);
        Label nomLabel = new Label(cours.getIntitule());
        nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        HBox.setHgrow(nomLabel, Priority.ALWAYS);

        Label coefLabel = new Label("Coef. " + cours.getCoefficient());
        coefLabel.setStyle("""
            -fx-background-color: #e8f0fe;
            -fx-text-fill: #1a56db;
            -fx-padding: 2 8 2 8;
            -fx-background-radius: 10;
            -fx-font-size: 11px;
        """);
        header.getChildren().addAll(nomLabel, coefLabel);

        Label matiereLabel = new Label("Matière : " + cours.getMatiere());
        matiereLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        // Infos
        HBox volRow = makeInfoRow("Volume horaire:", cours.getVolumeHoraire() + "h/semaine");

        Separator sep = new Separator();
        sep.setPadding(new Insets(4, 0, 4, 0));

        // Boutons
        HBox boutons = new HBox(10);
        Button btnModifier  = new Button("✏ Modifier");
        Button btnSupprimer = new Button("🗑 Supprimer");

        btnModifier.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #1a56db;
            -fx-cursor: hand;
        """);
        btnSupprimer.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #e02020;
            -fx-cursor: hand;
        """);

        btnModifier.setOnAction(e  -> ouvrirFormulaireModification(cours));
        btnSupprimer.setOnAction(e -> supprimerCours(cours));

        boutons.getChildren().addAll(btnModifier, btnSupprimer);
        carte.getChildren().addAll(header, matiereLabel, volRow, sep, boutons);
        return carte;
    }

    private HBox makeInfoRow(String label, String valeur) {
        HBox row = new HBox();
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #555;");
        HBox.setHgrow(lbl, Priority.ALWAYS);
        Label val = new Label(valeur);
        val.setStyle("-fx-font-weight: bold;");
        row.getChildren().addAll(lbl, val);
        return row;
    }

    @FXML
    private void handleAjouter() {
        ouvrirFormulaireAjout();
    }

    private void ouvrirFormulaireAjout() {
        boolean ok = afficherFormulaireCours(null);
        if (ok) {
            chargerCours();
        }
    }

    private void ouvrirFormulaireModification(Cours cours) {
        boolean ok = afficherFormulaireCours(cours);
        if (ok) {
            chargerCours();
        }
    }

    /**
     * Affiche le formulaire d'ajout / modification d'un cours, avec le même
     * style visuel que le formulaire "Ajouter un étudiant".
     */
    private boolean afficherFormulaireCours(Cours coursExistant) {
        boolean estModification = (coursExistant != null);

        FormulaireShell shell = new FormulaireShell(
            estModification ? "Modifier le cours" : "Ajouter un cours",
            estModification ? "Modifiez les informations du cours" : "Remplissez les informations du cours",
            estModification ? "Modifier" : "Sauvegarder");

        TextField tfIntitule = new TextField();
        tfIntitule.setPromptText("Ex: Mathématiques Avancées");

        ComboBox<String> cbMatiere = new ComboBox<>();
        cbMatiere.getItems().setAll(coursDAO.getAllMatieres());
        cbMatiere.setPromptText("Choisir une matière");

        TextField tfVol = new TextField();
        tfVol.setPromptText("Ex: 4");
        TextField tfCoef = new TextField();
        tfCoef.setPromptText("Ex: 5");

        shell.champ("Intitulé du cours *", tfIntitule);
        shell.champ("Matière *", cbMatiere);
        shell.champsSurLigne("Volume horaire (h/semaine) *", tfVol, "Coefficient *", tfCoef);

        if (estModification) {
            tfIntitule.setText(coursExistant.getIntitule());
            cbMatiere.setValue(coursExistant.getMatiere());
            tfVol.setText(String.valueOf(coursExistant.getVolumeHoraire()));
            tfCoef.setText(String.valueOf(coursExistant.getCoefficient()));
        }

        shell.getBtnSauvegarder().setOnAction(e -> {
            shell.masquerErreur();
            if (tfIntitule.getText() == null || tfIntitule.getText().isBlank()) {
                shell.afficherErreur("L'intitulé du cours est obligatoire.");
                return;
            }
            if (cbMatiere.getValue() == null) {
                shell.afficherErreur("La matière est obligatoire.");
                return;
            }
            if (tfVol.getText() == null || tfVol.getText().isBlank()
                    || tfCoef.getText() == null || tfCoef.getText().isBlank()) {
                shell.afficherErreur("Le volume horaire et le coefficient sont obligatoires.");
                return;
            }

            int volumeHoraire;
            int coefficient;
            try {
                volumeHoraire = Integer.parseInt(tfVol.getText().trim());
                coefficient = Integer.parseInt(tfCoef.getText().trim());
            } catch (NumberFormatException ex) {
                shell.afficherErreur("Le volume horaire et le coefficient doivent être des nombres entiers.");
                return;
            }
            if (volumeHoraire <= 0 || coefficient <= 0) {
                shell.afficherErreur("Le volume horaire et le coefficient doivent être supérieurs à 0.");
                return;
            }

            Cours c = estModification ? coursExistant : new Cours();
            c.setIntitule(tfIntitule.getText().trim());
            c.setMatiere(cbMatiere.getValue());
            c.setVolumeHoraire(volumeHoraire);
            c.setCoefficient(coefficient);

            boolean ok = estModification ? coursDAO.update(c) : coursDAO.ajoute(c);
            if (!ok) {
                shell.afficherErreur(estModification
                    ? "Impossible de modifier ce cours."
                    : "Impossible d'ajouter ce cours.");
                return;
            }

            shell.marquerSauvegarde();
            shell.fermer();
        });

        return shell.afficherEtAttendre(fenetre());
    }

    private void supprimerCours(Cours cours) {
        boolean confirme = DialogUtils.confirmerSuppression(fenetre(),
            "Vous allez supprimer le cours :", cours.getIntitule());
        if (confirme) {
            boolean ok = coursDAO.delete(cours.getId());
            if (ok) {
                chargerCours();
                DialogUtils.afficherAlerte(fenetre(), "Succès", "Cours supprimé avec succès.", TypeAlerte.SUCCES);
            } else {
                DialogUtils.afficherAlerte(fenetre(), "Erreur", "Impossible de supprimer ce cours.", TypeAlerte.ERREUR);
            }
        }
    }
}
