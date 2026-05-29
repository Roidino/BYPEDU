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
    private static final String URL = "jdbc:sqlite:src/main/resources/tg/univlome/epl/bypedu/database.db";
    private static Connection connection;
            
    private DatabaseConnection() {}
    
    public static Connection getDatabase(){
        if (connection == null) {
            try {
                connection =  DriverManager.getConnection(URL);
            } catch (SQLException ex) {
                System.getLogger(DatabaseConnection.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
        return connection;
    }
}
