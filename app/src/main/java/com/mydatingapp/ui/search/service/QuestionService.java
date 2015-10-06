package com.mydatingapp.ui.search.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;

import com.mydatingapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mydatingapp.ui.search.classes.LocationObject;
import com.mydatingapp.ui.search.classes.QuestionObject;
import com.mydatingapp.utils.SKLocation;
import com.mydatingapp.utils.form.CurrentLocation;
import com.mydatingapp.utils.form.CustomLocation;
import com.mydatingapp.utils.form.DateField;
import com.mydatingapp.utils.form.MultiCheckboxDialogField;
import com.mydatingapp.utils.form.RangeField;
import com.mydatingapp.utils.form.SelectDialogField;
import com.mydatingapp.utils.form.TextField;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by jk on 12/12/14.
 */
public class QuestionService {

    public final String GLOBAL_CURRENT_LOCATION = "global_current_location";

    public final String QUESTION_NAME_CURRENT_LOCATION = "current_location";
    public final String QUESTION_NAME_CURRENT_LOCATION_ADDRESS = "current_location_address";
    public final String QUESTION_NAME_CUSTOM_LOCATION = "custom_location";
    public final String QUESTION_NAME_DISTANCE = "distance";

    public final String  QUESTION_PRESENTATION_TEXT = "text";
    public final String  QUESTION_PRESENTATION_TEXTAREA = "textarea";
    public final String  QUESTION_PRESENTATION_SELECT = "select";
    public final String  QUESTION_PRESENTATION_DATE = "date";
    public final String  QUESTION_PRESENTATION_BIRTHDATE = "birthdate";
    public final String  QUESTION_PRESENTATION_AGE = "age";
    public final String  QUESTION_PRESENTATION_RANGE = "range";
    public final String  QUESTION_PRESENTATION_LOCATION = "googlemap_location";
    public final String  QUESTION_PRESENTATION_CHECKBOX = "checkbox";
    public final String  QUESTION_PRESENTATION_MULTICHECKBOX = "multicheckbox";
    public final String  QUESTION_PRESENTATION_RADIO = "radio";
    public final String  QUESTION_PRESENTATION_URL = "url";
    public final String  QUESTION_PRESENTATION_PASSWORD = "password";

    private static ArrayList<String> basicQuestionList = new ArrayList<String>();

    private static QuestionService newInstance = null;

    public static QuestionService getInstance() {
        if ( newInstance == null )
        {
            newInstance = new QuestionService();
        }

        return newInstance;
    }

    private QuestionService() {
        basicQuestionList.add("sex");
        basicQuestionList.add("match_sex");
        basicQuestionList.add("distance");
        basicQuestionList.add("custom_location");
        basicQuestionList.add("current_location");
        basicQuestionList.add("current_location_address");
        basicQuestionList.add("relationship");
        basicQuestionList.add("birthdate");
    }

    public Preference getFormField(Context context, QuestionParams questionParams, String prefix) {
        return getFormField(context, questionParams, prefix, null);
    }

