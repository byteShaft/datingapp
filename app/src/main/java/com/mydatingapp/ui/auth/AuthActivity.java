package com.mydatingapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mydatingapp.R;
import com.mydatingapp.SkStaticData;
import com.mydatingapp.core.SkApplication;
import com.mydatingapp.core.SkServiceCallbackListener;
import com.mydatingapp.model.base.BaseRestCommand;
import com.mydatingapp.model.base.classes.SkSite;
import com.mydatingapp.model.base.classes.SkUser;
import com.mydatingapp.ui.base.MainFragmentActivity;
import com.mydatingapp.ui.base.StatusActivity;
import com.mydatingapp.ui.fbconnect.FacebookLoginStepManager;
import com.mydatingapp.ui.fbconnect.service.FacebookConnectService;
import com.mydatingapp.ui.search.fragments.EditFormFragment;
import com.mydatingapp.ui.search.fragments.SearchFormFragment;
import com.mydatingapp.ui.search.service.QuestionService;
import com.mydatingapp.utils.SkApi;

import java.util.HashMap;

/**
 * Created by sardar on 4/30/14.
 */
public class AuthActivity extends BaseAuthActivity {

    private EditText mEmailView;
    private EditText mPasswordView;
    private UiLifecycleHelper uiHelper;
    private Boolean blockButton = false;
    private ProgressBar progress;
    private TextView siteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.auth_auth_activity);
        super.onCreate(savedInstanceState);

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        siteName = (TextView) findViewById(R.id.siteName);


        TextView changeSite = (TextView) findViewById(R.id.changeSite);
        changeSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AuthActivity.this, UrlActivity.class));
            }
        });

        if (SkStaticData.SITE_URL != null) {
            changeSite.setVisibility(View.GONE);
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        final Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        final LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setReadPermissions("email");
        authButton.setSessionStatusCallback(new SessionStatusCallback());
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });
        uiHelper.onCreate(savedInstanceState);

        Session session = Session.getActiveSession();

        if (session != null && !session.isClosed()) {
            session.closeAndClearTokenInformation();
        }

        getBaseHelper().runRestRequest(BaseRestCommand.ACTION_TYPE.SITE_INFO, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                getApp().saveOrUpdateSiteInfo(data, getApp());
                JsonObject dataObject = SkApi.processResult(data);

                if( SkApi.propExistsAndNotNull(dataObject, "isUserAuthenticated") ){
                    if( dataObject.get("isUserAuthenticated").getAsBoolean() ){
                        startActivity(new Intent(AuthActivity.this, MainFragmentActivity.class));
                    }
                }

                siteName.setText(SkApplication.getSiteInfo().getSiteName());
                mEmailView.setVisibility(View.VISIBLE);
                mPasswordView.setVisibility(View.VISIBLE);
                mEmailSignInButton.setVisibility(View.VISIBLE);

                AuthActivity.this.findViewById(R.id.auth_progress_bar).setVisibility(View.GONE);
                AuthActivity.this.findViewById(R.id.initialPBar).setVisibility(View.GONE);

                SkSite siteInfo = SkApplication.getSiteInfo();

                if ( siteInfo != null && siteInfo.getFacebookAppId() != null ) {
                    authButton.setVisibility(View.VISIBLE);
                    authButton.setApplicationId(siteInfo.getFacebookAppId());
                }
            }
        });
    }

    private void attemptLogin() {
        if (blockButton) {
            return;
        }

        blockButton = true;
        progress.setVisibility(View.VISIBLE);

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            blockButton = false;
            progress.setVisibility(View.GONE);

        } else {

            HashMap<String, String> params = new HashMap();
            params.put("username", email);
            params.put("password", password);

            //need to remove auth token
            SkApplication.setAuthToken(null);
            baseHelper.runRestRequest(BaseRestCommand.ACTION_TYPE.AUTHENTICATE, params, new SkServiceCallbackListener() {
                @Override
                public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                    JsonObject resultData = SkApi.processResult(data);
                    if (resultData != null) {
                        if (SkApi.propExistsAndNotNull(resultData, "token")) {
                            SkApplication.saveOrUpdateSiteInfo(data, getApp());
                            successSignInAction();
                        }


                    } else {
                        String result = data.getString("data");
                        if (result != null) {
                            resultData = new Gson().fromJson(result, JsonObject.class);
                            if( SkApi.propExistsAndNotNull(resultData, "data") ){
                                resultData = resultData.getAsJsonObject("data");
                            if (resultData.has("exception")) {
                                if (SkApi.propExistsAndNotNull(resultData, "userData")) {
                                    JsonObject temp = resultData.getAsJsonObject("userData");

                                    if (SkApi.propExistsAndNotNull(temp, "message")) {
                                        Toast.makeText(AuthActivity.this, temp.get("message").getAsString(), Toast.LENGTH_LONG).show();
                                    }
                                }
                                }
                            }
                        }
                    }

                    blockButton = false;
                    progress.setVisibility(View.GONE);
                }
            });
        }
    }

    private void successSignInAction() {
        SkUser user = SkApplication.getUserInfo();
        SkSite site = SkApplication.getSiteInfo();

        StatusActivity.STATUS_LIST specStatus = null;

        // clear search and edit form data
        QuestionService.getInstance().removeSharedPreferences(getApplicationContext(), EditFormFragment.QUESTION_PREFIX);
        QuestionService.getInstance().removeSharedPreferences(getApplicationContext(), SearchFormFragment.QUESTION_PREFIX);


        // check status
        if( site.getMaintenance() ){
            specStatus = StatusActivity.STATUS_LIST.MAINTENANCE;
        }
        else if (user.getIsSuspended()) {
            specStatus = StatusActivity.STATUS_LIST.SUSPENDED;
        }
        else if (!user.getIsEmailVerified() && site.getConfirmEmail()) {
            specStatus = StatusActivity.STATUS_LIST.NOT_VERIFIED;
        }
        else if (!user.getIsApproved() && site.getUserApprove()) {
            specStatus = StatusActivity.STATUS_LIST.NOT_APPROVED;
        }

        if (specStatus != null) {
            Intent in = new Intent(AuthActivity.this, StatusActivity.class);
            in.putExtra("type", specStatus);
            startActivity(in);
        }
        else{
            Intent in = new Intent(AuthActivity.this, MainFragmentActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            in.putExtra("showHotList", true);
            startActivity(in);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }

    @Override
    public void onResume() {
        super.onResume();

        Session session = Session.getActiveSession();

        if (session != null && (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        uiHelper.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case 500:
                Session session = Session.getActiveSession();

                if (session != null && !session.isClosed()) {
                    session.closeAndClearTokenInformation();
                }

                show();
                break;
            case 200:
//                String jsonString = data.getStringExtra("userData");
//
//                JsonParser jsonParser = new JsonParser();
//                JsonObject json = jsonParser.parse(jsonString).getAsJsonObject().getAsJsonObject("data");
                SkApplication.saveOrUpdateSiteInfo(data.getBundleExtra("userData"), getApp());
                successSignInAction();
                show();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        uiHelper.onSaveInstanceState(outState);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            //
        } else if (state.isClosed()) {
            //
        }
    }

    private void hide() {
        findViewById(R.id.auth_progress_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.auth_container).setVisibility(View.INVISIBLE);
    }

    private void show() {
        findViewById(R.id.auth_container).setVisibility(View.VISIBLE);
        findViewById(R.id.auth_progress_bar).setVisibility(View.INVISIBLE);
    }

    private void startFBConnect(GraphUser graphUser) {
        Intent intent = new Intent(AuthActivity.this, FacebookLoginStepManager.class);

        intent.putExtra("facebookId", graphUser.getId())
                .putExtra("name", graphUser.getName())
                .putExtra("gender", graphUser.asMap().get("gender").toString())
                .putExtra("birthday", graphUser.getBirthday());

        if (graphUser.asMap().containsKey("email")) {
            intent.putExtra("email", graphUser.asMap().get("email").toString());
        }

        startActivityForResult(intent, 200);
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(final Session session, SessionState state, Exception exception) {
            if (state.isOpened()) {
                Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(final GraphUser graphUser, Response response) {
                        if (graphUser != null) {
                            FacebookConnectService service = new FacebookConnectService(AuthActivity.this.getApp());
                            service.tryLogin(graphUser.getId(), new SkServiceCallbackListener() {
                                @Override
                                public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                                    JsonObject json = SkApi.processResult(data);

                                    if (json != null && json.has("loggedIn") && json.get("loggedIn").getAsBoolean() == true) {
                                        SkApplication.saveOrUpdateSiteInfo(data, getApp());
                                        successSignInAction();
                                        show();
                                    } else {
                                        startFBConnect(graphUser);
                                    }

                                    if (session != null && !session.isClosed()) {
                                        session.closeAndClearTokenInformation();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(AuthActivity.this, "Something is wrong", Toast.LENGTH_LONG).show();
                        }
                    }
                }).executeAsync();
            } else if (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException) {
                show();
            }
        }
    }
}
