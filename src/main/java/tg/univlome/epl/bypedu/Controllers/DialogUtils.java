package tg.univlome.epl.bypedu.Controllers;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Boîtes de dialogue stylées et cohérentes (confirmation de suppression,
 * messages de succès / erreur / information), utilisées sur toutes les
 * pages de l'application (Étudiants, Enseignants, Cours, Notes, Emploi du
 * Temps) pour remplacer les {@link javafx.scene.control.Alert} par défaut,
 * peu esthétiques et incohérentes avec le reste de l'interface.
 */
public final class DialogUtils {

    public enum TypeAlerte { SUCCES, ERREUR, INFO }

    private DialogUtils() {
    }

    /**
     * Affiche une boîte de confirmation de suppression stylée et retourne
     * {@code true} si l'utilisateur a confirmé l'action.
     *
     * @param owner    fenêtre parente (peut être {@code null})
     * @param message  phrase d'introduction (ex : "Vous allez supprimer le cours :")
     * @param cible    nom de l'élément concerné (ex : "Mathématiques Avancées")
     */
    public static boolean confirmerSuppression(Window owner, String message, String cible) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setResizable(false);
        stage.setTitle("Confirmation");

        boolean[] confirme = {false};

        Label icone = new Label("🗑");
        icone.setStyle("-fx-font-size: 32px; -fx-text-fill: #DC2626;");

        Label titre = new Label("Confirmer la suppression");
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(340);
        messageLabel.setAlignment(Pos.CENTER);

        Label nomCible = new Label(cible);
        nomCible.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        nomCible.setWrapText(true);
        nomCible.setMaxWidth(340);
        nomCible.setAlignment(Pos.CENTER);

        Label avertissement = new Label("Cette action est irréversible.");
        avertissement.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #DC2626; -fx-background-color: #FEF2F2;" +
            "-fx-background-radius: 6; -fx-border-color: #FECACA; -fx-border-radius: 6; -fx-padding: 8 12;");

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle(
            "-fx-background-color: white; -fx-text-fill: #374151; -fx-border-color: #D1D5DB;" +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 24; -fx-font-size: 13px;");
        btnAnnuler.setOnAction(e -> stage.close());

        Button btnSupprimer = new Button("Oui, supprimer");
        String styleSupprimer =
            "-fx-background-color: #DC2626; -fx-text-fill: white; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 10 24; -fx-font-size: 13px;";
        String styleSupprimerHover =
            "-fx-background-color: #B91C1C; -fx-text-fill: white; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 10 24; -fx-font-size: 13px;";
        btnSupprimer.setStyle(styleSupprimer);
        btnSupprimer.setOnMouseEntered(e -> btnSupprimer.setStyle(styleSupprimerHover));
        btnSupprimer.setOnMouseExited(e -> btnSupprimer.setStyle(styleSupprimer));
        btnSupprimer.setOnAction(e -> {
            confirme[0] = true;
            stage.close();
        });

        HBox boutons = new HBox(12, btnAnnuler, btnSupprimer);
        boutons.setAlignment(Pos.CENTER_RIGHT);
        boutons.setStyle("-fx-padding: 8 0 0 0;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #E5E7EB;");

        VBox header = new VBox(8, icone, titre);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #FEF2F2; -fx-padding: 20; -fx-background-radius: 10;");

        VBox layout = new VBox(16, header, messageLabel, nomCible, avertissement, sep, boutons);
        layout.setAlignment(Pos.CENTER);
        layout.setPrefWidth(400);
        layout.setStyle("-fx-background-color: white; -fx-padding: 30;");

        stage.setScene(new Scene(layout));
        stage.showAndWait();

        return confirme[0];
    }

    /** Affiche une alerte stylée (succès, erreur ou information). */
    public static void afficherAlerte(Window owner, String titre, String message, TypeAlerte type) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setResizable(false);

        String icone, couleurFond, couleurTexte, couleurBouton;
        switch (type) {
            case SUCCES:
                icone = "✓"; couleurFond = "#F0FDF4"; couleurTexte = "#166534"; couleurBouton = "#16A34A";
                break;
            case ERREUR:
                icone = "✕"; couleurFond = "#FEF2F2"; couleurTexte = "#991B1B"; couleurBouton = "#DC2626";
                break;
            default:
                icone = "ℹ"; couleurFond = "#EFF6FF"; couleurTexte = "#1E40AF"; couleurBouton = "#2563EB";
        }

        Label lblIcone = new Label(icone);
        lblIcone.setStyle("-fx-font-size: 32px; -fx-text-fill:" + couleurTexte + ";");

        Label lblTitre = new Label(titre);
        lblTitre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label lblMsg = new Label(message);
        lblMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        lblMsg.setWrapText(true);
        lblMsg.setMaxWidth(300);
        lblMsg.setAlignment(Pos.CENTER);

        VBox header = new VBox(8, lblIcone, lblTitre);
        header.setAlignment(Pos.CENTER);
        header.setMinWidth(340);
        header.setStyle("-fx-background-color:" + couleurFond + "; -fx-padding: 24; -fx-background-radius: 10;");

        Button btnOk = new Button("OK");
        btnOk.setMinWidth(120);
        btnOk.setStyle(
            "-fx-background-color:" + couleurBouton + "; -fx-text-fill: white; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 10 40; -fx-font-size: 13px;");
        btnOk.setOnAction(e -> stage.close());

        VBox layout = new VBox(20, header, lblMsg, btnOk);
        layout.setAlignment(Pos.CENTER);
        layout.setMinWidth(380);
        layout.setMinHeight(220);
        layout.setStyle("-fx-background-color: white; -fx-padding: 30;");

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.showAndWait();
    }
}
