package ch.epfl.pdse.polypotapp;

import android.support.v7.preference.PreferenceDataStore;

public class RemoteDataStore extends PreferenceDataStore {
    public void putString(String key, String value) {
        // Write the value somewhere ...
    }

    public String getString(String key, String defValue) {
        // Read the value from somewhere and return ...
        return "";
    }

    public void putInt(String key, int value) {
        // Write the value somewhere ...
    }

    public int getInt(String key, int defValue) {
        // Read the value from somewhere and return ...
        return 0;
    }
}
