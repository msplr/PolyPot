package ch.epfl.pdse.polypotapp;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;

public class PolyPotApplication extends Application {
    @Override
    public void onCreate () {
        super.onCreate();

        EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
    }
}
