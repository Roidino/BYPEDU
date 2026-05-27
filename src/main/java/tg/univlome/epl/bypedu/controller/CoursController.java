/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univlome.epl.bypedu.controller;

/**
 *
 * @author user
 */

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tg.univlome.epl.bypedu.dao.CoursDAO;
import tg.univlome.epl.bypedu.model.Cours;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CoursController implements Initializable {

    @FXML private FlowPane coursContainer;
    @FXML private Button btnAjouter;

    private CoursDAO coursDAO = new CoursDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerCours();
    }

    private void chargerCours() {
        coursContainer.getChildren().clear();
        List<Cours> liste = coursDAO.findAll();
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

        Label classeLabel = new Label(cours.getClasse());
        classeLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        // Infos
        HBox ensRow = makeInfoRow("Enseignant:", cours.getEnseignant());
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
        carte.getChildren().addAll(header, classeLabel, ensRow, volRow, sep, boutons);
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
        Dialog<Cours> dialog = creerFormulaire(null);
        Optional<Cours> result = dialog.showAndWait();
        result.ifPresent(c -> {
            coursDAO.save(c);
            chargerCours();
        });
    }

    private void ouvrirFormulaireModification(Cours cours) {
        Dialog<Cours> dialog = creerFormulaire(cours);
        Optional<Cours> result = dialog.showAndWait();
        result.ifPresent(c -> {
            coursDAO.update(c);
            chargerCours();
        });
    }

    private Dialog<Cours> creerFormulaire(Cours coursExistant) {
        Dialog<Cours> dialog = new Dialog<>();
        dialog.setTitle(coursExistant == null ? "Ajouter un cours" : "Modifier le cours");

        ButtonType btnOk = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField tfIntitule      = new TextField();
        TextField tfClasse   = new TextField();
        TextField tfEns      = new TextField();
        TextField tfVol      = new TextField();
        TextField tfCoef     = new TextField();

        if (coursExistant != null) {
            tfIntitule.setText(coursExistant.getIntitule());
            tfClasse.setText(coursExistant.getClasse());
            tfEns.setText(coursExistant.getEnseignant());
            tfVol.setText(String.valueOf(coursExistant.getVolumeHoraire()));
            tfCoef.setText(String.valueOf(coursExistant.getCoefficient()));
        }

        grid.addRow(0, new Label("Nom:"),             tfIntitule);
        grid.addRow(1, new Label("Classe:"),          tfClasse);
        grid.addRow(2, new Label("Enseignant:"),      tfEns);
        grid.addRow(3, new Label("Volume (h/sem):"),  tfVol);
        grid.addRow(4, new Label("Coefficient:"),     tfCoef);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnOk) {
                Cours c = coursExistant != null ? coursExistant : new Cours();
                c.setIntitule(tfIntitule.getText());
                c.setClasse(tfClasse.getText());
                c.setEnseignant(tfEns.getText());
                c.setVolumeHoraire(Integer.parseInt(tfVol.getText()));
                c.setCoefficient(Integer.parseInt(tfCoef.getText()));
                return c;
            }
            return null;
        });

        return dialog;
    }

    private void supprimerCours(Cours cours) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer \"" + cours.getIntitule() + "\" ?",
            ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                coursDAO.delete(cours.getId());
                chargerCours();
            }
        });
    }
}