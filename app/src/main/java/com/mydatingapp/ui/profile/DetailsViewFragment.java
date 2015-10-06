package com.mydatingapp.ui.profile;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mydatingapp.R;
import com.mydatingapp.core.SkServiceCallbackListener;
import com.mydatingapp.model.base.BaseRestCommand;
import com.mydatingapp.model.base.BaseServiceHelper;
import com.mydatingapp.ui.base.SkBaseInnerActivity;
import com.mydatingapp.ui.search.EditProfileActivity;
import com.mydatingapp.utils.SKDimensions;
import com.mydatingapp.utils.SkApi;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sardar on 10/7/14.
 */
public class DetailsViewFragment extends Fragment {

    private BaseServiceHelper helper;
    private Integer userId;
    private ArrayList<String> sectionOrder;
    private ArrayList<String> questionOrder;
    private HashMap<String, HashMap<String, String>> qData;
    private HashMap<String, String> secLabels;
    private Boolean owner;
    private SkBaseInnerActivity activity;
    private LinearLayout layout;
    private RelativeLayout pendingLayout;
    private ImageView avatar;
    private TextView details, compat;
    private ScrollView scrollView;
    private ProgressBar progressBar;
    private ImageView onlineIndicator;
    private JsonObject profileData;
    private boolean reloadData = false;

