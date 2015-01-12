/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.stud.twitter.Tweets2SQL;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterObjectFactory;

/**
 *
 * @author Johannes Gontrum <gontrum@uni-potsdam.de>
 */
public class TwitterStatusListener implements StatusListener{
    private BiConsumer<Status, String> fn;
    
    public TwitterStatusListener(BiConsumer<Status, String> fn) {
        this.fn = fn;
    }
    
    @Override
    public void onStatus(Status status) {
        fn.accept(status, TwitterObjectFactory.getRawJSON(status));
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice sdn) {}

    @Override
    public void onTrackLimitationNotice(int i) {}

    @Override
    public void onScrubGeo(long l, long l1) {}

    @Override
    public void onStallWarning(StallWarning sw) {}

    @Override
    public void onException(Exception excptn) {
        System.err.println("Exception in TweetStatusListener: " + excptn);
        excptn.printStackTrace();
    }
    
}
