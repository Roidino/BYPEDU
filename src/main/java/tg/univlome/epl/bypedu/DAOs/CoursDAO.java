package tg.univlome.epl.bypedu.DAOs;

import tg.univlome.epl.bypedu.Models.Cours;
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
            SELECT c.id, c.nom AS intitule, cl.nom AS classe_nom, 
                   (e.nom || ' ' || e.prenom) AS enseignant_nom, 
                   c.volume_horaire, c.coefficient
            FROM cours c
            LEFT JOIN classes cl ON c.classe_id = cl.id
            INNER JOIN enseignants e ON c.enseignant_id = e.id
        """;
        
        // On récupère la connexion globale sans la mettre dans le try(...) pour ne pas la fermer
        Connection conn = getConnection(); 
        
        // On ne ferme automatiquement que le Statement et le ResultSet
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String classe = rs.getString("classe_nom");
                if (classe == null) {
                    classe = "Toutes classes";
                }
                
                Cours c = new Cours(
                    rs.getInt("id"),
                    rs.getString("intitule"),
                    classe,
                    rs.getString("enseignant_nom"),
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
            SELECT c.id, c.nom AS intitule, cl.nom AS classe_nom, 
                   (e.nom || ' ' || e.prenom) AS enseignant_nom, 
                   c.volume_horaire, c.coefficient
            FROM cours c
            LEFT JOIN classes cl ON c.classe_id = cl.id
            INNER JOIN enseignants e ON c.enseignant_id = e.id
            WHERE c.id = ?
        """;
        
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String classe = rs.getString("classe_nom");
                    if (classe == null) {
                        classe = "Toutes classes";
                    }
                    
                    return new Cours(
                        rs.getInt("id"),
                        rs.getString("intitule"),
                        classe,
                        rs.getString("enseignant_nom"),
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
    
    String sql = """
        INSERT INTO cours (nom, matiere_id, classe_id, enseignant_id, volume_horaire, coefficient) 
        VALUES (
            ?, 
            (SELECT id FROM matieres LIMIT 1), -- Par défaut, prend la 1ère matière ou adapte selon ton besoin
            (SELECT id FROM classes WHERE nom = ?), 
            (SELECT id FROM enseignants WHERE (nom || ' ' || prenom) = ?), 
            ?, ?
        )
    """;
    
    Connection conn = getConnection();
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setString(1, cours.getIntitule());
        stmt.setString(2, cours.getClasse());      // Le nom de la classe issu du ComboBox
        stmt.setString(3, cours.getEnseignant());   // Le nom de l'enseignant issu du ComboBox
        stmt.setInt(4, cours.getVolumeHoraire());
        stmt.setInt(5, cours.getCoefficient());
        
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
        String sql = "UPDATE cours SET nom = ?, volume_horaire = ?, coefficient = ? WHERE id = ?";
        
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cours.getIntitule());
            stmt.setInt(2, cours.getVolumeHoraire());
            stmt.setInt(3, cours.getCoefficient());
            stmt.setInt(4, cours.getId());
            
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
    // Méthode pour récupérer toutes les classes de la BD
public List<String> getAllClasses() {
    List<String> liste = new ArrayList<>();
    String sql = "SELECT nom FROM classes";
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

// Méthode pour récupérer tous les enseignants de la BD
public List<String> getAllEnseignants() {
    List<String> liste = new ArrayList<>();
    String sql = "SELECT (nom || ' ' || prenom) AS nom_complet FROM enseignants";
    Connection conn = getConnection();
    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            liste.add(rs.getString("nom_complet"));
        }
    } catch (SQLException e) {
        System.err.println("Erreur récupération enseignants : " + e.getMessage());
    }
    return liste;
}
}