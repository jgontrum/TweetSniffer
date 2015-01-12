/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.stud.twitter.Tweets2SQL;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Johannes Gontrum <gontrum@uni-potsdam.de>
 */
public class MySQLDatabase {
    private Connection connect;
    private Statement statement;
    private ResultSet returnSet;
    
    private PreparedStatement preparedStatement;
    private int insertsInBatch;
    private final int maxInsertInBatch;
    
    
    private final Map<String, String> tableLayout;
    private final List<String> columns;
    
    private boolean running;
    
    public MySQLDatabase(String[] settings, Map<String, String> tableLayout) {
        String host = settings[0];
        String user = settings[1];
        String password = settings[2];
        
        maxInsertInBatch = 100;
        preparedStatement = null;
        
        this.tableLayout = tableLayout;
        
        // Columns is used to have an unmutable order
        columns = new ArrayList<>(tableLayout.keySet());
        
        // Connect to database
        try {
            connect = DriverManager.getConnection(host, user, password);
            statement = connect.createStatement();
            running = true;
        } catch (SQLException e) {
            System.err.println("mySQL: Could not connect: " + e.toString());
            running = false;
        }
    }
    
    private void prepareInsertStatement(String tableName) throws SQLException {
        if (running) {
            if (preparedStatement == null) {
                // Create the prepared statement
                StringBuilder queryBuilder = new StringBuilder("INSERT INTO ");
                queryBuilder.append(tableName)
                        .append(" (");

                columns.forEach(column -> queryBuilder.append(column + ","));

                // remove last ',' and replace it by a ')'
                queryBuilder.replace(queryBuilder.length() - 1, queryBuilder.length(), ")");

                queryBuilder.append(" VALUES (");
                columns.forEach(column -> queryBuilder.append("?,"));

                // remove last ',' and replace it by a ')'
                queryBuilder.replace(queryBuilder.length() - 1, queryBuilder.length(), ")");
                
                insertsInBatch = 0;
                preparedStatement =  connect.prepareStatement(queryBuilder.toString());
            }
        } else {
            preparedStatement = null;
        }
    }
    
    public void insert(String tableName, Map<String, Object> values) {
        if (running) {
            if (values.keySet().size() == tableLayout.keySet().size()) {
                try {
                    prepareInsertStatement(tableName);
                    // Replace the questionmarks by their actual values!
                    // THIS HAS TO BE CHANGED WHENEVER TABLELAYOUT IS CHANGED!
                    for (int i = 1; i <= columns.size(); i++) {
                        String column = columns.get(i-1);
                        Object currentValue = values.getOrDefault(column, null);
                        if (currentValue != null) {
                            String columnType = tableLayout.get(column);
                            // Integer
                            if (columnType.startsWith("INT")) {
                                preparedStatement.setInt(i, (Integer) currentValue);
                            }
                            // Long
                            if (columnType.startsWith("BIGINT")) {
                                preparedStatement.setLong(i, (Long) currentValue);
                            }
                            // Text
                            if (columnType.startsWith("TEXT")) {
                                preparedStatement.setString(i, (String) currentValue);
                            }
                            // ... Add more if needed!
                        } else {
                            System.err.println("Column " + column + " not given in values.");
                        }
                    }
                    insertsInBatch += 1;
                    preparedStatement.addBatch();
                    
                    if (insertsInBatch >= maxInsertInBatch) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                        insertsInBatch = 0;
                    }
                    
                } catch (SQLException ex) {
                    Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            } else {
                System.err.println("MySQLAccess: Failed to insert values: Number of the given values does not match the table layout!");
            }
        }
    }
    
    /**
     * Returns true, if a table with the given name exists.
     * @param tableName
     * @return 
     */
    public boolean doesTableExist(String tableName) {
        return getAllTables().contains(tableName);
    }
    
    public void createTable(String tableName) {
        if (running) {
            // Build a SQL query to create the structure defined in tableLayout
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder
                    .append("CREATE TABLE ")
                    .append(tableName)
                    .append(" (\n");
            
            columns.forEach(column -> queryBuilder
                    .append(column)
                    .append(" ")
                    .append(tableLayout.get(column))
                    .append(",\n"));
            
            // Replace linebreak and comma by ending characters
            queryBuilder.replace(
                    queryBuilder.length() - 2, 
                    queryBuilder.length(), 
                    "\n) ");
            
            
            // Add encoding settings
            queryBuilder.append("DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE = utf8mb4_unicode_ci;");
            
            try {
                int result = statement.executeUpdate(queryBuilder.toString());
            } catch (SQLException ex) {
                Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    /**
     * Deletes the Table with the given name (if it exists).
     * @param tableName 
     */
    public void deleteTable(String tableName) {
        if (running && getAllTables().contains(tableName)) {
            try {
                int result = statement.executeUpdate("DROP TABLE " + tableName);
            } catch (SQLException ex) {
                Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Returns a collection of the names of all tables within this DB.
     * @return 
     */
    private Collection<String> getAllTables() {
        Collection<String> ret = new ArrayList<>();
        
        if (running) {
            try {
                DatabaseMetaData meta = connect.getMetaData();
                
                ResultSet rs = meta.getTables(null, null, "%", null);
                
                while (rs.next()) {
                    ret.add(rs.getString(3)); // The name of the DB is at position 3
                }
            } catch (SQLException ex) {
                Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return ret;
    }
    
    /**
     * Deletes the table with the given name and creates it again.
     * Note that all structure that differs from 'tableLayout' is lost.
     * @param tableName 
     */
    public void resetTable(String tableName) {
        deleteTable(tableName);
        createTable(tableName);
    }
    
    
}