    public Preference getFormField(Context context, QuestionParams questionParams, String prefix, PreferenceGroup screen) {
        String name = questionParams.getName();
        String label = questionParams.getLabel();
        String presentation = questionParams.getPresentation();
        ArrayList<HashMap<String,String>> options = questionParams.getOptions();
        HashMap<String,String> custom = questionParams.getCustom();

        CharSequence[] entries = new CharSequence[0];
        CharSequence[] entryValues = new CharSequence[0];

        if ( options != null && options.size() > 0 )
        {
            entries = new CharSequence[options.size()];
            entryValues = new CharSequence[options.size()];

            int  i = 0;

            for ( HashMap<String,String> item:options )
            {
                entries[i] = item.get("label");
                entryValues[i] = item.get("value");
                i++;
            }
        }

        Preference field = null;

        if ( presentation == null )
        {
            return field;
        }

        name = prefix + name;

        switch( presentation )
        {
            case QUESTION_PRESENTATION_TEXT:
            case QUESTION_PRESENTATION_TEXTAREA:
            case QUESTION_PRESENTATION_URL:
                field = new TextField(context);
                field.setTitle(label);
                field.setKey(name);

                if ( questionParams.value != null ) {
                    ((TextField) field).setValue(questionParams.value);
                }
                break;

            case QUESTION_PRESENTATION_RADIO:
            case QUESTION_PRESENTATION_SELECT:

                field = new SelectDialogField(context);
                field.setTitle(label);
                field.setKey(name);
                ((SelectDialogField) field).setEntries(entries);
                ((SelectDialogField) field).setEntryValues(entryValues);
                if ( questionParams.value != null ) {
                    ((SelectDialogField) field).setValue(questionParams.value);
                }
                break;
            case QUESTION_PRESENTATION_MULTICHECKBOX:

                field = new MultiCheckboxDialogField(context);
                field.setTitle(label);
                field.setKey(name);
                ((MultiCheckboxDialogField) field).setEntries(entries);
                ((MultiCheckboxDialogField) field).setEntryValues(entryValues);

                if ( questionParams.value != null ) {
                    ((MultiCheckboxDialogField) field).setValues(questionParams.value);
                }

                break;

            case QUESTION_PRESENTATION_CHECKBOX:

                field = new CheckBoxPreference(context);
                field.setTitle(label);
                field.setKey(name);

                if ( questionParams.value != null ) {
                    ((CheckBoxPreference) field).setEnabled(Boolean.parseBoolean(questionParams.value));
                }

                break;

            case QUESTION_PRESENTATION_RANGE:

                field = new RangeField(context);
                field.setTitle(label);
                field.setKey(name);

                if ( custom != null ) {
                    int from  = Integer.parseInt(custom.get("from"));
                    int to  = Integer.parseInt(custom.get("to"));

                    GregorianCalendar c = new GregorianCalendar();

                    int toValue = c.get(Calendar.YEAR) - from;
                    int fromValue  = c.get(Calendar.YEAR) - to;
                    ((RangeField)field).setRange(fromValue, toValue);
                }

                if ( questionParams.value != null ) {
                    ((RangeField) field).setValue(questionParams.value);
                }

                break;

            case QUESTION_PRESENTATION_DATE:
            case QUESTION_PRESENTATION_BIRTHDATE:
            case QUESTION_PRESENTATION_AGE:

                field = new DateField(context);
                field.setTitle(label);
                field.setKey(name);

                if ( questionParams.custom != null && questionParams.custom instanceof HashMap )
                {
                    if ( questionParams.custom.containsKey("from") && questionParams.custom.containsKey("to") )
                    {
                        ((DateField) field).setMinYear(Integer.parseInt(questionParams.custom.get("from")));
                        ((DateField) field).setMaxYear(Integer.parseInt(questionParams.custom.get("to")));
                    }
                }

                if ( questionParams.value != null ) {
                    ((DateField) field).setValue(questionParams.value);
                }

                break;

            case QUESTION_PRESENTATION_LOCATION:

                field = new CustomLocation(context);
                field.setKey(prefix + QUESTION_NAME_CUSTOM_LOCATION);
                field.setTitle(context.getResources().getString(R.string.location_edit_title));

                if ( questionParams.value != null ) {
                    ((CustomLocation) field).setValue(questionParams.value);
                }

                break;

            default:

                break;
        }

        return field;
    }

    public Preference getSearchFormField(Context context, QuestionParams getQuestionParams, String prefix) {
        String name = getQuestionParams.getName();
        String label = getQuestionParams.getLabel();
        String presentation = getQuestionParams.getPresentation();
        ArrayList<HashMap<String,String>> options = getQuestionParams.getOptions();
        HashMap<String,String> custom = getQuestionParams.getCustom();

        CharSequence[] entries = new CharSequence[0];
        CharSequence[] entryValues = new CharSequence[0];

        if ( options != null && options.size() > 0 )
        {
            entries = new CharSequence[options.size()];
            entryValues = new CharSequence[options.size()];

            int  i = 0;

            for ( HashMap<String,String> item:options )
            {
                entries[i] = item.get("label");
                entryValues[i] = item.get("value");
                i++;
            }
        }

        Preference field = null;

        if ( presentation == null )
        {
            return field;
        }

        name = prefix + name;

        switch( presentation )
        {
            case QUESTION_PRESENTATION_TEXT:
            case QUESTION_PRESENTATION_TEXTAREA:
            case QUESTION_PRESENTATION_URL:
                field = new TextField(context);
                field.setTitle(label);
                field.setKey(name);
                ((TextField)field).setValue("");
                break;

            case QUESTION_PRESENTATION_RADIO:
            case QUESTION_PRESENTATION_SELECT:
            case QUESTION_PRESENTATION_MULTICHECKBOX:

                if ( (prefix+"match_sex").equals(name) ) {
                    field = new SelectDialogField(context);
                    field.setTitle(label);
                    field.setKey(name);
                    ((SelectDialogField) field).setEntries(entries);
                    ((SelectDialogField) field).setEntryValues(entryValues);
                }
                else {
                    field = new MultiCheckboxDialogField(context);
                    field.setTitle(label);
                    field.setKey(name);
                    ((MultiCheckboxDialogField) field).setEntries(entries);
                    ((MultiCheckboxDialogField) field).setEntryValues(entryValues);
                }

                break;

            case QUESTION_PRESENTATION_CHECKBOX:

                field = new CheckBoxPreference(context);
                field.setTitle(label);
                field.setKey(name);

                break;

            case QUESTION_PRESENTATION_DATE:

                //field = new Date

                break;

            case QUESTION_PRESENTATION_BIRTHDATE:
            case QUESTION_PRESENTATION_AGE:

                field = new RangeField(context);
                field.setTitle(label);
                field.setKey(name);

                if ( custom != null ) {
                    int from  = Integer.parseInt(custom.get("from"));
                    int to  = Integer.parseInt(custom.get("to"));

                    GregorianCalendar c = new GregorianCalendar();

                    int toValue = c.get(Calendar.YEAR) - from;
                    int fromValue  = c.get(Calendar.YEAR) - to;
                    ((RangeField)field).setRange(fromValue, toValue);
                }

                break;

            case QUESTION_PRESENTATION_LOCATION:

                field = new CurrentLocation(context);
                field.setTitle(context.getResources().getString(R.string.current_location_default_title));
                field.setKey(prefix+QUESTION_NAME_CURRENT_LOCATION);

                break;

            default:

                break;
        }

        return field;
    }

