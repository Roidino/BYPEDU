package tg.univlome.epl.bypedu.DAOs;

import tg.univlome.epl.bypedu.Models.Notes;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des notes.
 * Corrigé : jointure sur la table "matieres" (et non "cours")
 * pour correspondre au schéma réel de la base de données.
 *
 * @author Terence PEKPELI
 */
public class NotesDAO implements DAO<Notes> {

    private Connection getConnection() {
        return DatabaseConnection.getDatabase();
    }

    /**
     * Retourne la liste de toutes les matières distinctes qui ont des notes,
     * triées par nom. Ces noms servent de colonnes dynamiques dans le tableau.
     */
    public List<String> getListeDesMatieres() {
        List<String> matieres = new ArrayList<>();
        // On ne sélectionne que les matières qui ont réellement des notes
        // pour éviter des colonnes vides inutiles.
        String sql = "SELECT DISTINCT m.nom FROM matieres m "
                   + "INNER JOIN notes n ON n.matiere_id = m.id "
                   + "ORDER BY m.nom ASC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                matieres.add(rs.getString("nom"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur liste matières : " + e.getMessage());
        }
        return matieres;
    }

    /**
     * Construit un tableau pivot : une ligne par étudiant actif,
     * une colonne par matière, avec moyenne générale et tendance.
     *
     * @param trimestreCode  "T1", "T2" ou "T3"
     * @param classeNom      nom de classe ou "Toutes les classes" pour tout afficher
     */
    public List<Notes> getNotesSynthese(String trimestreCode, String classeNom) {
        List<Notes> liste = new ArrayList<>();
        List<String> toutesLesMatieres = getListeDesMatieres();

        boolean filtrerClasse = classeNom != null
                && !classeNom.isEmpty()
                && !"Toutes les classes".equals(classeNom);

        // ── Construction du pivot SQL ─────────────────────────────────────────
        // Chaque matière devient une colonne MAX(CASE WHEN ...) pour simuler
        // un PIVOT, car SQLite ne dispose pas du mot-clé PIVOT natif.
        StringBuilder sql = new StringBuilder(
            "SELECT e.id, "
          + "e.nom || ' ' || e.prenom AS etudiant_nom, "
          + "cl.nom AS classe_nom, "
        );

        for (int i = 0; i < toutesLesMatieres.size(); i++) {
            sql.append("MAX(CASE WHEN m.nom = ? THEN n.note END) AS mat_").append(i).append(", ");
        }

        // Tendance : on prend la tendance majoritaire de l'étudiant pour ce trimestre
        sql.append("AVG(n.note) AS moyenne_generale, ");
        sql.append("MAX(n.tendance) AS tendance_eleve ");
        sql.append("FROM etudiants e ");
        sql.append("INNER JOIN classes cl ON e.classe_id = cl.id ");
        // LEFT JOIN pour garder les étudiants même sans aucune note ce trimestre
        sql.append("LEFT JOIN notes n ON e.id = n.etudiant_id AND n.trimestre = ? ");
        // CORRECTION : jointure sur matieres (et non cours)
        sql.append("LEFT JOIN matieres m ON n.matiere_id = m.id ");
        sql.append("WHERE e.statut = 'ACTIF' ");

        if (filtrerClasse) {
            sql.append("AND cl.nom = ? ");
        }
        sql.append("GROUP BY e.id, cl.nom ");
        sql.append("ORDER BY moyenne_generale DESC NULLS LAST");

        try (PreparedStatement stmt = getConnection().prepareStatement(sql.toString())) {
            int p = 1;
            // Paramètres des CASE WHEN (un par matière)
            for (String mat : toutesLesMatieres) {
                stmt.setString(p++, mat);
            }
            // Paramètre du trimestre
            stmt.setString(p++, trimestreCode);
            // Paramètre optionnel de la classe
            if (filtrerClasse) {
                stmt.setString(p, classeNom);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double moyenne = rs.getObject("moyenne_generale") != null
                            ? rs.getDouble("moyenne_generale") : 0.0;
                    String tendance = rs.getString("tendance_eleve") != null
                            ? rs.getString("tendance_eleve") : "STABLE";

                    Notes note = new Notes(
                            rs.getString("etudiant_nom"),
                            rs.getString("classe_nom"),
                            moyenne,
                            tendance
                    );

                    // Peuplement dynamique des notes par matière
                    for (int i = 0; i < toutesLesMatieres.size(); i++) {
                        String nomMatiere = toutesLesMatieres.get(i);
                        Double val = rs.getObject("mat_" + i) != null
                                ? rs.getDouble("mat_" + i) : null;
                        note.ajouterNote(nomMatiere, val);
                    }
                    liste.add(note);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur synthèse notes : " + e.getMessage());
        }
        return liste;
    }

    /** Retourne toutes les classes, avec l'option "Toutes les classes" en tête. */
    public List<String> getAllClasses() {
        List<String> liste = new ArrayList<>();
        liste.add("Toutes les classes");
        String sql = "SELECT nom FROM classes ORDER BY nom ASC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                liste.add(rs.getString("nom"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur classes : " + e.getMessage());
        }
        return liste;
    }

    // ── Méthodes de l'interface DAO ──────────────────────────────────────────

    @Override
    public List<Notes> getAll() {
        return getNotesSynthese("T1", "Toutes les classes");
    }

    @Override public Notes getById(int id) { return null; }
    @Override public boolean ajoute(Notes objet) { return false; }
    @Override public boolean update(Notes objet) { return false; }
    @Override public boolean delete(int id) { return false; }
}