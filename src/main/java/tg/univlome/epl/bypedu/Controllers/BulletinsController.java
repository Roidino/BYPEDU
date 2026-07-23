package tg.univlome.epl.bypedu.Controllers;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import tg.univlome.epl.bypedu.Controllers.DialogUtils.TypeAlerte;
import tg.univlome.epl.bypedu.DAOs.NoteDAO;
import tg.univlome.epl.bypedu.models.Note;

public class BulletinsController implements Initializable {

    // À personnaliser selon l'établissement
    private static final String NOM_ETABLISSEMENT = "ÉTABLISSEMENT SCOLAIRE";

    @FXML private ComboBox<String> comboTypeBulletin;
    @FXML private ComboBox<String> comboCibleBulletin;
    @FXML private ComboBox<String> comboTrimestreBulletin;
    @FXML private TextField txtRechercheEtudiant;
    @FXML private TableView<Note> tableBulletinsEtudiant;
    @FXML private TableColumn<Note, String> colBulletinTrimestre;
    @FXML private TableColumn<Note, String> colBulletinMatiere;
    @FXML private TableColumn<Note, Double> colBulletinNote;
    @FXML private TableColumn<Note, String> colBulletinClasse;
    @FXML private Label lblResultatRecherche;

    private final NoteDAO noteDAO = new NoteDAO();
    private static final String[] TRIMESTRES = {"Trimestre 1", "Trimestre 2", "Trimestre 3"};
    private static final Map<String, String> TRIMESTRE_CODES = Map.of(
        "Trimestre 1", "T1", "Trimestre 2", "T2", "Trimestre 3", "T3");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboTypeBulletin.getItems().addAll("Étudiant", "Classe");
        comboTypeBulletin.setValue("Étudiant");
        comboTrimestreBulletin.getItems().addAll(TRIMESTRES);
        comboTypeBulletin.valueProperty().addListener((obs, oldValue, newValue) -> actualiserCibles());
        colBulletinTrimestre.setCellValueFactory(new PropertyValueFactory<>("trimestre"));
        colBulletinMatiere.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        colBulletinNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colBulletinClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colBulletinTrimestre.setCellFactory(column -> new TableCell<Note, String>() {
            @Override protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.replace("T", "Trimestre "));
            }
        });
        colBulletinNote.setCellFactory(column -> new TableCell<Note, Double>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f / 20", value));
            }
        });
        actualiserCibles();
    }

    private Window fenetre() {
        return comboTypeBulletin.getScene() == null ? null : comboTypeBulletin.getScene().getWindow();
    }

    private void actualiserCibles() {
        boolean parClasse = "Classe".equals(comboTypeBulletin.getValue());
        comboCibleBulletin.getItems().setAll(parClasse ? noteDAO.getAllClasses() : List.of());
        comboCibleBulletin.setValue(null);
        comboCibleBulletin.setPromptText(parClasse ? "Choisir une classe" : "Utiliser la recherche ci-dessous");
        comboCibleBulletin.setDisable(!parClasse);
        txtRechercheEtudiant.setDisable(parClasse);
    }

    @FXML
    private void RechercherBulletinsEtudiant(ActionEvent event) {
        String recherche = txtRechercheEtudiant.getText();
        List<Note> resultats = noteDAO.rechercherBulletinsEtudiant(recherche);
        tableBulletinsEtudiant.getItems().setAll(resultats);
        if (resultats.isEmpty()) {
            lblResultatRecherche.setText("Aucun bulletin trouvé pour cette recherche.");
        } else {
            String nom = resultats.get(0).getEtudiant();
            long trimestres = resultats.stream().map(Note::getTrimestre).distinct().count();
            lblResultatRecherche.setText(nom + " : " + trimestres + " trimestre(s), "
                + resultats.size() + " note(s) affichée(s).");
        }
    }

    @FXML
    private void TelechargerBulletin(ActionEvent event) {
        boolean parClasse = "Classe".equals(comboTypeBulletin.getValue());
        String cible = parClasse ? comboCibleBulletin.getValue() : txtRechercheEtudiant.getText().trim();
        String trimestre = comboTrimestreBulletin.getValue();
        if (cible == null || cible.isBlank() || trimestre == null) {
            DialogUtils.afficherAlerte(fenetre(), "Bulletin incomplet",
                "Choisissez une personne ou une classe ainsi qu'un trimestre.", TypeAlerte.ERREUR);
            return;
        }

        String codeTrimestre = TRIMESTRE_CODES.get(trimestre);
        List<Note> notes = parClasse
            ? noteDAO.getBulletin(null, cible, codeTrimestre)
            : noteDAO.rechercherBulletinsEtudiant(cible).stream()
                .filter(note -> codeTrimestre.equals(note.getTrimestre())).toList();
        if (notes.isEmpty()) {
            DialogUtils.afficherAlerte(fenetre(), "Aucune note",
                "Aucune note ne correspond à cette sélection.", TypeAlerte.ERREUR);
            return;
        }

        if (!parClasse) cible = notes.get(0).getEtudiant();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le bulletin");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
        chooser.setInitialFileName("bulletin-" + nomFichier(cible) + "-" + codeTrimestre + ".pdf");
        java.io.File fichier = chooser.showSaveDialog(fenetre());
        if (fichier == null) return;

        try {
            ecrireBulletinPdf(fichier.toPath(), parClasse, trimestre, notes);
            DialogUtils.afficherAlerte(fenetre(), "Téléchargement terminé",
                "Le bulletin a été enregistré avec succès.", TypeAlerte.SUCCES);
        } catch (IOException ex) {
            DialogUtils.afficherAlerte(fenetre(), "Erreur",
                "Impossible d'enregistrer le bulletin : " + ex.getMessage(), TypeAlerte.ERREUR);
        }
    }

    private String nomFichier(String valeur) {
        return Normalizer.normalize(valeur, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
            .replaceAll("[^a-zA-Z0-9]+", "-").replaceAll("^-|-$", "").toLowerCase();
    }

    private String pdfTexte(String valeur) {
        return Normalizer.normalize(valeur == null ? "" : valeur, Normalizer.Form.NFD)
            .replaceAll("[^\\p{ASCII}]", "").replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    /** Barème d'appréciation utilisé sur le bulletin (à afficher aussi côté interface). */
    private String appreciation(double note) {
        if (note >= 16) return "Excellent";
        if (note >= 14) return "Tres Bien";
        if (note >= 12) return "Bien";
        if (note >= 10) return "Assez Bien";
        if (note >= 8) return "Passable";
        return "Insuffisant";
    }

    private String anneeScolaire() {
        LocalDate aujourdhui = LocalDate.now();
        int annee = aujourdhui.getYear();
        return aujourdhui.getMonthValue() >= 9 ? (annee + "-" + (annee + 1)) : ((annee - 1) + "-" + annee);
    }

    // ----------------------------------------------------------------------------------------
    // Construction du bulletin : un bulletin (une page) par élève, format bulletin togolais.
    // ----------------------------------------------------------------------------------------

    private void ecrireBulletinPdf(Path chemin, boolean parClasse, String trimestre, List<Note> notes)
            throws IOException {
        Map<String, List<Note>> parEtudiant = new LinkedHashMap<>();
        for (Note n : notes) {
            parEtudiant.computeIfAbsent(n.getEtudiant(), k -> new ArrayList<>()).add(n);
        }

        Map<String, Double> moyennes = new LinkedHashMap<>();
        for (Map.Entry<String, List<Note>> entree : parEtudiant.entrySet()) {
            double moyenne = entree.getValue().stream().mapToDouble(Note::getNote).average().orElse(0);
            moyennes.put(entree.getKey(), moyenne);
        }

        List<String> classement = new ArrayList<>(moyennes.keySet());
        classement.sort((a, b) -> Double.compare(moyennes.get(b), moyennes.get(a)));

        String classeNom = notes.get(0).getClasse();
        List<String> pages = new ArrayList<>();
        for (String etudiant : parEtudiant.keySet()) {
            List<Note> notesEtudiant = parEtudiant.get(etudiant);
            double moyenne = moyennes.get(etudiant);
            int rang = parClasse ? classement.indexOf(etudiant) + 1 : 0;
            pages.add(construirePageBulletin(etudiant, classeNom, trimestre, notesEtudiant, moyenne,
                rang, parEtudiant.size()));
        }

        ecrirePdfMultiPages(chemin, pages);
    }

    private String construirePageBulletin(String etudiant, String classeNom, String trimestre,
            List<Note> notes, double moyenne, int rang, int totalEleves) {
        float margeG = 50, margeD = 545;
        float centreX = (margeG + margeD) / 2;
        StringBuilder c = new StringBuilder();
        float y = 800;

        // En-tête officiel
        c.append(texteCentre("REPUBLIQUE TOGOLAISE", 12, true, centreX, y)); y -= 14;
        c.append(texteCentre("Travail - Liberte - Patrie", 9, false, centreX, y)); y -= 16;
        c.append(texteCentre(NOM_ETABLISSEMENT, 11, true, centreX, y)); y -= 18;
        c.append(ligneH(margeG, margeD, y)); y -= 22;

        c.append(texteCentre("BULLETIN DE NOTES", 16, true, centreX, y)); y -= 16;
        c.append(texteCentre(trimestre.toUpperCase() + " - ANNEE SCOLAIRE " + anneeScolaire(), 10, false, centreX, y));
        y -= 28;

        // Bloc identité
        c.append(texte(margeG, y, 11, true, "Nom et prenom : " + etudiant));
        c.append(texte(340, y, 11, true, "Classe : " + (classeNom == null ? "-" : classeNom)));
        y -= 26;

        // Tableau des notes
        float colMatiere = margeG, colNote = 340, colAppr = 430;
        float hautTableau = y;
        c.append(texte(colMatiere + 5, y - 14, 10, true, "MATIERE"));
        c.append(texte(colNote + 10, y - 14, 10, true, "NOTE/20"));
        c.append(texte(colAppr + 5, y - 14, 10, true, "APPRECIATION"));
        y -= 20;
        c.append(ligneH(margeG, margeD, y));

        for (Note note : notes) {
            c.append(texte(colMatiere + 5, y - 14, 10, false, note.getMatiere()));
            c.append(texte(colNote + 15, y - 14, 10, false, String.format("%.2f", note.getNote())));
            c.append(texte(colAppr + 5, y - 14, 10, false, appreciation(note.getNote())));
            y -= 20;
            c.append(ligneH(margeG, margeD, y));
        }

        float basTableau = y;
        c.append(ligneV(margeG, basTableau, hautTableau));
        c.append(ligneV(colNote, basTableau, hautTableau));
        c.append(ligneV(colAppr, basTableau, hautTableau));
        c.append(ligneV(margeD, basTableau, hautTableau));

        // Résultats
        y -= 26;
        c.append(texte(margeG, y, 12, true, "Moyenne generale : " + String.format("%.2f", moyenne) + " / 20"));
        c.append(texte(330, y, 12, true, "Mention : " + appreciation(moyenne)));
        y -= 20;
        if (rang > 0) {
            c.append(texte(margeG, y, 11, false, "Rang : " + rang + (rang == 1 ? "er" : "e") + " sur " + totalEleves));
            y -= 20;
        }

        // Signatures
        y -= 40;
        c.append(texte(margeG, y, 10, false, "Le Professeur Principal"));
        c.append(texte(370, y, 10, false, "Le Directeur"));
        c.append(ligneH(margeG, margeG + 150, y - 35));
        c.append(ligneH(370, 370 + 150, y - 35));

        return c.toString();
    }

    // ----------------------------------------------------------------------------------------
    // Primitives de dessin PDF bas-niveau (texte + lignes) et écriture du fichier multi-pages.
    // ----------------------------------------------------------------------------------------

    private String texte(float x, float y, float taille, boolean gras, String contenu) {
        return "BT /" + (gras ? "F2" : "F1") + " " + taille + " Tf " + x + " " + y
            + " Td (" + pdfTexte(contenu) + ") Tj ET\n";
    }

    private String texteCentre(String contenu, float taille, boolean gras, float centreX, float y) {
        float x = centreX - (contenu.length() * taille * 0.28f);
        return texte(x, y, taille, gras, contenu);
    }

    private String ligneH(float x1, float x2, float y) {
        return x1 + " " + y + " m " + x2 + " " + y + " l S\n";
    }

    private String ligneV(float x, float y1, float y2) {
        return x + " " + y1 + " m " + x + " " + y2 + " l S\n";
    }

    private void ecrirePdfMultiPages(Path chemin, List<String> pages) throws IOException {
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        int premierObjetPage = 5;

        for (int i = 0; i < pages.size(); i++) {
            int pageObjet = premierObjetPage + i * 2;
            int contenuObjet = pageObjet + 1;
            String contenu = pages.get(i);
            offsets.add(pdf.length());
            pdf.append(pageObjet).append(" 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] ")
                .append("/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents ").append(contenuObjet)
                .append(" 0 R >>\nendobj\n");
            offsets.add(pdf.length());
            pdf.append(contenuObjet).append(" 0 obj\n<< /Length ").append(contenu.length())
                .append(" >>\nstream\n").append(contenu).append("endstream\nendobj\n");
        }

        int pagesOffset = pdf.length();
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [");
        for (int i = 0; i < pages.size(); i++) pdf.append(premierObjetPage + i * 2).append(" 0 R ");
        pdf.append("] /Count ").append(pages.size()).append(" >>\nendobj\n");

        int fontOffset = pdf.length();
        pdf.append("3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        int fontBoldOffset = pdf.length();
        pdf.append("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

        int catalogOffset = pdf.length();
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        int xref = pdf.length();
        int objets = 4 + pages.size() * 2;
        pdf.append("xref\n0 ").append(objets + 1).append("\n0000000000 65535 f \n");

        List<Integer> positions = new ArrayList<>();
        positions.add(catalogOffset);
        positions.add(pagesOffset);
        positions.add(fontOffset);
        positions.add(fontBoldOffset);
        positions.addAll(offsets);
        for (int position : positions) pdf.append(String.format("%010d 00000 n \n", position));

        pdf.append("trailer\n<< /Size ").append(objets + 1).append(" /Root 1 0 R >>\nstartxref\n")
            .append(xref).append("\n%%EOF");

        Files.write(chemin, pdf.toString().getBytes(StandardCharsets.ISO_8859_1));
    }
}