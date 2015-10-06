package com.mydatingapp.utils.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class NumberField extends DialogPreference {

    protected int value = 0;
    protected int maxValue = 100;
    protected int minValue = 0;
    protected int defaultValue = 20;

    protected NumberPicker np= null;

    public NumberField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NumberField(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberField(Context context) {
        super(context, null);
    }


    protected void setMaxValue(int value) {
        maxValue = value;
    }

    protected void setMinValue(int value) {
        minValue = value;
    }

    protected void setDefaultValue(int value) {
        minValue = value;
    }

    protected int getMaxValue() {
        return maxValue;
    }

    protected int getMinValue() {
        return minValue;
    }

    protected int getDefaultValue() {
        return minValue;
    }

    protected void setValue(int value) {

        if ( value < minValue )
        {
            this.value = minValue;
        }
        else if ( value > maxValue )
        {
            this.value = maxValue;
        }
        else
        {
            this.value = value;
        }
        persistString(String.valueOf(value));
        changeSummary();
    }

    @Override
    protected View onCreateDialogView() {
        np = new NumberPicker(getContext());
        np.setWrapSelectorWheel(false);
        return (np);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        np.setMaxValue(maxValue);
        np.setMinValue(minValue);

        np.setValue(value);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            value = np.getValue();
            setValue(value);

            if (callChangeListener(value)) {
                persistString(String.valueOf(value));
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String value = null;

        if (restoreValue) {
            if (defaultValue == null) {
                value = getPersistedString(String.valueOf(this.defaultValue));
            } else {
                value = getPersistedString(defaultValue.toString());
            }
        } else {
            value = defaultValue.toString();
        }

        setValue(Integer.parseInt(value));
    }

    protected void changeSummary() {

    }
}
