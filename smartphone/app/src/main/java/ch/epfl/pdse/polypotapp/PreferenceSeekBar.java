package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

public class PreferenceSeekBar extends DialogPreference {
    private int mValue;
    private String mOriginalSummary;

    public PreferenceSeekBar(Context context) {
        // Delegate to other constructor
        this(context, null);
    }

    public PreferenceSeekBar(Context context, AttributeSet attrs) {
        // Delegate to other constructor
        // Use the preferenceStyle as the default style
        this(context, attrs, R.attr.preferenceStyle);
    }

    public PreferenceSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        // Delegate to other constructor
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public PreferenceSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mOriginalSummary = super.getSummary().toString();
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.preference_seekbar;
    }

    public int getmValue() {
        return mValue;
    }

    public void setmValue(int v) {
        mValue = v;

        // Save to SharedPreference
        persistInt(v);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mValue = this.getPersistedInt(0);
        } else {
            // Set default state from the XML attribute
            mValue = (Integer) defaultValue;
            persistInt(mValue);
        }
    }

    @Override
    public CharSequence getSummary() {
        // Add ability to replace %s by current value
        return String.format(super.getSummary().toString(), mValue);
    }

    public CharSequence getmOriginalSummary() {
        // Add ability to replace %s by current value from original summary
        return String.format(mOriginalSummary, mValue);
    }
}