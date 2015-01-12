/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.stud.twitter.Tweets2SQL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.BiConsumer;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Johannes Gontrum <gontrum@uni-potsdam.de>
 */
public class JSONReader {

    public static void stream(String filename, BiConsumer<Status, String> fn) throws FileNotFoundException, IOException, TwitterException {
        FileInputStream instream = new FileInputStream(new File(filename));
        BufferedReader br = new BufferedReader(new InputStreamReader(instream));
        
        // Store the configuration
        ConfigurationBuilder config = new ConfigurationBuilder();
        config.setDebugEnabled(true);
        config.setJSONStoreEnabled(true);
        
        TwitterFactory tf = new TwitterFactory(config.build());
        
        String line;
        while ((line = br.readLine()) != null) {
            try {
                if (line.length() > 1) { // Ignore empty lines
                    fn.accept(TwitterObjectFactory.createStatus(line), line);
                }
            } catch (Exception e) {
                System.err.println("Error reading Tweet from file: " + e.getMessage());
            }

        }
    }
    
}
