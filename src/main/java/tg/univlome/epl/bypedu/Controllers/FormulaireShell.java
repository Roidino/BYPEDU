package tg.univlome.epl.bypedu.Controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Coquille de formulaire modal réutilisable : en-tête bleu avec titre et
 * sous-titre, corps contenant les champs, bandeau d'erreur, et pied de page
 * avec les boutons "Annuler" / "Sauvegarder".
 * <p>
 * Reprend exactement le style visuel du formulaire "Ajouter un étudiant"
 * (formulaireEtudiant.fxml) afin que tous les formulaires de l'application
 * (Enseignants, Cours, Notes, Emploi du Temps) soient cohérents entre eux.
 */
public class FormulaireShell {

    public static final String COULEUR_HEADER = "#102A43";
    public static final String COULEUR_SOUS_TITRE = "#9AD9DE";
    public static final String COULEUR_PRIMAIRE = "#0B7285";
    public static final String COULEUR_PRIMAIRE_HOVER = "#075B6A";

    private static final String STYLE_CHAMP =
            "-fx-background-color: white; -fx-border-color: #BCCCDC; -fx-border-radius: 8;" +
        "-fx-background-radius: 8; -fx-font-size: 13px;";
    private static final String STYLE_CHAMP_TEXTE = STYLE_CHAMP + " -fx-padding: 10 14;";
    private static final String STYLE_LABEL_CHAMP =
        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;";

    private final Stage stage;
    private final VBox champsContainer;
    private final Label messageErreur;
    private final HBox errorBox;
    private final Button btnAnnuler;
    private final Button btnSauvegarder;
    private boolean sauvegarde = false;

    public FormulaireShell(String titre, String sousTitre, String texteBouton) {
        this(titre, sousTitre, texteBouton, 480);
    }

    public FormulaireShell(String titre, String sousTitre, String texteBouton, double largeur) {
        Label titreLabel = new Label(titre);
        titreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sousTitreLabel = new Label(sousTitre);
        sousTitreLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + COULEUR_SOUS_TITRE + "; -fx-padding: 4 0 0 0;");

        VBox header = new VBox(titreLabel, sousTitreLabel);
        header.setStyle(
            "-fx-background-color: " + COULEUR_HEADER + ";" +
            "-fx-padding: 24 30 24 30; -fx-background-radius: 12 12 0 0;");

        champsContainer = new VBox(16);

        messageErreur = new Label("");
        messageErreur.setWrapText(true);
        messageErreur.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        errorBox = new HBox(messageErreur);
        errorBox.setVisible(false);
        errorBox.setManaged(false);
        errorBox.setStyle(
            "-fx-background-color: #FEF2F2; -fx-border-color: #FECACA; -fx-border-radius: 6;" +
            "-fx-background-radius: 6; -fx-padding: 8 12;");

        VBox corpsContenu = new VBox(20, champsContainer, errorBox);
        corpsContenu.setStyle("-fx-padding: 30 30 20 30;");

        ScrollPane scrollCorps = new ScrollPane(corpsContenu);
        scrollCorps.setFitToWidth(true);
        scrollCorps.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollCorps.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollCorps.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
        VBox.setVgrow(scrollCorps, Priority.ALWAYS);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #E5E7EB;");

        btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle(
            "-fx-background-color: white; -fx-text-fill: #374151; -fx-border-color: #D1D5DB;" +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 24; -fx-font-size: 13px;");
        btnAnnuler.setOnAction(e -> fermer());

        btnSauvegarder = new Button(texteBouton);
        String styleBtn =
            "-fx-background-color: " + COULEUR_PRIMAIRE + "; -fx-text-fill: white; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 10 24; -fx-font-size: 13px;";
        String styleBtnHover =
            "-fx-background-color: " + COULEUR_PRIMAIRE_HOVER + "; -fx-text-fill: white; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 10 24; -fx-font-size: 13px;";
        btnSauvegarder.setStyle(styleBtn);
        btnSauvegarder.setOnMouseEntered(e -> btnSauvegarder.setStyle(styleBtnHover));
        btnSauvegarder.setOnMouseExited(e -> btnSauvegarder.setStyle(styleBtn));

        HBox footer = new HBox(12, btnAnnuler, btnSauvegarder);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle(
            "-fx-padding: 16 30 20 30; -fx-background-color: #F4F7FB; -fx-background-radius: 0 0 12 12;");

        VBox racine = new VBox(0, header, scrollCorps, sep, footer);
        racine.setPrefWidth(largeur);
        racine.setPrefHeight(620);
        racine.setMaxHeight(Double.MAX_VALUE);
        racine.setStyle("-fx-background-color: white;");

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(true);
        stage.setMinWidth(largeur);
        stage.setMinHeight(280);
        stage.setTitle(titre);
        stage.setScene(new Scene(racine));
    }

    /** Ajoute un champ (label + contrôle) en pleine largeur dans le formulaire. */
    public VBox champ(String labelTexte, Control control) {
        VBox box = champSeul(labelTexte, control);
        champsContainer.getChildren().add(box);
        return box;
    }

    /** Place deux champs côte à côte (demi-largeur chacun) sur une même ligne. */
    public void champsSurLigne(String label1, Control control1, String label2, Control control2) {
        VBox box1 = champSeul(label1, control1);
        VBox box2 = champSeul(label2, control2);
        HBox.setHgrow(box1, Priority.ALWAYS);
        HBox.setHgrow(box2, Priority.ALWAYS);
        HBox ligne = new HBox(20, box1, box2);
        champsContainer.getChildren().add(ligne);
    }

    private VBox champSeul(String labelTexte, Control control) {
        Label lbl = new Label(labelTexte);
        lbl.setStyle(STYLE_LABEL_CHAMP);
        styliserChamp(control);
        VBox box = new VBox(6, lbl, control);
        return box;
    }

    /** Applique le style visuel standard (fond gris clair, bordure arrondie) à un champ. */
    public static void styliserChamp(Control control) {
        boolean estCombo = control.getClass().getSimpleName().contains("ComboBox")
            || control.getClass().getSimpleName().contains("DatePicker");
        control.setStyle(estCombo ? STYLE_CHAMP : STYLE_CHAMP_TEXTE);
        control.setMaxWidth(Double.MAX_VALUE);
    }

    public void afficherErreur(String message) {
        messageErreur.setText(message);
        errorBox.setVisible(true);
        errorBox.setManaged(true);
    }

    public void masquerErreur() {
        errorBox.setVisible(false);
        errorBox.setManaged(false);
    }

    public Button getBtnSauvegarder() {
        return btnSauvegarder;
    }

    public Button getBtnAnnuler() {
        return btnAnnuler;
    }

    public VBox getChampsContainer() {
        return champsContainer;
    }

    public void marquerSauvegarde() {
        this.sauvegarde = true;
    }

    public boolean isSauvegarde() {
        return sauvegarde;
    }

    public void fermer() {
        stage.close();
    }

    /** Affiche le formulaire de façon modale et bloque jusqu'à sa fermeture. */
    public boolean afficherEtAttendre(Window owner) {
        if (owner != null) stage.initOwner(owner);
        stage.showAndWait();
        return sauvegarde;
    }
}
