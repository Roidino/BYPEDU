/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univlome.epl.bypedu.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import tg.univlome.epl.bypedu.models.Etudiant;

/**
 *
 * @author Savastano
 */
public class EtudiantDAO implements DAO<Etudiant>{
    private Connection connection = DatabaseConnection.getDatabase();
    
    
    @Override
    public List<Etudiant> getAll() {
    String sql = "SELECT * FROM etudiants";
    List<Etudiant> liste = new ArrayList<>();
    try (PreparedStatement exec = connection.prepareStatement(sql);
         ResultSet result = exec.executeQuery()) {
        while (result.next()) {
            String dateNaissStr = result.getString("date_naissance");
            LocalDate dateNaissance = (dateNaissStr != null && !dateNaissStr.isEmpty()) 
                    ? LocalDate.parse(dateNaissStr) 
                    : null;
            String dateInscStr = result.getString("date_inscription");
            LocalDate dateInscription = (dateInscStr != null && !dateInscStr.isEmpty()) 
                    ? LocalDate.parse(dateInscStr) 
                    : null;
            Etudiant e = new Etudiant(
                    result.getInt("id"),
                    result.getString("nom"),
                    result.getString("prenom"),
                    result.getInt("telephone"),
                    dateNaissance,
                    result.getInt("classe_id"),
                    result.getString("statut"),
                    dateInscription
            );
            liste.add(e);
        }
        
    } catch (SQLException ex) {
        System.err.println("Erreur d'exécution dans EtudiantDAO.getAll() : " + ex.getMessage());
        ex.printStackTrace();
        return new ArrayList<>();
    }
    return liste;
}

    @Override
    public Etudiant getById(int id) {
        String sql = "SELECT * FROM etudiants WHERE id = ?";
        PreparedStatement exec;
        ResultSet result;
        Etudiant e = null;
        try {
            exec = connection.prepareStatement(sql);
            exec.setInt(1, id);
            result = exec.executeQuery();
            if(result.next()){
                e.setId(result.getInt("id"));
                e.setNom(result.getString("nom"));
                e.setPrenom(result.getString("prenom"));
                e.setTelephone(result.getInt("telephone"));
                e.setDate_naissance(result.getDate("date_naissance").toLocalDate());
                e.setClasse_id(result.getInt("classe_id"));
                e.setStatus(result.getString("status"));
                e.setDate_naissance(result.getDate("date_inscription").toLocalDate());
            }
            return e;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE * FROM etudiants WHERE id = ?";
        PreparedStatement exec;
        
        try {
            exec = connection.prepareStatement(sql);
            exec.setInt(1, id);
            int result = exec.executeUpdate();
            if(result == 0){
                return false;
            }
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    @Override
    public boolean ajoute(Etudiant t) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean update(Etudiant t) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