    public int getIntValue(Set<String> set)
    {
        if ( set == null || set.size() == 0 )
        {
            return 0;
        }

        int summ = 0;

        for ( String el:set)
        {
            summ += Integer.parseInt(el);
        }

        return summ;
    }

    public HashMap<String, String> getData(Context context, String prefix) {
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, ?> map = (HashMap<String, String>) PreferenceManager.getDefaultSharedPreferences(context).getAll();

        for (  Map.Entry<String,?> item:map.entrySet() ) {
            String key = (String) item.getKey();
            String value = String.valueOf(item.getValue());

            if ( item.getValue() instanceof Set) {
                HashSet<String> set = (HashSet<String>)item.getValue();
                value = String.valueOf(QuestionService.getInstance().getIntValue(set));
            }

            if ( key != null && key.startsWith(prefix) )
            {
                String name = key.substring(prefix.length());

                if (QUESTION_NAME_CURRENT_LOCATION.equals(name)) {
                    Address address = getCurrentLocation(context);
                    if (address != null) {
                        params.put(QUESTION_NAME_CURRENT_LOCATION_ADDRESS, toJson(new LocationObject(address)));
                    }
                }

                if (QUESTION_NAME_CUSTOM_LOCATION.equals(name)) {
                    if (value != null) {
                        LocationObject location = fromJson(value, LocationObject.class);
                        params.put(name, toJson(location));
                    }
                } else {
                    params.put(name, value);
                }
            }
        }

        return params;
    }

    public void removeSharedPreferences(Context context, String prefix) {
        HashMap<String, ?> map = (HashMap<String, String>) PreferenceManager.getDefaultSharedPreferences(context).getAll();
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();

        for (  Map.Entry<String,?> item:map.entrySet() ) {
            String key = (String) item.getKey();

            if ( key != null && key.startsWith(prefix) )
            {
                edit.remove(key);
            }
        }

        edit.commit();
    }

    public HashMap<String, String> getFilterData(Context context, String prefix) {
        HashMap<String, String> data = getData(context, prefix);
        boolean enableAdvancedOptions = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("advancedOptions", false);

        ArrayList<String> removeList = new ArrayList<String>();

        for( Map.Entry<String, String> item:data.entrySet() ) {
            String name = item.getKey();

            if ( !basicQuestionList.contains(name) && !enableAdvancedOptions ) {
                if ( data.containsKey(name) ) {
                    removeList.add(name);
                }
            }
        }

        for( String name:removeList ) {
            data.remove(name);
        }

        return data;
    }

