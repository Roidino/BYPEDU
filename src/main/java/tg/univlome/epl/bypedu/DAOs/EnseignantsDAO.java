package tg.univlome.epl.bypedu.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import tg.univlome.epl.bypedu.models.Enseignants;

public class EnseignantsDAO implements DAO<Enseignants> {

    // Connexion unique centralisée
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
                e.setNom(rs.getString("nom"));       // Extrait le nom seul
                e.setPrenom(rs.getString("prenom"));   // Extrait le prénom seul
                e.setEmail(rs.getString("email"));
                e.setTelephone(rs.getString("telephone"));
                e.setMatiere(rs.getString("nom_matiere"));
                e.setClasse(rs.getString("nom_classe"));
                
                liste.add(e);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération avec JOIN : " + ex.getMessage());
        }
        
        return liste;
    }

    @Override
    public Enseignants getById(int id) {
        return null;
    }

   @Override
public boolean ajoute(Enseignants e) {
    String sqlEnseignant = "INSERT INTO enseignants (nom, prenom, email, telephone, matiere_id) "
                         + "VALUES (?, ?, ?, ?, (SELECT id FROM matieres WHERE nom = ?))";
    
    String sqlLiaison = "INSERT INTO enseignant_classes (enseignant_id, classe_id) "
                      + "VALUES (?, (SELECT id FROM classes WHERE nom = ?))";
    
    if (connection == null) return false;

    try {
        // Mode transactionnel pour s'assurer que les deux tables sont mises à jour ensemble
        connection.setAutoCommit(false);

        // 1. Insertion de l'enseignant et récupération de son ID généré automatiquement
        int idGenere = -1;
        try (PreparedStatement pstmt = connection.prepareStatement(sqlEnseignant, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, e.getNom());
            pstmt.setString(2, e.getPrenom());
            pstmt.setString(3, e.getEmail());
            pstmt.setString(4, e.getTelephone());
            pstmt.setString(5, e.getMatiere()); 
            
            int rows = pstmt.executeUpdate();
            if (rows == 0) throw new SQLException("Échec de la création de l'enseignant.");

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idGenere = generatedKeys.getInt(1);
                }
            }
        }

        // 2. Insertion dans la table d'association avec la classe sélectionnée
        if (idGenere != -1 && e.getClasse() != null) {
            try (PreparedStatement pstmtLiaison = connection.prepareStatement(sqlLiaison)) {
                pstmtLiaison.setInt(1, idGenere);
                pstmtLiaison.setString(2, e.getClasse());
                pstmtLiaison.executeUpdate();
            }
        }

        connection.commit();
        connection.setAutoCommit(true);
        return true;

    } catch (SQLException ex) {
        try { connection.rollback(); connection.setAutoCommit(true); } catch (SQLException rollbackEx) {}
        System.err.println("Erreur lors de l'ajout complet (avec classe) : " + ex.getMessage());
        return false;
    }
}

@Override
public boolean update(Enseignants e) {
    String sqlEnseignant = "UPDATE enseignants SET nom = ?, prenom = ?, email = ?, telephone = ?, "
                         + "matiere_id = (SELECT id FROM matieres WHERE nom = ?) WHERE id = ?";
    
    String sqlSupprLiaison = "DELETE FROM enseignant_classes WHERE enseignant_id = ?";
    
    String sqlInsereLiaison = "INSERT INTO enseignant_classes (enseignant_id, classe_id) "
                            + "VALUES (?, (SELECT id FROM classes WHERE nom = ?))";
    
    if (connection == null) return false;

    try {
        connection.setAutoCommit(false);

        // 1. Modifier les infos de base de l'enseignant
        try (PreparedStatement pstmt = connection.prepareStatement(sqlEnseignant)) {
            pstmt.setString(1, e.getNom());
            pstmt.setString(2, e.getPrenom());
            pstmt.setString(3, e.getEmail());
            pstmt.setString(4, e.getTelephone());
            pstmt.setString(5, e.getMatiere());
            pstmt.setInt(6, e.getId());
            pstmt.executeUpdate();
        }

        // 2. Nettoyer l'ancienne classe assignée
        try (PreparedStatement pstmtSuppr = connection.prepareStatement(sqlSupprLiaison)) {
            pstmtSuppr.setInt(1, e.getId());
            pstmtSuppr.executeUpdate();
        }

        // 3. Assigner la nouvelle classe
        if (e.getClasse() != null) {
            try (PreparedStatement pstmtInsere = connection.prepareStatement(sqlInsereLiaison)) {
                pstmtInsere.setInt(1, e.getId());
                pstmtInsere.setString(2, e.getClasse());
                pstmtInsere.executeUpdate();
            }
        }

        connection.commit();
        connection.setAutoCommit(true);
        return true;

    } catch (SQLException ex) {
        try { connection.rollback(); connection.setAutoCommit(true); } catch (SQLException rollbackEx) {}
        System.err.println("Erreur lors de la modification complète : " + ex.getMessage());
        return false;
    }
}

    @Override
    public boolean delete(int idEnseignant) {
        String sqlLiaisons = "DELETE FROM enseignant_classes WHERE enseignant_id = ?";
        String sqlEnseignant = "DELETE FROM enseignants WHERE id = ?";
        
        if (connection == null) return false;

        try {
            connection.setAutoCommit(false);

            // 1. Supprimer d'abord les références dans la table d'association
            try (PreparedStatement pstmt1 = connection.prepareStatement(sqlLiaisons)) {
                pstmt1.setInt(1, idEnseignant);
                pstmt1.executeUpdate();
            }

            // 2. Supprimer définitivement l'enseignant
            int lignesAffectees = 0;
            try (PreparedStatement pstmt2 = connection.prepareStatement(sqlEnseignant)) {
                pstmt2.setInt(1, idEnseignant);
                lignesAffectees = pstmt2.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(true);
            
            return lignesAffectees > 0;

        } catch (SQLException ex) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                System.err.println("Erreur lors du rollback : " + rollbackEx.getMessage());
            }
            System.err.println("Erreur lors de la suppression de l'enseignant : " + ex.getMessage());
            return false;
        }
    }
}