    private BroadcastReceiver mDataChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadData = true;
        }
    };

    public static DetailsViewFragment newInstance(int userId, Boolean owner, JsonObject data) {
        DetailsViewFragment fr = new DetailsViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("userId", userId);
        bundle.putBoolean("owner", owner);
        bundle.putSerializable("profileData", data.toString());
        fr.setArguments(bundle);
        return fr;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        userId = bundle.getInt("userId");
        owner = bundle.getBoolean("owner");
        profileData = new Gson().fromJson(bundle.getString("profileData"), JsonObject.class);
        activity = (SkBaseInnerActivity) getActivity();
        helper = activity.getBaseHelper();

        setHasOptionsMenu(true);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDataChangeReceiver, new IntentFilter("base.profile_question_changed"));
    }

    @Override
    public void onResume() {
        super.onResume();

        if( !reloadData ){
            return;
        }

        scrollView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        HashMap<String, String> params = new HashMap();
        params.put("userId", String.valueOf(userId));

        ((SkBaseInnerActivity)getActivity()).getBaseHelper().runRestRequest(BaseRestCommand.ACTION_TYPE.PROFILE_INFO, params, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bData) {
                if (SkApi.checkResult(bData) == SkApi.API_RESULT.SUCCESS) {
                    updateView(SkApi.processResult(bData));
                }
            }
        });

        reloadData = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.profile_view_details_fragment, container, false);
        layout = (LinearLayout) fragmentView.findViewById(R.id.layout);
        avatar = (ImageView) fragmentView.findViewById(R.id.avatar);
        details = (TextView) fragmentView.findViewById(R.id.details);
        scrollView = (ScrollView)fragmentView.findViewById(R.id.scrollView);
        progressBar = (ProgressBar) fragmentView.findViewById(R.id.progressBar);
        compat = (TextView) fragmentView.findViewById(R.id.compat);
        onlineIndicator = (ImageView) fragmentView.findViewById(R.id.onlineStatus);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        avatar.setLayoutParams(new RelativeLayout.LayoutParams(size.x, size.x));

        pendingLayout = (RelativeLayout)fragmentView.findViewById(R.id.profile_pending_layout);
        pendingLayout.setLayoutParams(new RelativeLayout.LayoutParams(size.x, size.x));

        updateView(profileData);

        // Inflate the layout for this fragment
        return fragmentView;
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDataChangeReceiver);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if( owner ){
            inflater.inflate(R.menu.profile_view_details_fragment, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (owner && item.getItemId() == R.id.edit_btn) {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateView( JsonObject mData ){
        sectionOrder = new ArrayList();
        questionOrder = new ArrayList();
        qData = new HashMap<>();
        secLabels = new HashMap<>();
        layout.removeAllViews();

        JsonArray arr = mData.getAsJsonArray("viewSections"), tempArr;
        JsonObject temp;

        for (int i = 0; i < arr.size(); i++) {
            temp = arr.get(i).getAsJsonObject();
            secLabels.put(temp.get("name").getAsString(), temp.get("label").getAsString());
            qData.put(temp.get("name").getAsString(), new HashMap<String, String>());
            sectionOrder.add(temp.get("name").getAsString());
        }

        arr = mData.getAsJsonArray("viewQuestions");
        HashMap<String, String> tempQData;
        String value;
        JsonArray valArr;

        for (int i = 0; i < arr.size(); i++) {
            tempArr = arr.get(i).getAsJsonArray();

            for (int j = 0; j < tempArr.size(); j++) {
                temp = tempArr.get(j).getAsJsonObject();
                tempQData = qData.get(temp.get("section").getAsString());

                if (tempQData == null) {
                    tempQData = new HashMap<String, String>();
                    qData.put(temp.get("section").getAsString(), tempQData);
                }

                if (temp.get("value").isJsonArray()) {
                    valArr = temp.get("value").getAsJsonArray();
                    value = "";
                    for (int k = 0; k < valArr.size(); k++) {
                        value += valArr.get(k).getAsString();

                        if ((k + 1) < valArr.size()) {
                            value += ", ";
                        }
                    }
                } else {
                    value = temp.get("value").getAsString();
                }

                tempQData.put(temp.get("label").getAsString(), value);
                questionOrder.add(temp.get("label").getAsString());
            }
        }

        for (String key : sectionOrder) {
            layout.addView(new SectionTextView(getActivity(), secLabels.get(key)));

            for (String name : questionOrder) {
                if ( qData.get(key).containsKey(name) ) {
                    layout.addView(new QstNameTextView(getActivity(), name));
                    layout.addView(new QstValueTextView(getActivity(), qData.get(key).get(name)));
                }
            }
        }

        final JsonObject personalData = mData.get("viewBasic").getAsJsonObject();

        if( SkApi.propExistsAndNotNull(personalData, "online") && personalData.get("online").getAsBoolean() ){
            details.setPadding(
                    SKDimensions.convertDpToPixel(30, getActivity()),
                    SKDimensions.convertDpToPixel(8, getActivity()),
                    SKDimensions.convertDpToPixel(8, getActivity()),
                    SKDimensions.convertDpToPixel(8, getActivity())
            );

            onlineIndicator.setVisibility(View.VISIBLE);
        }

        try {
            Picasso.with(getActivity()).load(personalData.get("avatar").getAsJsonObject().get("bigAvatarUrl").getAsString()).centerCrop().fit().into(avatar, new Callback() {
                @Override
                public void onSuccess() {
                    JsonObject avatar = personalData.get("avatar").getAsJsonObject();

                    if ( avatar.has("approved") && !avatar.get("approved").getAsBoolean() ) {
                        pendingLayout.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception ex) {
            Log.i(" LOAD AVATAR ", ex.getMessage(), ex);
            avatar.setImageResource(R.drawable.no_avatar);
        }

        String htmlString = "<font color=\"#000000\">";

        if( SkApi.propExistsAndNotNull(personalData, "realname") ){
            htmlString += "<b>" + personalData.get("realname").getAsString() + "</b> ";
        }

        if( SkApi.propExistsAndNotNull(personalData, "sex") ){
            htmlString += personalData.get("sex").getAsString() + ", ";
        }

        if( SkApi.propExistsAndNotNull(personalData, "birthdate") ){
            htmlString += personalData.get("birthdate").getAsString();
//        ((LinearLayout)fragmentView.findViewById(R.id.profile_pending_layout)).setLayoutParams(new RelativeLayout.LayoutParams(size.x, size.x));
        }

        if( SkApi.propExistsAndNotNull(personalData, "location") ){
            htmlString += "<font color=\"#fefefe\"> " + personalData.get("location").getAsString() + "</font>";
        }

        htmlString += "</font> ";

        details.setText(Html.fromHtml(htmlString));

        if(SkApi.propExistsAndNotNull(personalData, "compatibility")){
            compat.setText(getResources().getString(R.string.profile_view_compat_label) + ": " + personalData.get("compatibility").getAsString() + "%");
        }
        else{
            compat.setVisibility(View.GONE);
        }

        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    private class SectionTextView extends TextView {
        public SectionTextView(Context context, String value) {
            super(context);
            setText(value);
            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            setTextColor(Color.parseColor("#87868E"));
            setBackgroundResource(R.drawable.qst_section_border_bottom);
            setPadding(0, SKDimensions.convertDpToPixel(20, context), 0, SKDimensions.convertDpToPixel(5, context));
            setAllCaps(true);
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            setTypeface(null, Typeface.BOLD);
        }
    }

    private class QstNameTextView extends TextView {
        public QstNameTextView(Context context, String value) {
            super(context);
            setText(value);
            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            setTextColor(Color.parseColor("#000000"));
            setPadding(0, SKDimensions.convertDpToPixel(10, context), 0, SKDimensions.convertDpToPixel(5, context));
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        }
    }

    private class QstValueTextView extends TextView {
        public QstValueTextView(Context context, String value) {
            super(context);
            setText(value);
            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            setTextColor(Color.parseColor("#87868E"));
            setBackgroundResource(R.drawable.qst_value_border_bottom);
            setPadding(0, 0, 0, SKDimensions.convertDpToPixel(10, context));
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        }
    }
}
