package com.mydatingapp.utils.form;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import com.mydatingapp.ui.search.service.QuestionService;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jk on 12/12/14.
 */
public class MultiCheckboxDialogField extends MultiSelectListPreference implements FormField {

    public MultiCheckboxDialogField(Context context) {
        super(context);
    }

    public MultiCheckboxDialogField(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        changeSummary();
    }

    public void setValues(String value) {
        CharSequence[] entriesValues = getEntryValues();

        int summ = Integer.parseInt(value);

        if ( entriesValues.length == 0 || summ == 0 )
        {
            setSummary("");
            return;
        }

        HashSet<String> set = new HashSet<String>();

        for (CharSequence item:entriesValues)
        {
            int val = Integer.parseInt(item.toString());

            if ( (val & summ) > 0 )
            {
                set.add(String.valueOf(val));
            }
        }
        setValues(set);
    }

    @Override
    public void setTitle(java.lang.CharSequence title) {
        super.setTitle(title);
        setDialogTitle(title);
    }

    public void setValue(String value) {
        setValues(value);
    }

    @Override
    public String getValue() {
        return String.valueOf(QuestionService.getInstance().getIntValue(getValues()));
    }

    @Override
    public void setValues(java.util.Set<java.lang.String> value) {
        super.setValues(value);
        changeSummary();
    }

    protected void changeSummary()
    {
        Set<String> values = this.getValues();

        CharSequence[] entries = getEntries();
        CharSequence[] entriesValues = getEntryValues();

        if ( entries == null || values == null || entriesValues == null
                || values.size() == 0 || entries.length == 0 || entriesValues.length == 0 )
        {
            setSummary("");
            return;
        }

        String summary = "";
        int i = 0;

        for (CharSequence item:entriesValues)
        {
            if( values.contains(item) && entries.length > i )
            {
                if ( summary.length() > 0 )
                {
                    summary += ", ";
                }

                summary += entries[i];
            }
            i++;
        }

        setSummary(summary);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, java.lang.Object defaultValue)
    {
        super.onSetInitialValue(restoreValue, defaultValue);

        changeSummary();
    }
}
