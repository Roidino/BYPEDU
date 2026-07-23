package tg.univlome.epl.bypedu.DAOs;

import tg.univlome.epl.bypedu.models.Note;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès aux données pour la gestion des notes des étudiants.
 *
 * @author BYPEDU
 */
public class NoteDAO implements DAO<Note> {

    private static final String SQL_BASE =
        "SELECT n.id, n.etudiant_id, (e.nom || ' ' || e.prenom) AS etudiant_nom, " +
        "c.nom AS classe_nom, n.matiere_id, m.nom AS matiere_nom, " +
        "n.note, n.trimestre " +
        "FROM notes n " +
        "JOIN etudiants e ON n.etudiant_id = e.id " +
        "LEFT JOIN classes c ON e.classe_id = c.id " +
        "JOIN matieres m ON n.matiere_id = m.id ";

    private Connection getConnection() {
        return DatabaseConnection.getDatabase();
    }

    @Override
    public List<Note> getAll() {
        List<Note> liste = new ArrayList<>();
        String sql = SQL_BASE + "ORDER BY e.nom, m.nom";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des notes : " + e.getMessage());
        }
        return liste;
    }

    /** Retourne les notes d'un étudiant ou d'une classe pour un trimestre. */
    public List<Note> getBulletin(String etudiant, String classe, String trimestre) {
        List<Note> liste = new ArrayList<>();
        String sql = SQL_BASE
            + "WHERE n.trimestre = ? AND ((? IS NOT NULL AND (e.nom || ' ' || e.prenom) = ?) "
            + "OR (? IS NOT NULL AND c.nom = ?)) ORDER BY e.nom, m.nom";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trimestre);
            stmt.setString(2, etudiant);
            stmt.setString(3, etudiant);
            stmt.setString(4, classe);
            stmt.setString(5, classe);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du bulletin : " + e.getMessage());
        }
        return liste;
    }

    /** Recherche les notes d'un étudiant par une partie de son nom complet, tous trimestres confondus. */
    public List<Note> rechercherBulletinsEtudiant(String recherche) {
        List<Note> liste = new ArrayList<>();
        if (recherche == null || recherche.isBlank()) return liste;
        String sql = SQL_BASE
            + "WHERE LOWER(e.nom || ' ' || e.prenom) LIKE LOWER(?) ORDER BY n.trimestre, m.nom";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + recherche.trim() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des bulletins : " + e.getMessage());
        }
        return liste;
    }

    @Override
    public Note getById(int id) {
        String sql = SQL_BASE + "WHERE n.id = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de la note : " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean ajoute(Note note) {
        if (note == null) return false;
        String sql = "INSERT INTO notes (etudiant_id, matiere_id, note, trimestre) VALUES (?, ?, ?, ?)";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, note.getEtudiantId());
            stmt.setInt(2, note.getMatiereId());
            stmt.setDouble(3, note.getNote());
            stmt.setString(4, note.getTrimestre());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) note.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la note : " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Note note) {
        if (note == null) return false;
        String sql = "UPDATE notes SET etudiant_id = ?, matiere_id = ?, note = ?, trimestre = ? WHERE id = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, note.getEtudiantId());
            stmt.setInt(2, note.getMatiereId());
            stmt.setDouble(3, note.getNote());
            stmt.setString(4, note.getTrimestre());
            stmt.setInt(5, note.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la note : " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM notes WHERE id = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la note : " + e.getMessage());
        }
        return false;
    }

    /** Liste "Nom Prénom" de tous les étudiants, pour peupler un ComboBox. */
    public List<String> getAllEtudiants() {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT (nom || ' ' || prenom) AS nom_complet FROM etudiants ORDER BY nom";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                liste.add(rs.getString("nom_complet"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération étudiants : " + e.getMessage());
        }
        return liste;
    }

    /** Liste des matières, pour peupler un ComboBox. */
    public List<String> getAllMatieres() {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT nom FROM matieres ORDER BY nom";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                liste.add(rs.getString("nom"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération matières : " + e.getMessage());
        }
        return liste;
    }

    public List<String> getAllClasses() {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT nom FROM classes ORDER BY niveau, nom";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) liste.add(rs.getString("nom"));
        } catch (SQLException e) {
            System.err.println("Erreur récupération classes : " + e.getMessage());
        }
        return liste;
    }

    /** Retrouve l'id d'un étudiant à partir de son "Nom Prénom" affiché. */
    public int getEtudiantIdParNomComplet(String nomComplet) {
        if (nomComplet == null) return 0;
        String sql = "SELECT id FROM etudiants WHERE (nom || ' ' || prenom) = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomComplet);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération id étudiant : " + e.getMessage());
        }
        return 0;
    }

    /** Retrouve l'id d'une matière à partir de son nom. */
    public int getMatiereIdParNom(String nom) {
        if (nom == null) return 0;
        String sql = "SELECT id FROM matieres WHERE nom = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nom);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération id matière : " + e.getMessage());
        }
        return 0;
    }

    private Note mapResultSet(ResultSet rs) throws SQLException {
        return new Note(
            rs.getInt("id"),
            rs.getInt("etudiant_id"),
            rs.getString("etudiant_nom"),
            rs.getString("classe_nom"),
            rs.getInt("matiere_id"),
            rs.getString("matiere_nom"),
            rs.getDouble("note"),
            rs.getString("trimestre"),
            null
        );
    }
}
