package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.support.v7.preference.PreferenceDataStore;

public class RemoteDataStore extends PreferenceDataStore {
    CommunicationManager mCommunicationManager;

    public RemoteDataStore() {
        mCommunicationManager = CommunicationManager.getInstance();
    }

    public void putString(String key, String value) {
        //mCommunicationManager.updateConfiguration(key, value);
    }

    public String getString(String key, String defValue) {
        // Read the value from somewhere and return ...
        return "";
    }

    public void putInt(String key, int value) {
        //mCommunicationManager.updateConfiguration(key, Integer.toString(value));
    }

    public int getInt(String key, int defValue) {
        // Read the value from somewhere and return ...
        return 0;
    }
}
