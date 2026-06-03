/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univlome.epl.bypedu.DAOs;

/**
 *
 * @author Honoré
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import tg.univlome.epl.bypedu.models.Enseignants;

public class EnseignantsDAO implements DAO<Enseignants> {

    // On récupère la connexion unique via la classe de tes camarades
    private final Connection connection = DatabaseConnection.getDatabase();

    @Override
    public List<Enseignants> getAll() {
        List<Enseignants> enseignants = new ArrayList<>();
        String sql = "SELECT * FROM enseignants"; // Vérifie bien le nom de la table dans votre .db

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Enseignants e = new Enseignants();
                e.setId(rs.getInt("id"));
                e.setNom(rs.getString("nom"));
                e.setMatiere(rs.getString("matiere"));
                e.setClasse(rs.getString("classe"));
                e.setEmail(rs.getString("email"));
                e.setTelephone(rs.getString("telephone"));
                
                enseignants.add(e);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération des enseignants : " + ex.getMessage());
        }
        return enseignants;
    }

    @Override
    public Enseignants getById(int id) {
        // On le fera si besoin plus tard
        return null;
    }

    @Override
    public boolean ajoute(Enseignants e) {
        String sql = "INSERT INTO enseignants (nom, matiere, classe, email, telephone) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, e.getNom());
            pstmt.setString(2, e.getMatiere());
            pstmt.setString(3, e.getClasse());
            pstmt.setString(4, e.getEmail());
            pstmt.setString(5, e.getTelephone());
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            System.err.println("Erreur lors de l'ajout de l'enseignant : " + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Enseignants e) {
        // On codera ça à l'étape de modification
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM enseignants WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la suppression de l'enseignant : " + ex.getMessage());
            return false;
        }
    }
}