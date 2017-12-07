package ch.epfl.pdse.polypotapp;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;

public class ApplicationPolyPot extends Application {
    private CommunicationManager mCommunicationManager;

    @Override
    public void onCreate () {
        super.onCreate();

        EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
        mCommunicationManager = CommunicationManager.getDefault(this);
    }
}
