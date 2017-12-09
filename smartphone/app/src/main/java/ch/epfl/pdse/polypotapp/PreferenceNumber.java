package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Timer;
import java.util.TimerTask;

public class PreferenceNumber extends DialogPreference {
    private EditText mEditText;

    private String mPreviousValue;
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
                if(mPreviousValue == null) {
                    mPreviousValue = mValue;
                }

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
    public void onAttached() {
        super.onAttached();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetached() {
        super.onDetached();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void handlePreferenceChange(ActivityMain.PreferenceChanged event) {
        if(event.key.equals(getKey())) {
            if(event.failed && mPreviousValue != null) {
                mValue = mPreviousValue;
                persistString(mValue);

                mPreviousValue = null;
                notifyChanged();
            } else if(!event.failed) {
                mPreviousValue = null;
            }
        }
    }
}