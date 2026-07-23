/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univlome.epl.bypedu.DAOs;

/**
 *
 * @author Savastano
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import tg.univlome.epl.bypedu.models.Classe;

public class ClasseDAO implements DAO<Classe> {

    private Connection connection = DatabaseConnection.getDatabase();

    @Override
    public List<Classe> getAll() {
        List<Classe> liste = new ArrayList<>();
        String sql = "SELECT id, nom, niveau FROM classes ORDER BY niveau, nom";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {          // ← while pas if
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return liste;                    // ← retourne liste vide si erreur, jamais null
    }

    @Override
    public Classe getById(int id) {
        String sql = "SELECT id, nom, niveau FROM classes WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean ajoute(Classe c) {
        String sql = "INSERT INTO classes (nom, niveau) VALUES (?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getNom());
            ps.setString(2, c.getNiveau());
            if (ps.executeUpdate() == 0) return false;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
            return c.getId() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Classe c) {
        String sql = "UPDATE classes SET nom = ?, niveau = ? WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, c.getNom());
            ps.setString(2, c.getNiveau());
            ps.setInt(3, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Supprime une classe même si elle a encore des matières/professeurs affectés
     * ou des élèves inscrits :
     *  - les affectations (classe_matieres) liées à la classe sont supprimées ;
     *  - les élèves de la classe sont détachés (classe_id remis à NULL), pas supprimés ;
     *  - puis la classe elle-même est supprimée.
     * Le tout dans une seule transaction : si une étape échoue, rien n'est appliqué.
     */
    @Override
    public boolean delete(int id) {
        String supprimerCreneaux = "DELETE FROM emploi_du_temps WHERE classe_id = ?";
        String supprimerAffectations = "DELETE FROM classe_matieres WHERE classe_id = ?";
        String detacherEtudiants = "UPDATE etudiants SET classe_id = NULL WHERE classe_id = ?";
        String supprimerClasse = "DELETE FROM classes WHERE id = ?";
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement psCreneaux = connection.prepareStatement(supprimerCreneaux)) {
                psCreneaux.setInt(1, id);
                psCreneaux.executeUpdate();
            }

            try (PreparedStatement psAffectations = connection.prepareStatement(supprimerAffectations)) {
                psAffectations.setInt(1, id);
                psAffectations.executeUpdate();
            }

            try (PreparedStatement psEtudiants = connection.prepareStatement(detacherEtudiants)) {
                psEtudiants.setInt(1, id);
                psEtudiants.executeUpdate();
            }

            int lignesSupprimees;
            try (PreparedStatement psClasse = connection.prepareStatement(supprimerClasse)) {
                psClasse.setInt(1, id);
                lignesSupprimees = psClasse.executeUpdate();
            }

            connection.commit();
            return lignesSupprimees > 0;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            ex.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public List<String> getAllMatieres() {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT nom FROM matieres ORDER BY nom";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(rs.getString("nom"));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return liste;
    }

    public Map<Integer, String> getMatieres() {
        Map<Integer, String> liste = new LinkedHashMap<>();
        String sql = "SELECT id, nom FROM matieres ORDER BY nom";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.put(rs.getInt("id"), rs.getString("nom"));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return liste;
    }

    public List<TeacherOption> getEnseignantsParMatiere(int matiereId) {
        List<TeacherOption> liste = new ArrayList<>();
        String sql = "SELECT id, nom || ' ' || prenom AS nom_complet FROM enseignants WHERE matiere_id = ? ORDER BY nom, prenom";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, matiereId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(new TeacherOption(rs.getInt("id"), rs.getString("nom_complet")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return liste;
    }

    public Map<Integer, Integer> getAffectations(int classeId) {
        Map<Integer, Integer> affectations = new LinkedHashMap<>();
        String sql = "SELECT matiere_id, enseignant_id FROM classe_matieres WHERE classe_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int enseignantId = rs.getInt("enseignant_id");
                    if (!rs.wasNull()) affectations.put(rs.getInt("matiere_id"), enseignantId);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return affectations;
    }

    public int getNombreAffectations(int classeId) {
        String sql = "SELECT COUNT(*) FROM classe_matieres WHERE classe_id = ? AND enseignant_id IS NOT NULL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            return 0;
        }
    }

    public List<Affectation> getAffectationsDetails(int classeId) {
        List<Affectation> liste = new ArrayList<>();
        String sql = "SELECT m.nom AS matiere, e.nom || ' ' || e.prenom AS enseignant "
                   + "FROM classe_matieres cm JOIN matieres m ON m.id = cm.matiere_id "
                   + "LEFT JOIN enseignants e ON e.id = cm.enseignant_id "
                   + "WHERE cm.classe_id = ? ORDER BY m.nom";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, classeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Affectation(rs.getString("matiere"), rs.getString("enseignant")));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return liste;
    }

    public boolean remplacerAffectations(int classeId, Map<Integer, Integer> affectations) {
        String delete = "DELETE FROM classe_matieres WHERE classe_id = ?";
        String insert = "INSERT INTO classe_matieres (classe_id, matiere_id, enseignant_id) VALUES (?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement psDelete = connection.prepareStatement(delete)) {
                psDelete.setInt(1, classeId);
                psDelete.executeUpdate();
            }
            try (PreparedStatement psInsert = connection.prepareStatement(insert)) {
                for (Map.Entry<Integer, Integer> affectation : affectations.entrySet()) {
                    psInsert.setInt(1, classeId);
                    psInsert.setInt(2, affectation.getKey());
                    psInsert.setInt(3, affectation.getValue());
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }
            connection.commit();
            return true;
        } catch (SQLException ex) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    public static record TeacherOption(int id, String nom) {
        @Override public String toString() { return nom; }
    }

    public static record Affectation(String matiere, String enseignant) {}

    private Classe mapResultSet(ResultSet rs) throws SQLException {
        return new Classe(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getString("niveau")
        );
    }
}