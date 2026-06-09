package tg.univlome.epl.bypedu.Models;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Terence PEKPELI
 */
public class Notes {
    private String nomEtudiant;
    private String classe;
    private Map<String, Double> notesParCours; // Stocke dynamiquement "Nom du Cours" -> Note
    private double moyenne;
    private String tendance;

    public Notes() {
        this.notesParCours = new HashMap<>();
    }

    public Notes(String nomEtudiant, String classe, double moyenne, String tendance) {
        this.nomEtudiant = nomEtudiant;
        this.classe = classe;
        this.notesParCours = new HashMap<>();
        this.moyenne = moyenne;
        this.tendance = tendance;
    }

    public String getNomEtudiant() { return nomEtudiant; }
    public void setNomEtudiant(String nomEtudiant) { this.nomEtudiant = nomEtudiant; }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    public double getMoyenne() { return moyenne; }
    public void setMoyenne(double moyenne) { this.moyenne = moyenne; }

    public String getTendance() { return tendance; }
    public void setTendance(String tendance) { this.tendance = tendance; }

    public Map<String, Double> getNotesParCours() { return notesParCours; }
    public void ajouterNote(String coursNom, Double note) {
        this.notesParCours.put(coursNom, note);
    }
}