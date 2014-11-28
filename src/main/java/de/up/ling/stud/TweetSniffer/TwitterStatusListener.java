/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.stud.TweetSniffer;

import java.util.function.Consumer;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

/**
 *
 * @author Johannes Gontrum <gontrum@uni-potsdam.de>
 */
public class TwitterStatusListener implements StatusListener{
    private Consumer<Status> fn;
    
    public TwitterStatusListener(Consumer<Status> fn) {
        this.fn = fn;
    }
    
    @Override
    public void onStatus(Status status) {
        fn.accept(status);
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
