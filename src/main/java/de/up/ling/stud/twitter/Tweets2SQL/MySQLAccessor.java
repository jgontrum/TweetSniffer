/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.stud.twitter.Tweets2SQL;

import java.util.HashMap;
import java.util.Map;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

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
        
        tableLayout.put("id", "BIGINT");
        tableLayout.put("user_id", "BIGINT");
        tableLayout.put("friends_count", "INT");
        tableLayout.put("followers_count", "INT");
        tableLayout.put("in_reply_to_status_id", "BIGINT");
        tableLayout.put("in_reply_to_user_id", "BIGINT");
        tableLayout.put("user_mentions_count", "INT");
        tableLayout.put("user_mentions_list", "TEXT");
        tableLayout.put("created_at", "BIGINT");
        tableLayout.put("source", "TEXT");
        tableLayout.put("text", "VARCHAR(200)");
        tableLayout.put("direct_replies_count", "INT");
        tableLayout.put("direct_replies_list", "TEXT");
        tableLayout.put("indirect_replies_count", "INT");
        tableLayout.put("indirect_replies_list", "TEXT");
        tableLayout.put("is_base_tweet", "TINYINT");
        tableLayout.put("is_question", "TINYINT");
        tableLayout.put("question_mark_counter", "INT");
        tableLayout.put("is_wh_question", "TINYINT");

        // Save JSON?
        if (sqlSettings[6].startsWith("True")) {
            tableLayout.put("JSON", "TEXT");
        }
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
                case "text":
                    insertValue = tweet.getText();
                    break;
                case "user_id":
                    insertValue = tweet.getUser().getId();
                    break;
                case "id":
                    insertValue = tweet.getId();
                    break;
                case "created_at":
                    insertValue = tweet.getCreatedAt().getTime() / 1000L; // UNIX timestamp
                    break;
                case "in_reply_to_status_id":
                    insertValue = tweet.getInReplyToStatusId();
                    break;
                case "in_reply_to_user_id":
                    insertValue = tweet.getInReplyToUserId();
                    break;
                case "source":
                    insertValue = tweet.getSource();
                    break;
                case "followers_count":
                    insertValue = tweet.getUser().getFollowersCount();
                    break;
                case "friends_count":
                    insertValue = tweet.getUser().getFriendsCount();
                    break;
                case "user_mentions_count":
                    insertValue = tweet.getUserMentionEntities().length;
                    break;
                case "user_mentions_list":
                    // = "12345","1234555","..."
                    StringBuilder sb = new StringBuilder();
                    for (UserMentionEntity mention : tweet.getUserMentionEntities()) {
                        sb.append("\"");
                        sb.append(mention.getId());
                        sb.append("\",");
                    }
                    if (sb.length() > 0) {
                        sb.replace(sb.length() - 1, sb.length(), "");
                    }
                    insertValue = sb.toString();
                    break;
                    
                case "JSON":
                    insertValue = json; // json
                    break;
                case "Longitude":
                    insertValue = tweet.getGeoLocation() == null ? -1 : tweet.getGeoLocation().getLongitude();
                    break;
                case "Latitude":
                    insertValue = tweet.getGeoLocation() == null ? -1 : tweet.getGeoLocation().getLatitude();
                    break;
            }
            insert.put(column, insertValue);
        });
        
        // insert it into the database.
        saveInsert(insert);
    }
    
    /**
     * Checks if the table exists before inserting data.
     * Creates a new table if the test fails.
     * @param table
     * @param values 
     */
    private void saveInsert(Map<String, Object> values) {
//        if (!database.doesTableExist(currentTable)) {
//            database.createTable(currentTable);
//        }
        database.insert(values);
    }
    
    public void closeDB() {
        database.close();
    }
}
