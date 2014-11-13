/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.stud.TweetSniffer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Johannes Gontrum <gontrum@uni-potsdam.de>
 */
public class MySQLAccess {
    private Connection connect;
    private Statement statement;
    private ResultSet returnSet;
    private boolean running;
    
    public MySQLAccess(String[] settings) {
        String host = settings[0];
        String user = settings[1];
        String password = settings[2];
                
        try {
            connect = DriverManager.getConnection(host, user, password);
            running = true;
        } catch (SQLException e) {
            System.err.println("mySQL: Could not connect: " + e.toString());
            running = false;
        }
        
        try {
            statement = connect.createStatement();
            
            returnSet = statement.executeQuery("SELECT VERSION()");

            if (returnSet.next()) {
                System.out.println(returnSet.getString(1));
            }
        } catch (Exception e) {
        }
    }
    
    
    
}