    private Gson getGson() {
        ExclusionStrategy ex = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };
        Gson gson = new GsonBuilder().addDeserializationExclusionStrategy(ex).addSerializationExclusionStrategy(ex).create();
        return gson;
    }

    public String toJson(Object object) {
        return getGson().toJson(object);
    }

    public <T> T fromJson(String json, Class<T> classOfT) {
        return getGson().fromJson(json, classOfT);
    }

    public void saveCurrentLocation(Context context, Address address) {
        SharedPreferences.Editor edit =  PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString(GLOBAL_CURRENT_LOCATION, toJson(address));
        edit.commit();
    }

    public Address getCurrentLocation(Context context) {
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString(GLOBAL_CURRENT_LOCATION, "");
        return fromJson(json, Address.class);
    }

    public void updateCurrentLocation(final Context context) {
        SKLocation.SKGoogleLocation location = new SKLocation.SKGoogleLocation(context) {
            @Override
            public void onConnectionSuspended(int i) {

            }

            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {

            }

            @Override
            public void onConnected(Location lastLocation) {

            }

            @Override
            public void onLocationInfoReady(SKLocation.SKLocationInfo skLocationInfo) {
                if ( skLocationInfo != null ) {
                    QuestionService.getInstance().saveCurrentLocation(context, skLocationInfo.address);
                }
            }
        };

        location.connect();
    }

    public QuestionParams getQuestionParams(HashMap map) {
        return new QuestionParams(map);
    }

    public static class QuestionParams implements Parcelable {
        String name;
        String label;
        String presentation;
        ArrayList<HashMap<String,String>> options;
        HashMap<String,String> custom;
        String value = null;
        String sectionName;
        Boolean required;


        public QuestionParams ()
        {
            String value = null;
        }

        public QuestionParams ( HashMap map )
        {
            name = ( map.containsKey("name") && null != map.get("name") ) ? (String) map.get("name") : null;
            label = ( map.containsKey("label") && null != map.get("label") ) ? (String) map.get("label") : null;
            presentation = ( map.containsKey("presentation") && null != map.get("presentation") ) ? (String) map.get("presentation") : null;
            options = ( map.containsKey("options") && null != map.get("options") ) ? (ArrayList<HashMap<String,String>>) map.get("options") : null;
            custom = ( map.containsKey("custom") && null != map.get("custom") ) ? (HashMap<String,String>) map.get("custom") : null;
            sectionName = ( map.containsKey("sectionName") && null != map.get("sectionName") ) ? (String) map.get("sectionName") : null;
            required = ( map.containsKey("required") && null != map.get("required") ) ? map.get("required").equals("1") : null;
        }

        public QuestionParams ( QuestionObject map )
        {
            name = map.getName();
            label = map.getLabel();
            presentation = map.getPresentation();

            if ( map.getOptions() != null && map.getOptions().length > 0  )
            {
                options = new ArrayList<HashMap<String,String>>();

                for ( QuestionObject.QuestionOption item:map.getOptions() )
                {
                    HashMap<String,String> option = new HashMap<String,String>();
                    option.put("value",item.getValue());
                    option.put("label",item.getLabel());
                    options.add(option);
                }
            }

            if ( map.getCustom() != null && map.getCustom().getYearRange() != null  )
            {
                custom = new HashMap<String,String>();
                custom.put("from", map.getCustom().getYearRange().getFrom());
                custom.put("to", map.getCustom().getYearRange().getTo());
            }

            value = map.getValue();
        }

        public void setName ( String name ) {
            this.name = name;
        }

        public void setLabel ( String label ) {
            this.label = label;
        }

        public void setPresentation ( String presentation ) {
            this.presentation = presentation;
        }

        public void setOptions ( ArrayList<HashMap<String,String>> options ) {
            this.options = options;
        }

        public void setCustom ( HashMap<String,String> map ) {
            custom = map;
        }

        public String getName () {
            return this.name;
        }

        public String getLabel () {
            return this.label;
        }

        public String getPresentation () {
            return presentation;
        }

        public ArrayList<HashMap<String,String>> getOptions () {
            return options;
        }

        public HashMap<String,String> getCustom () {
            return custom;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getSectionName() {
            return sectionName;
        }

        public void setSectionName(String sectionName) {
            this.sectionName = sectionName;
        }

        public Boolean isRequired() {
            return required != null && required == true;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }

        public static final Parcelable.Creator<QuestionParams> CREATOR = new Creator<QuestionParams>() {
            public QuestionParams createFromParcel(Parcel source) {
                QuestionParams mQuestionParams = new QuestionParams();

                mQuestionParams.name = source.readString();
                mQuestionParams.label = source.readString();
                mQuestionParams.presentation = source.readString();
                mQuestionParams.options = (ArrayList<HashMap<String,String>>)source.readSerializable();
                mQuestionParams.custom = (HashMap<String,String>)source.readSerializable();
                mQuestionParams.sectionName = source.readString();
                mQuestionParams.required = source.readByte() != 0;

                return mQuestionParams;
            }
            public QuestionParams[] newArray(int size) {
                return new QuestionParams[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(name);
            parcel.writeString(label);
            parcel.writeString(presentation);
            parcel.writeSerializable(options);
            parcel.writeSerializable(custom);
            parcel.writeString(sectionName);
            parcel.writeByte((byte)(required != null && required ? 1 : 0));
        }
    }

    public static class LocationField {

    }
}
