package de.up.ling.stud.TweetSniffer;

import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class App 
{
    public static void main( String[] args ) throws InterruptedException, FileNotFoundException, IOException
    {
        String configFile = "config.properties";
        // Load the four API keys from a file
        String[] apiKeys = loadAPISettings(configFile);
        String[] sqlSettings = loadSQLSettings(configFile);
        
        // Connect do database...
        MySQLAccess database = new MySQLAccess(sqlSettings);
        
        // Build a list of stopwords or user ids to track.
        // Just leave the list empty if you do not want specific terms / users.
        // This should be read from file as well. // TODO
        List<Long> users = Lists.newArrayList();
        List<String> stopWords = Lists.newArrayList("squirrel");
        
        /*
        // Start streaming.
        TweetStreamer.stream(apiKeys, stopWords, users, tweet -> {
            System.err.println(tweet.getText());
        });
        */
    }
    
    private static String[] loadAPISettings(String file) throws IOException  {
        // Load API keys from property-file
        Properties prop = new Properties();
        InputStream input = new FileInputStream(file);
        prop.load(input);

        String[] apiKeys = new String[4];

        apiKeys[0] = prop.getProperty("consumerKey", "");
        apiKeys[1] = prop.getProperty("consumerSecret", "");
        apiKeys[2] = prop.getProperty("token", "");
        apiKeys[3] = prop.getProperty("secret", "");

        if (apiKeys[0].isEmpty() || apiKeys[1].isEmpty() || apiKeys[2].isEmpty() || apiKeys[3].isEmpty()) {
            System.err.println("Please specify your consumerKey, consumerSecret, token and secret in a file called 'config.properties");
            System.exit(-1);
        }
        
        return apiKeys;
    }

    private static String[] loadSQLSettings(String configFile) throws IOException {
        // Load SQL settings from property-file
        Properties prop = new Properties();
        InputStream input = new FileInputStream(configFile);
        prop.load(input);

        String[] sqlSettings = new String[3];

        sqlSettings[0] = prop.getProperty("mySQLHost", "");
        sqlSettings[1] = prop.getProperty("mySQLUser", "");
        sqlSettings[2] = prop.getProperty("mySQLPassword", "");

        if (sqlSettings[0].isEmpty() || sqlSettings[1].isEmpty() || sqlSettings[2].isEmpty()) {
            System.err.println("Please specify your mySQLHost, mySQLUser and mySQLPassword in a file called 'config.properties");
            System.exit(-1);
        }

        return sqlSettings;    }
}
