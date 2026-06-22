
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univlome.epl.bypedu.DAOs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 *
 * @author Savastano
 */

public class DatabaseConnection {
    private static Connection instance = null;

    public static Connection getDatabase() {
        if (instance == null) {
            try {
                instance = DriverManager.getConnection("jdbc:sqlite:src/main/resources/tg/univlome/epl/bypedu/database.db");
                instance.createStatement()
                        .execute("PRAGMA foreign_keys = ON");
                // ↑ une seule connexion réutilisée partout
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return instance;
    }
}