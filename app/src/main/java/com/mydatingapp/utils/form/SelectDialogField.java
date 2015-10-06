package com.mydatingapp.utils.form;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.util.Iterator;

/**
 * Created by jk on 12/12/14.
 */
public class SelectDialogField extends ListPreference implements FormField {
    public SelectDialogField(Context context) {
        super(context);
    }

    public SelectDialogField(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        changeSummary();
    }

    @Override
    public void setTitle(java.lang.CharSequence title) {
        super.setTitle(title);
        setDialogTitle(title);
    }

    public void setValues(java.util.Set<java.lang.String> value) {

        if ( value == null || value.size() == 0 )
        {
            return;
        }

        Iterator<String> iterator = value.iterator();

        while ( iterator.hasNext() )
        {
            String item = iterator.next();

            if ( item != null )
            {
                super.setValue(item);
                break;
            }
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        changeSummary();
    }

    protected void changeSummary()
    {
        String value = this.getValue();

        CharSequence[] entries = getEntries();
        CharSequence[] entriesValues = getEntryValues();

        if ( entries == null || value == null || entriesValues == null
                || entries.length == 0 || entriesValues.length == 0 )
        {
            setSummary("");
            return;
        }

        int i = 0;

        for (CharSequence item:entriesValues)
        {
            if( item.equals(value) && entries.length > i )
            {
                setSummary(entries[i]);
                return;
            }
            i++;
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, java.lang.Object defaultValue)
    {
        super.onSetInitialValue(restoreValue, defaultValue);

        changeSummary();
    }
}
