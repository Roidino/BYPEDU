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
    private static final String SQL_BASE =
        "SELECT e.id, e.nom, e.prenom, e.telephone, " +
        "e.date_naissance, e.classe_id, e.statut, e.date_inscription, " +
        "c.nom AS nom_classe, " +
        "COALESCE(ROUND(AVG(n.note), 2), 0.0) AS moyenne " +
        "FROM etudiants e " +
        "LEFT JOIN classes c ON e.classe_id = c.id " +
        "LEFT JOIN notes n ON e.id = n.etudiant_id ";

    @Override
    public List<Etudiant> getAll() {
        List<Etudiant> liste = new ArrayList<>();
        String sql = SQL_BASE + "GROUP BY e.id ORDER BY e.nom";
        PreparedStatement exec;
        ResultSet result;
        try {
            exec = connection.prepareStatement(sql);
            result = exec.executeQuery();
            while(result.next()) {
                liste.add(creeEtudiant(result));
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); 
        }
        return liste;
    }

    @Override
    public Etudiant getById(int id) {
        String sql = SQL_BASE + "WHERE e.id = ?";
        PreparedStatement exec;
        ResultSet result;
        try {
            exec = connection.prepareStatement(sql);
            exec.setInt(1, id);
            result = exec.executeQuery();
            if(result.next()){
                return creeEtudiant(result);
            }
        } catch (SQLException ex) { 
            ex.printStackTrace(); 
        }
        return null;
    }

    public List<Etudiant> search(String terme) {
        List<Etudiant> liste = new ArrayList<>();
        String sql = SQL_BASE +
            "WHERE e.nom LIKE ? OR e.prenom LIKE ? " +
            "GROUP BY e.id ORDER BY e.nom";
        PreparedStatement exec;
        ResultSet result;
        try {
            exec = connection.prepareStatement(sql);
            exec.setString(1, "%" + terme + "%");
            exec.setString(2, "%" + terme + "%");
            result = exec.executeQuery();
            if(result.next()){
                liste.add(creeEtudiant(result));
            }
        } catch (SQLException ex) { 
            ex.printStackTrace(); 
        }
        return liste;
    }

    public List<Etudiant> getByClasse(int classeId) {
        List<Etudiant> liste = new ArrayList<>();
        String sql = SQL_BASE +
            "WHERE e.classe_id = ? GROUP BY e.id ORDER BY e.nom";
        PreparedStatement exec;
        ResultSet result;
        try {
            exec = connection.prepareStatement(sql);
            exec.setInt(1, classeId);
            result = exec.executeQuery();
            if(result.next()){
                liste.add(creeEtudiant(result));
            }
        } catch (SQLException ex) { 
            ex.printStackTrace(); 
        }
        return liste;
    }
    
    @Override
    public boolean ajoute(Etudiant e) {
        String sql =
            "INSERT INTO etudiants (nom, prenom, telephone, date_naissance, " +
            "classe_id, statut, date_inscription) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement exec;
        try {
            exec = connection.prepareStatement(sql);
            exec.setString(1, e.getNom());
            exec.setString(2, e.getPrenom());
            exec.setString(3, e.getTelephone());
            exec.setString(4, e.getDate_naissance() != null
                    ? e.getDate_naissance().toString() : null);
            exec.setInt(5, e.getClasse_id());
            exec.setString(6, e.getStatus());
            exec.setString(7, LocalDate.now().toString());
            exec.executeUpdate();
            try (ResultSet keys = exec.getGeneratedKeys()) {
                if (keys.next()) e.setId(keys.getInt(1));
            }
            return true;
            
        } catch (SQLException ex) { 
            ex.printStackTrace(); 
        }
        return false;
    }

    @Override
    public boolean update(Etudiant e) {
    String sql =
        "UPDATE etudiants SET nom = ?, prenom = ?, telephone = ?, " +
        "date_naissance = ?, classe_id = ?, statut = ? " +
        "WHERE id = ?";
    try {
        PreparedStatement exec = connection.prepareStatement(sql);
        exec.setString(1, e.getNom());
        exec.setString(2, e.getPrenom());
        exec.setString(3, e.getTelephone());
        exec.setString(4, e.getDate_naissance() != null
                ? e.getDate_naissance().toString() : null);
        exec.setInt(5, e.getClasse_id());
        exec.setString(6, e.getStatus());
        exec.setInt(7, e.getId());      // ← l'id de l'étudiant à modifier
        return exec.executeUpdate() > 0;
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return false;
}

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM etudiants WHERE id = ?";
        PreparedStatement exec;
        try {
            exec = connection.prepareStatement(sql);
            exec.setInt(1, id);
            return  exec.executeUpdate() > 0;
        } catch (SQLException ex) { 
            ex.printStackTrace(); 
        }
        return false; 
    }

    private Etudiant creeEtudiant(ResultSet result) throws SQLException {
        Etudiant e = new Etudiant();
        e.setId(result.getInt("id"));
        e.setNom(result.getString("nom"));
        e.setPrenom(result.getString("prenom"));
        e.setTelephone(result.getString("telephone"));
        e.setStatus(result.getString("statut"));
        e.setClasse_id(result.getInt("classe_id"));
        e.setClasse(result.getString("nom_classe"));
        e.setMoyenne(result.getDouble("moyenne"));
        
        String dn = result.getString("date_naissance");
        if (dn != null){
            e.setDate_naissance(LocalDate.parse(dn));
        }
        String di = result.getString("date_inscription");
        if (di != null) {
            e.setDate_inscription(LocalDate.parse(di));
        }
        return e;
    }
}
