/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.stud.twitter.Tweets2SQL;

import java.util.HashMap;
import java.util.Map;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

/**
 *
 * @author Johannes Gontrum <gontrum@uni-potsdam.de>
 */
public class MySQLAccessor {
    private final Map<String,String> tableLayout;
    private final MySQLDatabase database;
    private String currentTable;
    
    public MySQLAccessor(String[] sqlSettings) {
        // Create the used layout of the table in the database
        tableLayout = new HashMap<>();
        tableLayout.put("Text", "TEXT");
        tableLayout.put("UserID", "BIGINT");
        tableLayout.put("TweetID", "BIGINT");
        tableLayout.put("CreatedAt", "BIGINT");
        tableLayout.put("JSON", "TEXT");
        
        // Connect to DB server
        database = new MySQLDatabase(sqlSettings, tableLayout);
        
        // Make sure the current db exists
        currentTable = sqlSettings[3];
        if (!database.doesTableExist(currentTable)) {
            database.createTable(currentTable);
        }
    }
    
    
    public void queryTweet(Status tweet, String json) {
        Map<String, Object> insert = new HashMap<>();
        
        tableLayout.keySet().forEach(column -> {
            Object insertValue = null;
            switch (column) {
                case "Text":
                    insertValue = tweet.getText();
                    break;
                case "UserID":
                    insertValue = tweet.getUser().getId();
                    break;
                case "TweetID":
                    insertValue = tweet.getId();
                    break;
                case "CreatedAt":
                    insertValue = tweet.getCreatedAt().getTime() / 1000L; // UNIX timestamp
                    break;
                case "JSON":
                    insertValue = json;
                    break;
            }
            insert.put(column, insertValue);
        });
        
        // insert it into the database.
        saveInsert(currentTable, insert);
    }
    
    /**
     * Checks if the table exists before inserting data.
     * Creates a new table if the test fails.
     * @param table
     * @param values 
     */
    private void saveInsert(String table, Map<String, Object> values) {
//        if (!database.doesTableExist(currentTable)) {
//            database.createTable(currentTable);
//        }
        database.insert(table, values);
    }
}
