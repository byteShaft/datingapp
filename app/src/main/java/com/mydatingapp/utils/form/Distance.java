package com.mydatingapp.utils.form;

import android.content.Context;
import android.util.AttributeSet;

import com.mydatingapp.R;

/**
 * Created by jk on 12/30/14.
 */
public class Distance extends SingleRangeField implements FormField {

    Context context;

    public Distance(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public Distance(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Distance(Context context) {
        super(context);
        init(context);
    }

    protected void init(Context context)
    {
        super.init(context);
        this.context = context;
        setDialogTitle(context.getResources().getString(R.string.location_distance_default_dialog_title));
        setTitle(context.getResources().getString(R.string.location_distance_default_title));
    }

    protected void changeSummary() {
        setSummary(mValue + " " + context.getResources().getString(R.string.location_distance_from));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String value = "50";

        if (restoreValue) {
            if (defaultValue == null) {
                if ( mRange != null && mRange[0] != null ) {
                    value = getPersistedString(String.valueOf(mRange[0]));
                }
            } else {
                value = getPersistedString(defaultValue.toString());
            }
        } else {
            value = defaultValue.toString();
        }

        setValue(Integer.parseInt(value));
    }
}
