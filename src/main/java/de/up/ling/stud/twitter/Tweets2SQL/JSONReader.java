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
import java.util.function.Consumer;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

/**
 *
 * @author Johannes Gontrum <gontrum@uni-potsdam.de>
 */
public class JSONReader {

    public static void stream(String filename, Consumer<Status> fn) throws FileNotFoundException, IOException, TwitterException {
        FileInputStream instream = new FileInputStream(new File(filename));
        BufferedReader br = new BufferedReader(new InputStreamReader(instream));
        
        String line;
        while ((line = br.readLine()) != null) {
            fn.accept(TwitterObjectFactory.createStatus(line));
        }
    }
    
}
