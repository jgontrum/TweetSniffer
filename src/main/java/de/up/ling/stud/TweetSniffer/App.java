package de.up.ling.stud.TweetSniffer;

import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws InterruptedException, FileNotFoundException, IOException
    {
        // Load API keys from property-file
        Properties prop = new Properties();
        InputStream input = new FileInputStream("config.properties");
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
        
        // Build a list of stopwords or user ids to track.
        // Just leave the list empty if you do not want specific terms / users.
        List<Long> users = Lists.newArrayList(1234L, 566788L);
        List<String> stopwords = Lists.newArrayList("twitter", "api");
        
        // Initialize the streamer
        TweetStreamer streamer = new TweetStreamer(apiKeys, stopwords, users);
        
        streamer.startStreaming(status -> {
            System.err.println(status.getText());
        });

    }
}
