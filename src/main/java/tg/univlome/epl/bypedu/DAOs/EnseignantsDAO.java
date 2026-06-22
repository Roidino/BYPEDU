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
                   + "m.nom AS nom_matiere, c.nom AS nom_classe "
                   + "FROM enseignants e "
                   + "LEFT JOIN matieres m ON e.matiere_id = m.id "
                   + "LEFT JOIN enseignant_classes ec ON e.id = ec.enseignant_id "
                   + "LEFT JOIN classes c ON ec.classe_id = c.id";

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
                e.setClasse(rs.getString("nom_classe"));
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
        String sqlLiaison = "INSERT INTO enseignant_classes (enseignant_id, classe_id) "
                          + "VALUES (?, (SELECT id FROM classes WHERE nom = ?))";
        
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

            if (idEnseignant != -1 && e.getClasse() != null && !e.getClasse().isEmpty()) {
                String sqlCheckLiaison = "SELECT 1 FROM enseignant_classes WHERE enseignant_id = ? "
                                       + "AND classe_id = (SELECT id FROM classes WHERE nom = ?)";
                boolean liaisonExiste = false;
                try (PreparedStatement pstmtCheckL = connection.prepareStatement(sqlCheckLiaison)) {
                    pstmtCheckL.setInt(1, idEnseignant);
                    pstmtCheckL.setString(2, e.getClasse());
                    try (ResultSet rsL = pstmtCheckL.executeQuery()) {
                        if (rsL.next()) liaisonExiste = true;
                    }
                }

                if (!liaisonExiste) {
                    try (PreparedStatement pstmtLiaison = connection.prepareStatement(sqlLiaison)) {
                        pstmtLiaison.setInt(1, idEnseignant);
                        pstmtLiaison.setString(2, e.getClasse());
                        pstmtLiaison.executeUpdate();
                    }
                } else {
                    connection.rollback();
                    return false;
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

    public boolean deleteAffectation(int idEnseignant, String nomClasse) {
    String sqlCompteAffectations = "SELECT COUNT(*) FROM enseignant_classes WHERE enseignant_id = ?";
    String sqlDeleteLiaison = "DELETE FROM enseignant_classes WHERE enseignant_id = ? "
                            + "AND classe_id = (SELECT id FROM classes WHERE nom = ?)";
    
    if (connection == null) return false;

    try {
        // Étape 1 : Compter combien d'affectations il reste à cet enseignant
        int nombreAffectations = 0;
        try (PreparedStatement pstmtCheck = connection.prepareStatement(sqlCompteAffectations)) {
            pstmtCheck.setInt(1, idEnseignant);
            try (ResultSet rs = pstmtCheck.executeQuery()) {
                if (rs.next()) {
                    nombreAffectations = rs.getInt(1);
                }
            }
        }

        // Étape 2 : Si c'est la toute dernière affectation (1 seule restante),
        // on fait un clean-up complet via la méthode delete() globale pour éviter les blocages de clés étrangères
        if (nombreAffectations <= 1) {
            System.out.println("Dernière affectation détectée. Suppression complète de l'enseignant.");
            return delete(idEnseignant); // Appelle ta méthode delete globale qui gère les PRAGMA foreign_keys
        }

        // Étape 3 : S'il lui reste d'autres classes, on retire juste celle-ci
        try (PreparedStatement pstmtDelete = connection.prepareStatement(sqlDeleteLiaison)) {
            pstmtDelete.setInt(1, idEnseignant);
            pstmtDelete.setString(2, nomClasse);
            return pstmtDelete.executeUpdate() > 0;
        }

    } catch (SQLException ex) {
        System.err.println("Erreur lors du retrait de l'affectation : " + ex.getMessage());
        return false;
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
        String sqlLiaisonsClasses = "DELETE FROM enseignant_classes WHERE enseignant_id = ?";
        String sqlSupprimerCours = "DELETE FROM cours WHERE enseignant_id = ?"; 
        String sqlEnseignant = "DELETE FROM enseignants WHERE id = ?";
        
        if (connection == null) return false;

        try (java.sql.Statement stmtPragma = connection.createStatement()) {
            stmtPragma.execute("PRAGMA foreign_keys = OFF;");
            connection.setAutoCommit(false);

            try (PreparedStatement pstmtClasses = connection.prepareStatement(sqlLiaisonsClasses)) {
                pstmtClasses.setInt(1, idEnseignant);
                pstmtClasses.executeUpdate();
            }

            try (PreparedStatement pstmtCours = connection.prepareStatement(sqlSupprimerCours)) {
                pstmtCours.setInt(1, idEnseignant);
                pstmtCours.executeUpdate();
            }

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