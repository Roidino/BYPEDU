package tg.univlome.epl.bypedu.models;

/**
 * Ligne d'affichage pour le tableau "Inscriptions Récentes" du tableau de bord.
 * Regroupe les informations essentielles d'un étudiant ou d'un enseignant
 * récemment inscrit, indépendamment de son type.
 */
public class InscriptionRow {
    private final String nom;
    private final String classe;
    private final String role;
    private final String date;
    private final String statut;

    public InscriptionRow(String nom, String classe, String role, String date, String statut) {
        this.nom = nom;
        this.classe = classe;
        this.role = role;
        this.date = date;
        this.statut = statut;
    }

    public String getNom() {
        return nom;
    }

    public String getClasse() {
        return classe;
    }

    public String getRole() {
        return role;
    }

    public String getDate() {
        return date;
    }

    public String getStatut() {
        return statut;
    }
}
