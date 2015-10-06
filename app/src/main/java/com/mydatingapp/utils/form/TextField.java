package com.mydatingapp.utils.form;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by jk on 12/15/14.
 */
public class TextField extends EditTextPreference implements FormField {
    public TextField(Context context) {
        super(context);
    }

    public TextField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TextField(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTitle(java.lang.CharSequence title) {
        super.setTitle(title);
        setDialogTitle(title);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        changeSummary();
    }

    public void setValue(String value) {
        setText(value);
    }

    @Override
    public String getValue() {
        return this.getText();
    }

    public void setText(String value) {
        super.setText(value);
        changeSummary();
    }

    protected void changeSummary()
    {
        String value = this.getText();

        setSummary(value);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, java.lang.Object defaultValue)
    {
        String text = null;

        if ( getValue() == null ) {
            if (restoreValue) {
                if (defaultValue == null) {
                    text = getPersistedString("");
                } else {
                    text = getPersistedString(defaultValue.toString());
                }
            } else {
                text = defaultValue.toString();
            }
        }
        else
        {
            text = getValue();
        }

        setValue(text);

        changeSummary();
    }
}
