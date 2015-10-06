package com.mydatingapp.ui.auth;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mydatingapp.R;
import com.mydatingapp.core.SkApplication;
import com.mydatingapp.utils.SkApi;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by sardar on 6/11/14.
 */
public class UrlActivity extends BaseAuthActivity {

    String urlVal;
    TextView text;
    ImageButton button;
    ProgressBar progress;
    boolean imgButtonState = false, blockSubmit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.auth_url_activity);
        super.onCreate(savedInstanceState, false);
        text = (TextView) findViewById(R.id.text);
        button = (ImageButton) findViewById(R.id.button);
        progress = (ProgressBar)findViewById(R.id.progressBar);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if( blockSubmit ){
                    return;
                }

                if( imgButtonState ){
                    hideText();
                }
                else{
                    showText();
                }
            }
        });
        final EditText url = (EditText)findViewById(R.id.urlField);
        url.setHint(getResources().getString(R.string.url_page_input_invitation));
        url.requestFocus();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        url.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if( blockSubmit ){
                        return true;
                    }
                    // Perform action on key press
                    urlVal = url.getText().toString();

                    if( !Patterns.WEB_URL.matcher(urlVal).matches() ){
                        Toast.makeText(UrlActivity.this, getString(R.string.invalid_url_error_msg), Toast.LENGTH_LONG).show();
                        return true;
                    }

                    new ApiCheckAsyncTask().execute();

                    return true;
                }
                return false;
            }
        });

        String urlVal = getApp().getSiteUrl();
        if( urlVal != null ){
            url.setText(urlVal);
        }
    }

    private void showText(){
        text.setVisibility(View.VISIBLE);
        button.setBackgroundResource(R.drawable.speedmatches_info_on);
        imgButtonState = true;
    }

    private void hideText(){
        text.setVisibility(View.GONE);
        button.setBackgroundResource(R.drawable.speedmatches_info);
        imgButtonState = false;
    }

    class ApiCheckAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideText();
            blockSubmit = true;
            progress.setVisibility(View.VISIBLE);

        }

        @Override
        protected Boolean doInBackground(String... urls) {
            if (!urlVal.substring(urlVal.length() - 1).equals("/")) {
                urlVal += "/";
            }

            try {
                HttpParams params = new BasicHttpParams();
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, "utf-8");
                params.setBooleanParameter("http.protocol.expect-continue", false);
                HttpProtocolParams.setUserAgent(params, "ANDROID");
                params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 50000);
                params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 50000);

                HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

                SchemeRegistry registry = new SchemeRegistry();
                org.apache.http.conn.ssl.SSLSocketFactory socketFactory = org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory();
                socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
                registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", socketFactory, 443));
                SingleClientConnManager mgr = new SingleClientConnManager(params, registry);
                DefaultHttpClient httpClient = new DefaultHttpClient(mgr, params);

                HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
                HttpGet httppost = new HttpGet(urlVal+"api/android/base/check-api");

                HttpResponse response = httpClient.execute(httppost);

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);

                    if( data != null ){
                        JsonObject result = new Gson().fromJson(data, JsonObject.class);

                        if(SkApi.propExistsAndNotNull(result, "type") && result.get("type").getAsString().equals("success")){
                            return true;
                        }
                    }
                }

            } catch (Exception e) {
                return false;
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if( result ){
                SkApplication.setUrl(urlVal);
                startActivity(new Intent(UrlActivity.this, AuthActivity.class));
            }
            else{
                Toast.makeText(UrlActivity.this, UrlActivity.this.getString(R.string.invalid_site_error_msg), Toast.LENGTH_LONG).show();
            }

            blockSubmit = false;
            progress.setVisibility(View.GONE);
        }
    }
}
