package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class PreferenceString extends DialogPreference {
    private EditText mEditText;

    private final String mHint;
    private final String mType;
    private String mPreviousValue;
    private String mValue;

    public PreferenceString(Context context) {
        // Delegate to other constructor
        this(context, null);
    }

    public PreferenceString(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferenceString(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.layout_preference);
        setWidgetLayoutResource(R.layout.preference_string);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PreferenceString);
        mHint = a.getString(R.styleable.PreferenceString_hint);
        mType = a.getString(R.styleable.PreferenceString_type);
        a.recycle();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setClickable(false);

        mEditText = (EditText) holder.findViewById(R.id.string);

        mEditText.setText(mValue);

        if(mHint != null) {
            mEditText.setHint(mHint);
        }

        if(mType != null) {
            if (mType.equals("password")) {
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else if (mType.equals("uri")) {
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
            }
        }

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