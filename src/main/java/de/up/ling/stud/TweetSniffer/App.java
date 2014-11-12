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
        // Load the four API keys from a file
        String[] apiKeys = loadAPISettings("config.properties");
        
        // Build a list of stopwords or user ids to track.
        // Just leave the list empty if you do not want specific terms / users.
        // This should be read from file as well. // TODO
        List<Long> users = Lists.newArrayList(1234L, 566788L);
        List<String> stopWords = Lists.newArrayList("twitter", "api");
        
        // Start streaming.
        TweetStreamer.stream(apiKeys, stopWords, users, tweet -> {
            System.err.println(tweet.getText());
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

        System.err.println(Arrays.toString(apiKeys));

        if (apiKeys[0].isEmpty() || apiKeys[1].isEmpty() || apiKeys[2].isEmpty() || apiKeys[3].isEmpty()) {
            System.err.println("Please specify your consumerKey, consumerSecret, token and secret in a file called 'config.properties");
            System.exit(-1);
        }
        
        return apiKeys;
    }
}
