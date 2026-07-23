package tg.univlome.epl.bypedu.Controllers;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import tg.univlome.epl.bypedu.DAOs.EmploiDAO;
import tg.univlome.epl.bypedu.models.Creneau;

/**
 * Contrôleur de la page "Emploi du Temps".
 * Affiche, pour une classe donnée, la grille hebdomadaire des créneaux de cours
 * sous forme d'un tableau horaire (lignes = créneaux horaires, colonnes = jours),
 * avec un code couleur par matière et une légende, façon maquette.
 *
 * @author BYPEDU
 */
public class EmploiController implements Initializable {

    @FXML private ComboBox<String> comboClasse;
    @FXML private GridPane grilleEmploi;
    @FXML private Label labelVide;
    @FXML private Label labelSousTitre;
    @FXML private FlowPane legende;

    private final EmploiDAO emploiDAO = new EmploiDAO();

    private static final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};

    /** Palette de couleurs cycliques, une couleur par cours (assignation stable via hash du nom). */
    private static final String[] PALETTE = {
        "#DBEAFE", // bleu clair
        "#EDE9FE", // violet clair
        "#DCFCE7", // vert clair
        "#FEF3C7", // jaune clair
        "#FCE7F3", // rose clair
        "#E0F2FE", // cyan clair
        "#FFE4E6", // rouge clair
        "#E5E7EB", // gris clair
    };
    private static final String[] PALETTE_TEXTE = {
        "#1D4ED8", "#6D28D9", "#15803D", "#B45309", "#BE185D", "#0369A1", "#BE123C", "#374151"
    };

    private static final DateTimeFormatter HEURE_PARSER = DateTimeFormatter.ofPattern("H:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<String> classes = emploiDAO.getAllClasses();
        comboClasse.getItems().addAll(classes);
        if (!classes.isEmpty()) {
            comboClasse.setValue(classes.get(0));
        }

        comboClasse.valueProperty().addListener((obs, oldV, newV) -> chargerGrille());

        chargerGrille();
    }

    private void chargerGrille() {
        grilleEmploi.getChildren().clear();
        legende.getChildren().clear();

        String classe = comboClasse.getValue();
        if (classe == null) {
            labelVide.setVisible(true);
            labelVide.setManaged(true);
            labelSousTitre.setText("");
            construireEnTeteJours();
            return;
        }

        int classeId = emploiDAO.getClasseIdParNom(classe);
        List<Creneau> creneaux = emploiDAO.getByClasse(classeId);

        labelSousTitre.setText(classe + " — " + descriptionSemaineCourante());
        labelVide.setVisible(creneaux.isEmpty());
        labelVide.setManaged(creneaux.isEmpty());

        construireEnTeteJours();

        if (creneaux.isEmpty()) {
            return;
        }

        List<String[]> creneauxHoraires = extraireCreneauxHoraires(creneaux);

        int ligne = 1;
        String finPrecedente = null;
        for (String[] slot : creneauxHoraires) {
            String debut = slot[0];
            String fin = slot[1];

            if (finPrecedente != null && ecartEnMinutes(finPrecedente, debut) >= 75) {
                ajouterLignePause(ligne, finPrecedente, debut);
                ligne++;
            }

            ajouterLigneHoraire(ligne, debut, fin, creneaux);
            ligne++;
            finPrecedente = fin;
        }

        construireLegende(creneaux);
    }

    private void construireEnTeteJours() {
        Label coinHoraire = new Label("Horaire");
        coinHoraire.setMaxWidth(Double.MAX_VALUE);
        coinHoraire.setAlignment(Pos.CENTER);
        coinHoraire.setStyle("""
            -fx-background-color: #0D47A1;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 11px;
            -fx-padding: 10;
            -fx-background-radius: 8 0 0 0;
            """);
        GridPane.setHgrow(coinHoraire, Priority.ALWAYS);
        grilleEmploi.add(coinHoraire, 0, 0);

        for (int i = 0; i < JOURS.length; i++) {
            Label header = new Label(JOURS[i]);
            header.setMaxWidth(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);
            boolean dernier = i == JOURS.length - 1;
            header.setStyle(
                "-fx-background-color: #0D47A1;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10;" +
                "-fx-background-radius: " + (dernier ? "0 8 0 0" : "0") + ";"
            );
            GridPane.setHgrow(header, Priority.ALWAYS);
            grilleEmploi.add(header, i + 1, 0);
        }
    }

    /** Extrait, triés chronologiquement, les couples (heureDebut, heureFin) distincts présents dans les créneaux. */
    private List<String[]> extraireCreneauxHoraires(List<Creneau> creneaux) {
        Set<String> pairesUniques = new LinkedHashSet<>();
        for (Creneau c : creneaux) {
            if (c.getHeureDebut() != null && c.getHeureFin() != null) {
                pairesUniques.add(c.getHeureDebut() + "|" + c.getHeureFin());
            }
        }
        List<String[]> paires = new ArrayList<>();
        for (String p : pairesUniques) {
            String[] parts = p.split("\\|", 2);
            paires.add(parts);
        }
        paires.sort(Comparator.comparingInt(p -> minutesDepuisMinuit(p[0])));
        return paires;
    }

    private int minutesDepuisMinuit(String heure) {
        try {
            LocalTime t = LocalTime.parse(heure.length() == 4 ? "0" + heure : heure, HEURE_PARSER);
            return t.getHour() * 60 + t.getMinute();
        } catch (Exception e) {
            return 0;
        }
    }

    private int ecartEnMinutes(String heureFin, String heureDebutSuivant) {
        return minutesDepuisMinuit(heureDebutSuivant) - minutesDepuisMinuit(heureFin);
    }

    private void ajouterLignePause(int ligne, String debut, String fin) {
        for (int i = 0; i < JOURS.length; i++) {
            Label pause = new Label(i == 0 ? "Pause déjeuner" : "");
            pause.setMaxWidth(Double.MAX_VALUE);
            pause.setAlignment(Pos.CENTER);
            pause.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px; -fx-font-style: italic;");
            grilleEmploi.add(pause, i + 1, ligne);
        }
        Label heureLabel = new Label(debut + "-" + fin);
        heureLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 10px;");
        heureLabel.setAlignment(Pos.CENTER);
        heureLabel.setMaxWidth(Double.MAX_VALUE);
        grilleEmploi.add(heureLabel, 0, ligne);
    }

    private void ajouterLigneHoraire(int ligne, String debut, String fin, List<Creneau> creneaux) {
        Label heureLabel = new Label(debut + "\n" + fin);
        heureLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 11px; -fx-font-weight: bold;");
        heureLabel.setAlignment(Pos.CENTER);
        heureLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        heureLabel.setMaxWidth(Double.MAX_VALUE);
        StackPane heureCell = new StackPane(heureLabel);
        heureCell.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 6; -fx-padding: 8;");
        grilleEmploi.add(heureCell, 0, ligne);

        for (int i = 0; i < JOURS.length; i++) {
            String jour = JOURS[i];
            Optional<Creneau> creneau = creneaux.stream()
                .filter(c -> jour.equalsIgnoreCase(c.getJour())
                        && debut.equals(c.getHeureDebut())
                        && fin.equals(c.getHeureFin()))
                .findFirst();

            if (creneau.isPresent()) {
                grilleEmploi.add(creerCarteCreneau(creneau.get()), i + 1, ligne);
            } else {
                Region vide = new Region();
                vide.setStyle("-fx-background-color: transparent;");
                grilleEmploi.add(vide, i + 1, ligne);
            }
        }
    }

    private VBox creerCarteCreneau(Creneau c) {
        int index = Math.floorMod(nomNormalise(c.getCours()).hashCode(), PALETTE.length);
        String fond = PALETTE[index];
        String texte = PALETTE_TEXTE[index];

        VBox carte = new VBox(3);
        carte.setPadding(new Insets(8));
        carte.setStyle(
            "-fx-background-color: " + fond + ";" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );

        Label nomCours = new Label(c.getCours());
        nomCours.setWrapText(true);
        nomCours.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + texte + ";");

        Label salle = new Label((c.getSalle() == null || c.getSalle().isBlank()) ? "" : c.getSalle());
        salle.setStyle("-fx-font-size: 10px; -fx-text-fill: " + texte + ";");

        carte.getChildren().addAll(nomCours, salle);

        HBox actions = new HBox(6);
        Button btnModifier = new Button("✏");
        Button btnSupprimer = new Button("🗑");
        btnModifier.setStyle("-fx-background-color: transparent; -fx-text-fill: " + texte + "; -fx-cursor: hand; -fx-padding: 0; -fx-font-size: 10px;");
        btnSupprimer.setStyle("-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-cursor: hand; -fx-padding: 0; -fx-font-size: 10px;");
        btnModifier.setOnAction(e -> ouvrirFormulaireModification(c));
        btnSupprimer.setOnAction(e -> supprimerCreneau(c));
        actions.getChildren().addAll(btnModifier, btnSupprimer);
        carte.getChildren().add(actions);

        return carte;
    }

    private void construireLegende(List<Creneau> creneaux) {
        Set<String> nomsCours = new TreeSet<>();
        for (Creneau c : creneaux) {
            if (c.getCours() != null) nomsCours.add(c.getCours());
        }
        for (String nom : nomsCours) {
            int index = Math.floorMod(nomNormalise(nom).hashCode(), PALETTE.length);
            Region swatch = new Region();
            swatch.setPrefSize(12, 12);
            swatch.setStyle("-fx-background-color: " + PALETTE_TEXTE[index] + "; -fx-background-radius: 3;");

            Label label = new Label(nom);
            label.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

            HBox item = new HBox(6, swatch, label);
            item.setAlignment(Pos.CENTER_LEFT);
            legende.getChildren().add(item);
        }
    }

    private String nomNormalise(String nom) {
        return nom == null ? "" : nom.trim().toLowerCase();
    }

    private String descriptionSemaineCourante() {
        LocalDate aujourdHui = LocalDate.now();
        LocalDate lundi = aujourdHui.with(DayOfWeek.MONDAY);
        LocalDate vendredi = lundi.plusDays(4);
        DateTimeFormatter jourMois = DateTimeFormatter.ofPattern("d");
        String mois = vendredi.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        return "Semaine du " + lundi.format(jourMois) + " au " + vendredi.format(jourMois) + " " + mois + " " + vendredi.getYear();
    }

    @FXML
    private void handleAjouterCreneau() {
        boolean ok = afficherFormulaireCreneau(null);
        if (ok) {
            chargerGrille();
        }
    }

    private void ouvrirFormulaireModification(Creneau creneau) {
        boolean ok = afficherFormulaireCreneau(creneau);
        if (ok) {
            chargerGrille();
        }
    }

    private javafx.stage.Window fenetre() {
        return grilleEmploi.getScene() != null ? grilleEmploi.getScene().getWindow() : null;
    }

    private void supprimerCreneau(Creneau creneau) {
        boolean confirme = DialogUtils.confirmerSuppression(fenetre(),
            "Vous allez supprimer le créneau :",
            creneau.getCours() + " — " + creneau.getJour() + " " + creneau.getHeureDebut() + "-" + creneau.getHeureFin());

        if (confirme) {
            if (emploiDAO.delete(creneau.getId())) {
                chargerGrille();
                DialogUtils.afficherAlerte(fenetre(), "Succès", "Créneau supprimé avec succès.", DialogUtils.TypeAlerte.SUCCES);
            } else {
                DialogUtils.afficherAlerte(fenetre(), "Erreur", "Impossible de supprimer ce créneau.", DialogUtils.TypeAlerte.ERREUR);
            }
        }
    }

    /**
     * Affiche le formulaire d'ajout / modification d'un créneau, avec le même
     * style visuel que le formulaire "Ajouter un étudiant".
     */
    private boolean afficherFormulaireCreneau(Creneau creneauExistant) {
        boolean estModification = (creneauExistant != null);

        FormulaireShell shell = new FormulaireShell(
            estModification ? "Modifier le créneau" : "Ajouter un créneau",
            estModification ? "Modifiez les informations du créneau" : "Remplissez les informations du créneau",
            estModification ? "Modifier" : "Sauvegarder");

        ComboBox<String> cbClasse = new ComboBox<>();
        cbClasse.getItems().addAll(emploiDAO.getAllClasses());
        cbClasse.setPromptText("Choisir une classe");
        cbClasse.setValue(estModification ? creneauExistant.getClasse() : comboClasse.getValue());

        ComboBox<String> cbCours = new ComboBox<>();
        cbCours.setPromptText("Choisir un cours");
        Runnable rafraichirCours = () -> {
            cbCours.getItems().setAll(emploiDAO.getCoursParClasse(cbClasse.getValue()));
        };
        cbClasse.valueProperty().addListener((obs, oldV, newV) -> rafraichirCours.run());
        rafraichirCours.run();

        ComboBox<String> cbJour = new ComboBox<>();
        cbJour.getItems().addAll(JOURS);
        cbJour.setPromptText("Choisir un jour");

        TextField tfHeureDebut = new TextField();
        tfHeureDebut.setPromptText("HH:mm (ex: 08:00)");
        TextField tfHeureFin = new TextField();
        tfHeureFin.setPromptText("HH:mm (ex: 10:00)");
        TextField tfSalle = new TextField();
        tfSalle.setPromptText("Ex: Salle A1");

        shell.champ("Classe *", cbClasse);
        shell.champsSurLigne("Cours *", cbCours, "Jour *", cbJour);
        shell.champsSurLigne("Heure début *", tfHeureDebut, "Heure fin *", tfHeureFin);
        shell.champ("Salle", tfSalle);

        if (estModification) {
            cbCours.setValue(creneauExistant.getCours());
            cbJour.setValue(creneauExistant.getJour());
            tfHeureDebut.setText(creneauExistant.getHeureDebut());
            tfHeureFin.setText(creneauExistant.getHeureFin());
            tfSalle.setText(creneauExistant.getSalle());
        }

        shell.getBtnSauvegarder().setOnAction(e -> {
            shell.masquerErreur();

            if (cbClasse.getValue() == null || cbCours.getValue() == null || cbJour.getValue() == null
                    || tfHeureDebut.getText() == null || tfHeureDebut.getText().isBlank()
                    || tfHeureFin.getText() == null || tfHeureFin.getText().isBlank()) {
                shell.afficherErreur("Veuillez remplir tous les champs obligatoires.");
                return;
            }
            if (!tfHeureDebut.getText().matches("\\d{1,2}:\\d{2}") || !tfHeureFin.getText().matches("\\d{1,2}:\\d{2}")) {
                shell.afficherErreur("Les heures doivent être au format HH:mm (ex: 08:00).");
                return;
            }
            if (minutesDepuisMinuit(tfHeureFin.getText()) <= minutesDepuisMinuit(tfHeureDebut.getText())) {
                shell.afficherErreur("L'heure de fin doit être après l'heure de début.");
                return;
            }

            Creneau c = estModification ? creneauExistant : new Creneau();
            c.setClasse(cbClasse.getValue());
            c.setClasseId(emploiDAO.getClasseIdParNom(cbClasse.getValue()));
            c.setCours(cbCours.getValue());
            c.setCoursId(emploiDAO.getCoursIdParNom(cbCours.getValue()));
            c.setJour(cbJour.getValue());
            c.setHeureDebut(tfHeureDebut.getText());
            c.setHeureFin(tfHeureFin.getText());
            c.setSalle(tfSalle.getText());

            boolean ok = estModification ? emploiDAO.update(c) : emploiDAO.ajoute(c);
            if (!ok) {
                shell.afficherErreur(estModification
                    ? "Impossible de modifier ce créneau."
                    : "Impossible d'ajouter ce créneau.");
                return;
            }

            shell.marquerSauvegarde();
            shell.fermer();
        });

        return shell.afficherEtAttendre(fenetre());
    }
}
