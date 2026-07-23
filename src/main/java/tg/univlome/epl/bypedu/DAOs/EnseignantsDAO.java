package tg.univlome.epl.bypedu.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import tg.univlome.epl.bypedu.models.Enseignants;

public class EnseignantsDAO implements DAO<Enseignants> {

    private final Connection connection = DatabaseConnection.getDatabase();

    @Override
    public List<Enseignants> getAll() {
        List<Enseignants> liste = new ArrayList<>();
        String sql = "SELECT e.id, e.nom, e.prenom, e.email, e.telephone, "
                   + "m.nom AS nom_matiere "
                   + "FROM enseignants e "
                   + "LEFT JOIN matieres m ON e.matiere_id = m.id";

        if (connection == null) return liste;

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Enseignants e = new Enseignants();
                e.setId(rs.getInt("id"));
                e.setNom(rs.getString("nom"));
                e.setPrenom(rs.getString("prenom"));
                e.setEmail(rs.getString("email"));
                e.setTelephone(rs.getString("telephone"));
                e.setMatiere(rs.getString("nom_matiere"));
                liste.add(e);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur getAll : " + ex.getMessage());
        }
        return liste;
    }

    @Override
    public boolean ajoute(Enseignants e) {
        String sqlSelectId = "SELECT id FROM enseignants WHERE email = ?";
        String sqlEnseignant = "INSERT INTO enseignants (nom, prenom, email, telephone, matiere_id) "
                             + "VALUES (?, ?, ?, ?, (SELECT id FROM matieres WHERE nom = ?))";
        
        if (connection == null) return false;

        try {
            connection.setAutoCommit(false);
            int idEnseignant = -1;

            try (PreparedStatement pstmtCheck = connection.prepareStatement(sqlSelectId)) {
                pstmtCheck.setString(1, e.getEmail());
                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        idEnseignant = rs.getInt("id"); 
                    }
                }
            }

            if (idEnseignant == -1) {
                try (PreparedStatement pstmt = connection.prepareStatement(sqlEnseignant, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, e.getNom());
                    pstmt.setString(2, e.getPrenom());
                    pstmt.setString(3, e.getEmail());
                    pstmt.setString(4, e.getTelephone());
                    pstmt.setString(5, e.getMatiere());
                    pstmt.executeUpdate();

                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) idEnseignant = generatedKeys.getInt(1);
                    }
                }
            }

            connection.commit();
            return true;
        } catch (SQLException ex) {
            try { connection.rollback(); } catch (SQLException rollbackEx) {}
            System.err.println("Erreur ajoute : " + ex.getMessage());
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ex) {}
        }
    }

    @Override
    public boolean update(Enseignants e) {
        String sqlCheckEmail = "SELECT id FROM enseignants WHERE email = ? AND id != ?";
        String sqlEnseignant = "UPDATE enseignants SET nom = ?, prenom = ?, email = ?, telephone = ?, "
                             + "matiere_id = (SELECT id FROM matieres WHERE nom = ?) WHERE id = ?";
        
        if (connection == null) return false;

        try {
            try (PreparedStatement pstmtCheck = connection.prepareStatement(sqlCheckEmail)) {
                pstmtCheck.setString(1, e.getEmail());
                pstmtCheck.setInt(2, e.getId());
                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        System.err.println("Erreur : Cet email appartient déjà à un autre profil.");
                        return false; 
                    }
                }
            }

            try (PreparedStatement pstmt = connection.prepareStatement(sqlEnseignant)) {
                pstmt.setString(1, e.getNom());
                pstmt.setString(2, e.getPrenom());
                pstmt.setString(3, e.getEmail());
                pstmt.setString(4, e.getTelephone());
                pstmt.setString(5, e.getMatiere()); 
                pstmt.setInt(6, e.getId());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            System.err.println("Erreur update : " + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int idEnseignant) {
        String sqlEnseignant = "DELETE FROM enseignants WHERE id = ?";
        
        if (connection == null) return false;

        try (java.sql.Statement stmtPragma = connection.createStatement()) {
            stmtPragma.execute("PRAGMA foreign_keys = OFF;");
            connection.setAutoCommit(false);

            int lignesAffectees = 0;
            try (PreparedStatement pstmtEnseignant = connection.prepareStatement(sqlEnseignant)) {
                pstmtEnseignant.setInt(1, idEnseignant);
                lignesAffectees = pstmtEnseignant.executeUpdate();
            }

            connection.commit();
            stmtPragma.execute("PRAGMA foreign_keys = ON;");
            connection.setAutoCommit(true);

            return lignesAffectees > 0;

        } catch (SQLException ex) {
            try {
                connection.rollback();
                try (java.sql.Statement stmtReset = connection.createStatement()) {
                    stmtReset.execute("PRAGMA foreign_keys = ON;");
                }
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {}
            System.err.println("Erreur lors de la suppression complète : " + ex.getMessage());
            return false;
        }
    }

    @Override
    public Enseignants getById(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}