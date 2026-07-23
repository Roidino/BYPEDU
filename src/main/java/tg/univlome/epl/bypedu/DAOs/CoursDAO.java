package tg.univlome.epl.bypedu.DAOs;

import tg.univlome.epl.bypedu.models.Cours;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Terence PEKPELI
 */
public class CoursDAO implements DAO<Cours> {

    private Connection getConnection() {
        return DatabaseConnection.getDatabase();
    }

    @Override
    public List<Cours> getAll() {
        List<Cours> coursList = new ArrayList<>();
        String sql = """
             SELECT c.id, c.nom AS intitule, m.nom AS matiere_nom,
                 c.volume_horaire, c.coefficient
            FROM cours c
             INNER JOIN matieres m ON c.matiere_id = m.id
        """;
        
        // On récupère la connexion globale sans la mettre dans le try(...) pour ne pas la fermer
        Connection conn = getConnection(); 
        
        // On ne ferme automatiquement que le Statement et le ResultSet
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Cours c = new Cours(
                    rs.getInt("id"),
                    rs.getString("intitule"),
                    rs.getString("matiere_nom"),
                    rs.getInt("volume_horaire"),
                    rs.getInt("coefficient")
                );
                coursList.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des cours : " + e.getMessage());
        }
        return coursList;
    }

    @Override
    public Cours getById(int id) {
        String sql = """
             SELECT c.id, c.nom AS intitule, m.nom AS matiere_nom,
                 c.volume_horaire, c.coefficient
            FROM cours c
             INNER JOIN matieres m ON c.matiere_id = m.id
            WHERE c.id = ?
        """;
        
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Cours(
                        rs.getInt("id"),
                        rs.getString("intitule"),
                        rs.getString("matiere_nom"),
                        rs.getInt("volume_horaire"),
                        rs.getInt("coefficient")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du cours : " + e.getMessage());
        }
        return null;
    }

    @Override
public boolean ajoute(Cours cours) {
    if (cours == null) return false;
    
    String sql = "INSERT INTO cours (nom, matiere_id, volume_horaire, coefficient) VALUES (?, (SELECT id FROM matieres WHERE nom = ?), ?, ?)";
    
    Connection conn = getConnection();
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setString(1, cours.getIntitule());
        stmt.setString(2, cours.getMatiere());
        stmt.setInt(3, cours.getVolumeHoraire());
        stmt.setInt(4, cours.getCoefficient());
        
        int rowsInserted = stmt.executeUpdate();
        if (rowsInserted > 0) {
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cours.setId(generatedKeys.getInt(1));
                }
            }
            return true;
        }
    } catch (SQLException e) {
        System.err.println("Erreur lors de l'ajout du cours : " + e.getMessage());
    }
    return false;
}
    @Override
    public boolean update(Cours cours) {
        if (cours == null) return false;
        String sql = "UPDATE cours SET nom = ?, matiere_id = (SELECT id FROM matieres WHERE nom = ?), volume_horaire = ?, coefficient = ? WHERE id = ?";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cours.getIntitule());
            stmt.setString(2, cours.getMatiere());
            stmt.setInt(3, cours.getVolumeHoraire());
            stmt.setInt(4, cours.getCoefficient());
            stmt.setInt(5, cours.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du cours : " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM cours WHERE id = ?";
        
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du cours : " + e.getMessage());
        }
        return false;
    }
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
}