package tg.univlome.epl.bypedu.DAOs;

import tg.univlome.epl.bypedu.models.Creneau;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès aux données pour la gestion de l'emploi du temps.
 *
 * @author BYPEDU
 */
public class EmploiDAO implements DAO<Creneau> {

    private static final String SQL_BASE =
        "SELECT ed.id, ed.classe_id, cl.nom AS classe_nom, ed.cours_id, co.nom AS cours_nom, " +
        "(en.nom || ' ' || en.prenom) AS enseignant_nom, ed.jour, ed.heure_debut, ed.heure_fin, ed.salle " +
        "FROM emploi_du_temps ed " +
        "JOIN classes cl ON ed.classe_id = cl.id " +
        "JOIN cours co ON ed.cours_id = co.id " +
        "LEFT JOIN classe_matieres cm ON cm.classe_id = ed.classe_id AND cm.matiere_id = co.matiere_id " +
        "LEFT JOIN enseignants en ON cm.enseignant_id = en.id ";

    private Connection getConnection() {
        return DatabaseConnection.getDatabase();
    }

    @Override
    public List<Creneau> getAll() {
        List<Creneau> liste = new ArrayList<>();
        String sql = SQL_BASE + "ORDER BY ed.jour, ed.heure_debut";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'emploi du temps : " + e.getMessage());
        }
        return liste;
    }

    /** Récupère uniquement les créneaux d'une classe donnée. */
    public List<Creneau> getByClasse(int classeId) {
        List<Creneau> liste = new ArrayList<>();
        String sql = SQL_BASE + "WHERE ed.classe_id = ? ORDER BY ed.jour, ed.heure_debut";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, classeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'emploi du temps : " + e.getMessage());
        }
        return liste;
    }

    @Override
    public Creneau getById(int id) {
        String sql = SQL_BASE + "WHERE ed.id = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du créneau : " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean ajoute(Creneau c) {
        if (c == null) return false;
        String sql = "INSERT INTO emploi_du_temps (classe_id, cours_id, jour, heure_debut, heure_fin, salle) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, c.getClasseId());
            stmt.setInt(2, c.getCoursId());
            stmt.setString(3, c.getJour());
            stmt.setString(4, c.getHeureDebut());
            stmt.setString(5, c.getHeureFin());
            stmt.setString(6, c.getSalle());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) c.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du créneau : " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Creneau c) {
        if (c == null) return false;
        String sql = "UPDATE emploi_du_temps SET classe_id = ?, cours_id = ?, jour = ?, " +
                     "heure_debut = ?, heure_fin = ?, salle = ? WHERE id = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, c.getClasseId());
            stmt.setInt(2, c.getCoursId());
            stmt.setString(3, c.getJour());
            stmt.setString(4, c.getHeureDebut());
            stmt.setString(5, c.getHeureFin());
            stmt.setString(6, c.getSalle());
            stmt.setInt(7, c.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du créneau : " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM emploi_du_temps WHERE id = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du créneau : " + e.getMessage());
        }
        return false;
    }

    /** Liste des classes (nom) pour peupler un ComboBox. */
    public List<String> getAllClasses() {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT nom FROM classes ORDER BY nom";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                liste.add(rs.getString("nom"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération classes : " + e.getMessage());
        }
        return liste;
    }

    /** Liste des cours disponibles pour une classe donnée (nom du cours). */
    public List<String> getCoursParClasse(String nomClasse) {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT co.nom FROM cours co "
               + "JOIN classes cl ON cl.nom = ? "
               + "JOIN classe_matieres cm ON cm.classe_id = cl.id AND cm.matiere_id = co.matiere_id "
               + "ORDER BY co.nom";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomClasse);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(rs.getString("nom"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération cours : " + e.getMessage());
        }
        return liste;
    }

    public int getClasseIdParNom(String nom) {
        return getIdParNom("classes", nom);
    }

    public int getCoursIdParNom(String nom) {
        return getIdParNom("cours", nom);
    }

    private int getIdParNom(String table, String nom) {
        if (nom == null) return 0;
        String sql = "SELECT id FROM " + table + " WHERE nom = ? LIMIT 1";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nom);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération id (" + table + ") : " + e.getMessage());
        }
        return 0;
    }

    private Creneau mapResultSet(ResultSet rs) throws SQLException {
        return new Creneau(
            rs.getInt("id"),
            rs.getInt("classe_id"),
            rs.getString("classe_nom"),
            rs.getInt("cours_id"),
            rs.getString("cours_nom"),
            rs.getString("enseignant_nom"),
            rs.getString("jour"),
            rs.getString("heure_debut"),
            rs.getString("heure_fin"),
            rs.getString("salle")
        );
    }
}
