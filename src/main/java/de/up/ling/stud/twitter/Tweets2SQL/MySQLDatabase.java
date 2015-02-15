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

    private final int maxInsertsInBatch;
    private final int maxInsertsToCommit;
    private int currentInserts;
    
    private List<Map<String, Object>> insertCache;
    
    private final Map<String, String> tableLayout;
    private final List<String> columns;
    
    private final String tableName;
    
    private boolean running;
    
    public MySQLDatabase(String[] settings, Map<String, String> tableLayout) {
        String host = settings[0];
        String user = settings[1];
        String password = settings[2];
        
        tableName = settings[3];
        
        maxInsertsInBatch = 300;
        maxInsertsToCommit = 5000;
        
        currentInserts = 0;
        preparedStatement = null;
        
        insertCache = new ArrayList<>();
        
        this.tableLayout = tableLayout;
        
        // Columns is used to have an unmutable order
        columns = new ArrayList<>(tableLayout.keySet());
        
        // Connect to database
        try {
            connect(settings);
            connect.setAutoCommit(false);  
            statement = connect.createStatement();
            running = true;
        } catch (SQLException e) {
            System.err.println("mySQL: Could not connect: " + e.toString());
            running = false;
        }
    }
    
    private void connect(String[] settings) throws SQLException {
        String host = settings[0];
        String user = settings[1];
        String password = settings[2];
        
        connect = DriverManager.getConnection(host, user, password);
    }
    
    private void prepareInsertStatement(int inserts) throws SQLException  {
        if (running) {
            if (preparedStatement == null) {
                // Create the prepared statement
                StringBuilder queryBuilder = new StringBuilder("INSERT INTO ");
                queryBuilder.append(tableName)
                        .append(" (");

                columns.forEach(column -> queryBuilder.append(column).append(","));

                // remove last ',' and replace it by a ')'
                queryBuilder.replace(queryBuilder.length() - 1, queryBuilder.length(), ")");

                queryBuilder.append(" VALUES ");
                
                // Add '(?,?,...,?)' for all comming inserts
                for (int i = 0; i < inserts; ++i) {
                    queryBuilder.append("(");
                    columns.forEach(column -> queryBuilder.append("?,"));
                    // remove last ',' and replace it by a ')'
                    queryBuilder.replace(queryBuilder.length() - 1, queryBuilder.length(), "),");
                }
                
                // remove last ',' and replace it by a ';'
                queryBuilder.replace(queryBuilder.length() - 1, queryBuilder.length(), ";");
                preparedStatement =  connect.prepareStatement(queryBuilder.toString());
            }
        } else {
            preparedStatement = null;
        }
    }
    
    private void storeInsertInCache(Map<String, Object> values) { 
        assert insertCache.size() < maxInsertsInBatch;
        insertCache.add(values);
    }
    
    private int getNumberOfItemsInCache() {
        return insertCache.size();
    }
    
    private Map<String, Object> getItemFromCache(int position) {
        if (position < getNumberOfItemsInCache()) {
            return insertCache.get(position);
        } else return null;
    }
    
    private void resetCache() {
        insertCache.clear();
    }
    
    private void writeCacheToStatement() throws SQLException {
        int numberOfColumns = columns.size();
        
        // iterate over all items in the cache
        for (int c = 0; c < getNumberOfItemsInCache(); ++c) {
            Map<String, Object> values = getItemFromCache(c);
            
            // Replace the questionmarks by their actual values!
            // THIS HAS TO BE CHANGED WHENEVER TABLELAYOUT IS CHANGED!
            for (int i = c*numberOfColumns+1; i <= (c+1)*numberOfColumns; i++) {
                String column = columns.get(i - c*numberOfColumns - 1);
                Object currentValue = values.getOrDefault(column, null);
                String columnType = tableLayout.get(column);
                // Integer
                if (columnType.startsWith("INT")) {
                    if (currentValue != null) {
                        preparedStatement.setInt(i, (Integer) currentValue);
                    } else {
                        preparedStatement.setNull(i, java.sql.Types.INTEGER);
                    }
                }
                // Long
                if (columnType.startsWith("BIGINT")) {
                    if (currentValue != null) {
                        preparedStatement.setLong(i, (long) currentValue);
                    } else {
                        preparedStatement.setNull(i, java.sql.Types.BIGINT);
                    }
                }
                // Text
                if (columnType.startsWith("TEXT")) {
                    if (currentValue != null) {
                        preparedStatement.setString(i, (String) currentValue);
                    } else {
                        preparedStatement.setString(i, "");
                    }
                }
                // Char
                if (columnType.startsWith("CHAR")) {
                    if (currentValue != null) {
                        preparedStatement.setString(i, (String) currentValue);
                    } else {
                        preparedStatement.setNull(i, java.sql.Types.CHAR);
                    }
                }
                // Double
                if (columnType.startsWith("DOUBLE")) {
                    if (currentValue != null) {
                        preparedStatement.setDouble(i, (double) currentValue);
                    } else {
                        preparedStatement.setNull(i, java.sql.Types.DOUBLE);
                    }
                }
                // Float
                if (columnType.startsWith("FLOAT")) {
                    if (currentValue != null) {
                        preparedStatement.setFloat(i, (float) currentValue);
                    } else {
                        preparedStatement.setNull(i, java.sql.Types.FLOAT);
                    }
                }
                // Tinyint
                if (columnType.startsWith("TINYINT")) {
                    if (currentValue != null) {
                        preparedStatement.setByte(i, (byte) currentValue);
                    } else {
                        preparedStatement.setNull(i, java.sql.Types.TINYINT);
                    }
                }
                // ... Add more if needed!
            }
            
        }
    }
    
    private void sendQuery() throws SQLException {
        prepareInsertStatement(getNumberOfItemsInCache());
        assert preparedStatement != null;

        // Fill the prepared statement with values.
        writeCacheToStatement();
        resetCache();

        // Now make the query!
        System.err.println("Making a query...");
        preparedStatement.executeUpdate();
        
        if (currentInserts >= maxInsertsToCommit) {
            System.err.println("Commiting a query...");
            connect.commit();
            currentInserts = 0;
        }
    }
    
    public void insert (Map<String, Object> values) {
        if (running) {
            if (values.keySet().size() == tableLayout.keySet().size()) {
                try {
                    storeInsertInCache(values);
                    ++currentInserts;
                    // Check if it is time to store our tweets in the db
                    if (getNumberOfItemsInCache() == maxInsertsInBatch) {
                        sendQuery();
                    }
                    
                } catch (SQLException ex) {
                    Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            } else {
                System.err.println("MySQLAccess: Failed to insert values: Number of the given values does not match the table layout!");
            }
        }
    }
    
    public void close() {
        try {
            sendQuery();
            connect.commit();
            connect.close();
        } 
        catch (SQLException e) {
            System.err.println("Error while closing connection: " + e.getMessage());
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
