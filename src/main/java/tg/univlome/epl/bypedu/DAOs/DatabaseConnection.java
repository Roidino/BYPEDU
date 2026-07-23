package tg.univlome.epl.bypedu.DAOs;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {

    private static Connection instance = null;

    public static Connection getDatabase() {
        if (instance == null) {
            try {
                // 1. Définir le dossier d'application dans le répertoire utilisateur
                String userHome = System.getProperty("user.home");
                File appDir = new File(userHome, ".bypedu");
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }

                File dbFile = new File(appDir, "database.db");

                // 2. Si le fichier n'existe pas encore, le copier depuis les ressources du JAR
                if (!dbFile.exists()) {
                    try (InputStream is = DatabaseConnection.class.getResourceAsStream("/tg/univlome/epl/bypedu/database.db")) {
                        if (is != null) {
                            Files.copy(is, dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // 3. Connexion à la base de données dans le dossier utilisateur
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                instance = DriverManager.getConnection(url);
                instance.createStatement().execute("PRAGMA foreign_keys = ON");
                
                initialiserSchema(instance);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return instance;
    }

    private static void initialiserSchema(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS classe_matieres (
                    classe_id INTEGER NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
                    matiere_id INTEGER NOT NULL REFERENCES matieres(id) ON DELETE CASCADE,
                    enseignant_id INTEGER REFERENCES enseignants(id) ON DELETE SET NULL,
                    PRIMARY KEY (classe_id, matiere_id)
                )
                """);
            statement.executeUpdate("UPDATE classes SET nom = 'Terminale C' WHERE nom = 'Terminale S'");
            statement.executeUpdate("UPDATE classes SET nom = 'Terminale D' WHERE nom = 'Terminale ES'");
            statement.executeUpdate("UPDATE classes SET nom = 'Terminale A4' WHERE nom = 'Terminale L'");
            statement.executeUpdate("UPDATE classes SET nom = 'Première C' WHERE nom = 'Première ES'");
            statement.executeUpdate("UPDATE classes SET nom = 'Première D' WHERE nom = 'Première L'");
            statement.executeUpdate("UPDATE classes SET nom = 'Première A4' WHERE nom = 'Première S'");
            statement.executeUpdate("UPDATE classes SET nom = 'Seconde C' WHERE nom = 'Seconde B'");
            statement.executeUpdate("DROP TABLE IF EXISTS Enseignat_matiere");
            statement.executeUpdate("DROP TABLE IF EXISTS enseignant_classes");
            statement.executeUpdate("""
                INSERT INTO enseignants (nom, prenom, email, telephone, matiere_id, statut)
                SELECT 'Koffi', 'Akossiwa', 'akossiwa.koffi@bypedu.tg', '90 12 34 56', id, 'ACTIF'
                FROM matieres WHERE nom = 'Arts'
                AND NOT EXISTS (SELECT 1 FROM enseignants WHERE matiere_id = matieres.id)
                """);
            statement.executeUpdate("""
                INSERT INTO enseignants (nom, prenom, email, telephone, matiere_id, statut)
                SELECT 'Mensah', 'Kodjo', 'kodjo.mensah@bypedu.tg', '91 23 45 67', id, 'ACTIF'
                FROM matieres WHERE nom = 'Chimie Organique'
                AND NOT EXISTS (SELECT 1 FROM enseignants WHERE matiere_id = matieres.id)
                """);

            if (hasColumn(connection, "cours", "classe_id")) {
                statement.execute("PRAGMA foreign_keys = OFF");
                connection.setAutoCommit(false);
                statement.executeUpdate("""
                    INSERT OR IGNORE INTO classe_matieres (classe_id, matiere_id, enseignant_id)
                    SELECT classe_id, matiere_id, enseignant_id FROM cours WHERE classe_id IS NOT NULL
                    """);
                statement.executeUpdate("ALTER TABLE emploi_du_temps RENAME TO emploi_du_temps_legacy");
                statement.executeUpdate("ALTER TABLE cours RENAME TO cours_legacy");
                statement.executeUpdate("""
                    CREATE TABLE cours (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom TEXT NOT NULL,
                        matiere_id INTEGER NOT NULL REFERENCES matieres(id),
                        coefficient INTEGER NOT NULL DEFAULT 1,
                        volume_horaire INTEGER NOT NULL DEFAULT 1
                    )
                    """);
                statement.executeUpdate("""
                    INSERT INTO cours (id, nom, matiere_id, coefficient, volume_horaire)
                    SELECT id, nom, matiere_id, coefficient, volume_horaire FROM cours_legacy
                    """);
                statement.executeUpdate("""
                    CREATE TABLE emploi_du_temps (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        classe_id INTEGER NOT NULL REFERENCES classes(id),
                        cours_id INTEGER NOT NULL REFERENCES cours(id),
                        jour TEXT NOT NULL CHECK(jour IN ('Lundi','Mardi','Mercredi','Jeudi','Vendredi')),
                        heure_debut TEXT NOT NULL,
                        heure_fin TEXT NOT NULL,
                        salle TEXT NOT NULL
                    )
                    """);
                statement.executeUpdate("""
                    INSERT INTO emploi_du_temps (id, classe_id, cours_id, jour, heure_debut, heure_fin, salle)
                    SELECT id, classe_id, cours_id, jour, heure_debut, heure_fin, salle
                    FROM emploi_du_temps_legacy
                    """);
                statement.executeUpdate("DROP TABLE emploi_du_temps_legacy");
                statement.executeUpdate("DROP TABLE cours_legacy");
                connection.commit();
                connection.setAutoCommit(true);
                statement.execute("PRAGMA foreign_keys = ON");
            }

            initialiserDonneesDemo(connection);

            if (hasColumn(connection, "notes", "tendance")) {
                statement.execute("PRAGMA foreign_keys = OFF");
                statement.executeUpdate("ALTER TABLE notes RENAME TO notes_legacy");
                statement.executeUpdate("""
                    CREATE TABLE notes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        etudiant_id INTEGER NOT NULL REFERENCES etudiants(id) ON DELETE CASCADE,
                        matiere_id INTEGER NOT NULL REFERENCES matieres(id),
                        note REAL NOT NULL CHECK(note >= 0 AND note <= 20),
                        trimestre TEXT NOT NULL CHECK(trimestre IN ('T1','T2','T3'))
                    )
                    """);
                statement.executeUpdate("""
                    INSERT INTO notes (id, etudiant_id, matiere_id, note, trimestre)
                    SELECT id, etudiant_id, matiere_id, note, trimestre FROM notes_legacy
                    """);
                statement.executeUpdate("DROP TABLE notes_legacy");
                statement.execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            } catch (SQLException ignored) {
            }
            throw ex;
        }
    }

    private static boolean hasColumn(Connection connection, String table, String column) throws SQLException {
        try (var statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (result.next()) {
                if (column.equalsIgnoreCase(result.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void initialiserDonneesDemo(Connection connection) throws SQLException {
        boolean autoCommitInitial = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            String[] prenoms = {
                "Kossi", "Ama", "Komla", "Akossiwa", "Yao", "Mawuli", "Eyram", "Sena", "Kodjo", "Abla",
                "Folly", "Merveille", "Dodzi", "Esso", "Afi", "Kévin", "Mariam", "Prince", "Amah", "Joël"
            };
            String[] noms = {
                "Mensah", "Kouassi", "Lawson", "Adomah", "Amouzou", "Dossou", "Tété", "Koffi", "Ayélé", "Agbodjan"
            };

            try (var classes = connection.prepareStatement("SELECT id, nom FROM classes ORDER BY id");
                 var elevesClasse = connection.prepareStatement("SELECT COUNT(*) FROM etudiants WHERE classe_id = ?");
                 var ajouterEleve = connection.prepareStatement(
                     "INSERT INTO etudiants (nom, prenom, telephone, date_naissance, classe_id, statut, date_inscription) "
                     + "VALUES (?, ?, ?, ?, ?, 'ACTIF', date('now'))");
                 var resultatsClasses = classes.executeQuery()) {
                int indexEleve = 0;
                while (resultatsClasses.next()) {
                    int classeId = resultatsClasses.getInt("id");
                    elevesClasse.setInt(1, classeId);
                    int effectif;
                    try (var resultatEffectif = elevesClasse.executeQuery()) {
                        resultatEffectif.next();
                        effectif = resultatEffectif.getInt(1);
                    }
                    while (effectif < 10) {
                        String prenom = prenoms[indexEleve % prenoms.length];
                        String nom = noms[(indexEleve / prenoms.length) % noms.length];
                        ajouterEleve.setString(1, nom);
                        ajouterEleve.setString(2, prenom + " " + (indexEleve + 1));
                        ajouterEleve.setString(3, "90 00 " + String.format("%02d %02d", (indexEleve % 90) + 10, (indexEleve % 90) + 10));
                        ajouterEleve.setString(4, (2008 + (indexEleve % 5)) + "-" + String.format("%02d", (indexEleve % 12) + 1)
                            + "-" + String.format("%02d", (indexEleve % 20) + 1));
                        ajouterEleve.setInt(5, classeId);
                        ajouterEleve.executeUpdate();
                        effectif++;
                        indexEleve++;
                    }
                }
            }

            String[] enseignantsNoms = {"Mensah", "Koffi", "Lawson", "Amouzou", "Dossou", "Adomah", "Agbétiko", "Tété", "Ayélé", "Kodjo"};
            String[] enseignantsPrenoms = {"Kokou", "Ama", "Komlan", "Akossiwa", "Yao", "Mawuli", "Sena", "Eyram", "Kossi", "Abla"};
            try (var matieres = connection.prepareStatement("SELECT id, nom FROM matieres ORDER BY id");
                 var resultatsMatieres = matieres.executeQuery()) {
                int indexMatiere = 0;
                while (resultatsMatieres.next()) {
                    int matiereId = resultatsMatieres.getInt("id");
                    try (var enseignant = connection.prepareStatement(
                            "INSERT INTO enseignants (nom, prenom, email, telephone, matiere_id, statut) "
                            + "SELECT ?, ?, ?, ?, ?, 'ACTIF' WHERE NOT EXISTS "
                            + "(SELECT 1 FROM enseignants WHERE matiere_id = ?)")) {
                        enseignant.setString(1, enseignantsNoms[indexMatiere % enseignantsNoms.length]);
                        enseignant.setString(2, enseignantsPrenoms[indexMatiere % enseignantsPrenoms.length]);
                        enseignant.setString(3, "prof." + (indexMatiere + 1) + "@bypedu.tg");
                        enseignant.setString(4, "90 11 " + String.format("%02d %02d", indexMatiere + 10, indexMatiere + 20));
                        enseignant.setInt(5, matiereId);
                        enseignant.setInt(6, matiereId);
                        enseignant.executeUpdate();
                    }
                    indexMatiere++;
                }
            }

            try (var affectations = connection.prepareStatement(
                    "INSERT OR IGNORE INTO classe_matieres (classe_id, matiere_id, enseignant_id) "
                    + "SELECT ?, ?, id FROM enseignants WHERE matiere_id = ? ORDER BY id LIMIT 1");
                 var classes = connection.prepareStatement("SELECT id FROM classes");
                 var matieres = connection.prepareStatement("SELECT id FROM matieres");
                 var resultatsClasses = classes.executeQuery();
                 var resultatsMatieres = matieres.executeQuery()) {
                List<Integer> classesIds = new ArrayList<>();
                List<Integer> matieresIds = new ArrayList<>();
                while (resultatsClasses.next()) classesIds.add(resultatsClasses.getInt(1));
                while (resultatsMatieres.next()) matieresIds.add(resultatsMatieres.getInt(1));
                for (int classeId : classesIds) {
                    for (int matiereId : matieresIds) {
                        affectations.setInt(1, classeId);
                        affectations.setInt(2, matiereId);
                        affectations.setInt(3, matiereId);
                        affectations.executeUpdate();
                    }
                }
            }

            try (var eleves = connection.prepareStatement("SELECT id FROM etudiants");
                 var matieres = connection.prepareStatement("SELECT id FROM matieres");
                 var notes = connection.prepareStatement(
                     "INSERT INTO notes (etudiant_id, matiere_id, note, trimestre) "
                     + "SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM notes WHERE etudiant_id = ? AND matiere_id = ? AND trimestre = ?)");
                 var resultatsEleves = eleves.executeQuery();
                 var resultatsMatieres = matieres.executeQuery()) {
                List<Integer> elevesIds = new ArrayList<>();
                List<Integer> matieresIds = new ArrayList<>();
                while (resultatsEleves.next()) elevesIds.add(resultatsEleves.getInt(1));
                while (resultatsMatieres.next()) matieresIds.add(resultatsMatieres.getInt(1));
                for (int eleveId : elevesIds) {
                    for (int matiereId : matieresIds) {
                        for (int trimestre = 1; trimestre <= 3; trimestre++) {
                            double valeur = 8.0 + ((eleveId * 3 + matiereId * 2 + trimestre) % 12) * 0.5;
                            String code = "T" + trimestre;
                            notes.setInt(1, eleveId);
                            notes.setInt(2, matiereId);
                            notes.setDouble(3, valeur);
                            notes.setString(4, code);
                            notes.setInt(5, eleveId);
                            notes.setInt(6, matiereId);
                            notes.setString(7, code);
                            notes.executeUpdate();
                        }
                    }
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(autoCommitInitial);
        }
    }
}