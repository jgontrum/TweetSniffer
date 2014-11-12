/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.stud.TweetSniffer;

import java.util.List;
import java.util.function.Consumer;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Johannes Gontrum <gontrum@uni-potsdam.de>
 */
public class TweetStreamer {
    private final List<String> stopwordList;
    private final List<Long> userList;
    private final ConfigurationBuilder config;

    public TweetStreamer(String[] apiKeys, List<String> stopwords, List<Long> users ) throws InterruptedException {
        // Save the arguments
        String consumerKey = apiKeys[0];
        String consumerSecret = apiKeys[1];
        String token = apiKeys[2];
        String secret = apiKeys[3];
        
        stopwordList = stopwords;
        userList = users;
        
        // Store the configuration
        config = new ConfigurationBuilder();
        config.setDebugEnabled(true);
        config.setOAuthConsumerKey(consumerKey);
        config.setOAuthConsumerSecret(consumerSecret);
        config.setOAuthAccessToken(token);
        config.setOAuthAccessTokenSecret(secret);
    }   
    
    public void startStreaming(Consumer<Status> fn) {
        // Create a status listener. When a new tweet arives, the function object will accept it.
        StatusListener listener = new TwitterStatusListener(fn);

        // Create the stream-object
        TwitterStream stream = new TwitterStreamFactory(config.build()).getInstance();

        stream.addListener(listener);
        
        stream.sample();
    }
      
   
    
   
}
