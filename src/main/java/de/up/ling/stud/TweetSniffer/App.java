package de.up.ling.stud.TweetSniffer;

import com.beust.jcommander.JCommander;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class App 
{
    public static void main( String[] args ) throws InterruptedException, FileNotFoundException, IOException {
        boolean dbTest = false;
        // Parse CLI arguments
        CommandLineArguments arguments = new CommandLineArguments();
        JCommander cliParser = new JCommander(arguments, args);
        cliParser.setProgramName("Tweet2SQL");

        // Print help
        if (arguments.help) {
            cliParser.usage();
            System.exit(0);
        }
        
        // Change value only of not set true in file
        if (!dbTest) {
            dbTest = arguments.testDB;
        }
        
        // Load the four API keys from a file
        String[] apiKeys = loadAPISettings(arguments.configFile);
        String[] sqlSettings = loadSQLSettings(arguments.configFile);
        
        double[][] coordinates = loadFilterCoordinates(arguments.filterFile);
        String[] stopWords = loadFilterTerms(arguments.filterFile);
        long[] users = loadFilterUsers(arguments.filterFile);
        
        // Setup the DB structure and connect to the MySQL server
        MySQLAccessor database = new MySQLAccessor(sqlSettings);
        
        // Leave the program when a connection to the database is established
        if (dbTest) {
            System.exit(0);
        }
        
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
        
        if (allCoordinates[0].length() > 0) {
        
        double[][] ret = new double[allCoordinates.length * 2][2];
        
        for (int i = 0; i < allCoordinates.length; i++) {
            String[] coordinateValues = allCoordinates[i].split(",");
            double longSW = Double.parseDouble(coordinateValues[0]);
            double latSW = Double.parseDouble(coordinateValues[1]);
            double longNE = Double.parseDouble(coordinateValues[2]);
            double latNE = Double.parseDouble(coordinateValues[3]);
            ret[i * 2][0] = longSW;
            ret[i * 2][1] = latSW;
            ret[i * 2 + 1][0] = longNE;
            ret[i * 2 + 1][1] = latNE;
        }
                
        return ret;
        
        } else {
            return new double[0][0];
        }
    }
    
    /**
     * The filed 'users' in the configuration file describes user ids to follow.
     * They must be separated by a semicolon.
     * @param configFile
     * @return
     * @throws IOException 
     */
    private static long[] loadFilterUsers(String configFile) throws IOException {
        // Load settings from property-file
        Properties prop = new Properties();
        InputStream input = new FileInputStream(configFile);
        prop.load(input);
        
        String[] allUsers = prop.getProperty("users", "").split(";");
        
        if (allUsers[0].length() > 0) {
            long[] ret = new long[allUsers.length];

            for (int i = 0; i < allUsers.length; i++) {
                ret[i] = Long.parseLong(allUsers[i]);
            }
            return ret;
        } else {
            return new long[0];
        }

    }
    
    /**
     * The filed 'terms' in the configuration file describes stopwords to track.
     * They must be separated by a semicolon.
     * @param configFile
     * @return
     * @throws IOException 
     */
    private static String[] loadFilterTerms(String configFile) throws IOException {
        // Load settings from property-file
        Properties prop = new Properties();
        InputStream input = new FileInputStream(configFile);
        prop.load(input);
        
        String[] terms = prop.getProperty("terms", "").split(";");
        if (terms[0].length() > 0) {
            return terms;
        } else {
            return new String[0];
        }
    }
}
