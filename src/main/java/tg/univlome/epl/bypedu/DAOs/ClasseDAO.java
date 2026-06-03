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
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, c.getNom());
            ps.setString(2, c.getNiveau());
            return ps.executeUpdate() > 0;
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

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM classes WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private Classe mapResultSet(ResultSet rs) throws SQLException {
        return new Classe(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getString("niveau")
        );
    }
}
