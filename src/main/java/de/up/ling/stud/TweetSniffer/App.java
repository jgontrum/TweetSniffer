package de.up.ling.stud.TweetSniffer;

import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.jcp.xml.dsig.internal.dom.DOMXMLSignature;

public class App 
{
    public static void main( String[] args ) throws InterruptedException, FileNotFoundException, IOException
    {
        String configFile = "config.properties";
        String filterConfigFile = "twitter.properties";
        // Load the four API keys from a file
        String[] apiKeys = loadAPISettings(configFile);
        String[] sqlSettings = loadSQLSettings(configFile);
        
        double[][] coordinates = loadFilterCoordinates(filterConfigFile);
        String[] stopWords = loadFilterTerms(filterConfigFile);
        long[] users = loadFilterUsers(filterConfigFile);
        
        // Setup the DB structure and connect to the MySQL server
        MySQLAccessor database = new MySQLAccessor(sqlSettings);
        
        // Start streaming.
        TweetStreamer.stream(apiKeys, stopWords, users, coordinates, tweet -> {
            if (!tweet.isRetweet()) { //ignore RTs
                System.err.println("Tweet: " + tweet.getText());
                database.queryTweet(tweet);    
            }
        });
        
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

        String[] sqlSettings = new String[4];

        sqlSettings[0] = prop.getProperty("mySQLHost", "");
        sqlSettings[1] = prop.getProperty("mySQLUser", "");
        sqlSettings[2] = prop.getProperty("mySQLPassword", "");
        sqlSettings[3] = prop.getProperty("mySQLTablePrefix", "DEFAULT");

        if (sqlSettings[0].isEmpty() || sqlSettings[1].isEmpty() || sqlSettings[2].isEmpty()) {
            System.err.println("Please specify your mySQLHost, mySQLUser and mySQLPassword in a file called 'config.properties");
            System.exit(-1);
        }

        return sqlSettings;    
    }
    
    /**
     * Reads in the coordinates to track.
     * Define boxes by getting the long-lat values of the south western point first
     * and the long-lat values of the north eastern point. They must be separated by comma:
     * 'longSW,latSW,logNE,latNE'.
     * If you want to track multiple locations, separate the tuples by a semicolon.
     * @param configFile
     * @return
     * @throws IOException 
     */
    private static double[][] loadFilterCoordinates(String configFile) throws IOException {
        // Load coordinates settings from property-file
        Properties prop = new Properties();
        InputStream input = new FileInputStream(configFile);
        prop.load(input);
        
        String[] allCoordinates = prop.getProperty("coordinates", "").split(";");
        
        double[][] ret = new double[allCoordinates.length * 2][2];
        
        for (int i = 0; i < allCoordinates.length; i++) {
            String[] coordinateValues = allCoordinates[i].split(",");
            double longSW = Double.parseDouble(coordinateValues[0]);
            double latSW = Double.parseDouble(coordinateValues[1]);
            double longNE = Double.parseDouble(coordinateValues[2]);
            double latNE = Double.parseDouble(coordinateValues[3]);
            ret[i*2][0] = longSW;
            ret[i*2][1] = latSW;
            ret[i*2+1][0] = longNE;
            ret[i*2+1][1] = latNE;
        }
                
        return ret;
    }
    
    // TODO
    private static long[] loadFilterUsers(String configFile) throws IOException {
        return new long[0];
    }
    
    // TODO
    private static String[] loadFilterTerms(String configFile) throws IOException {
        return new String[0];
    }
}
