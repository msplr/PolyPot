package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class TabFragmentConfiguration extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.configuration, rootKey);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        if (preference instanceof PreferenceSeekBar) {
            // Create a new instance of the fragment with the key of the related Preference
            dialogFragment = PreferenceSeekBarFragment.newInstance(preference.getKey());
        } else if (preference instanceof PreferenceNumber) {
            // Create a new instance of the fragment with the key of the related Preference
            dialogFragment = PreferenceNumberFragment.newInstance(preference.getKey());
        } else if (preference instanceof PreferenceString) {
            // Create a new instance of the fragment with the key of the related Preference
            dialogFragment = PreferenceStringFragment.newInstance(preference.getKey());
        }

        if (dialogFragment != null) {
            // The dialog was created (it was one of our custom Preferences), show the dialog for it
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            // Dialog creation could not be handled here. Try with the super method.
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
