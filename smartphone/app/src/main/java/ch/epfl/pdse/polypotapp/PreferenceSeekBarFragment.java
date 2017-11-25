package ch.epfl.pdse.polypotapp;

import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.os.Bundle;
import android.widget.SeekBar;

public class PreferenceSeekBarFragment extends PreferenceDialogFragmentCompat {

    private SeekBar seekBar;

    /**
     * Creates a new Instance of the PreferenceSeekBarDialogFragment and stores the key of the
     * related Preference
     *
     * @param key The key of the related Preference
     * @return A new Instance of the PreferenceSeekBarDialogFragment
     */
    public static PreferenceSeekBarFragment newInstance(String key) {
        final PreferenceSeekBarFragment fragment = new PreferenceSeekBarFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);

        // Get the value from the related Preference
        Integer value = ((PreferenceSeekBar) getPreference()).getmValue();

        seekBar.setProgress(value);

        //TODO: min/max, show current value
    }

    /**
     * Called when the Dialog is closed.
     *
     * @param positiveResult Whether the Dialog was accepted or canceled.
     */
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Generate value to save
            int value = seekBar.getProgress();

            // Save the value
            PreferenceSeekBar preference = (PreferenceSeekBar) getPreference();
            preference.setmValue(value);

            // Update the summary
            preference.setSummary(preference.getmOriginalSummary());

        }
    }
}