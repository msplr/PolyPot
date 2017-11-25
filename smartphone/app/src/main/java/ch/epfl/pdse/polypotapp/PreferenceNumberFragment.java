package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView.BufferType;

public class PreferenceNumberFragment extends PreferenceDialogFragmentCompat {

    private EditText mNumberEditText;

    /**
     * Creates a new Instance of the PreferenceEditTextDialogFragment and stores the key of the
     * related Preference
     *
     * @param key The key of the related Preference
     * @return A new Instance of the PreferenceEditTextDialogFragment
     */
    public static PreferenceNumberFragment newInstance(String key) {
        final PreferenceNumberFragment fragment = new PreferenceNumberFragment();
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

        mNumberEditText = (EditText) view.findViewById(R.id.number);

        // Get the value from the related Preference
        String value = ((PreferenceNumber) getPreference()).getmValue();

        mNumberEditText.setText(value, BufferType.EDITABLE);

        //TODO: min/max
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
            String value = mNumberEditText.getText().toString();

            // Save the value
            PreferenceNumber preference = (PreferenceNumber) getPreference();
            preference.setmValue(value);

            // Update the summary
            preference.setSummary(preference.getmOriginalSummary());

        }
    }
}