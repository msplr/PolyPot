package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

public class PreferenceNumber extends Preference implements Preference.OnPreferenceChangeListener {
    private EditText mEditText;

    private String mValue;

    public PreferenceNumber(Context context) {
        // Delegate to other constructor
        this(context, null);
    }

    public PreferenceNumber(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferenceNumber(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //setLayoutResource(R.layout.layout_preference);
        setWidgetLayoutResource(R.layout.preference_number);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setClickable(false);

        mEditText = (EditText) holder.findViewById(R.id.number);

        mEditText.setText(mValue);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                mValue = editable.toString();
                persistString(mValue);
            }
        });
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mValue = this.getPersistedString("");
        } else {
            // Set default state from the XML attribute
            mValue = (String) defaultValue;
            persistString(mValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        mValue = (String) newValue;

        mEditText.setText(mValue);

        return true;
    }
}