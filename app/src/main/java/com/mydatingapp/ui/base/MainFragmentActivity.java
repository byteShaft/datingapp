package com.mydatingapp.ui.base;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.JsonObject;
import com.mydatingapp.R;
import com.mydatingapp.core.SkApplication;
import com.mydatingapp.core.SkServiceCallbackListener;
import com.mydatingapp.model.base.BaseRestCommand;
import com.mydatingapp.model.base.BaseServiceHelper;
import com.mydatingapp.model.base.classes.SkMenuItem;
import com.mydatingapp.ui.base.main_activity_fragments.AboutFragment;
import com.mydatingapp.ui.base.main_activity_fragments.TermsFragment;
import com.mydatingapp.ui.hot_list.HotListView;
import com.mydatingapp.ui.mailbox.conversation_list.MailboxFragment;
import com.mydatingapp.ui.matches.MatchesList;
import com.mydatingapp.ui.search.fragments.SearchResultList;
import com.mydatingapp.ui.speedmatch.SpeedmatchesView;
import com.mydatingapp.ui.user_list.fragments.BookmarkListFragment;
import com.mydatingapp.ui.user_list.fragments.GuestListFragment;
import com.mydatingapp.utils.SkApi;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class MainFragmentActivity extends SkBaseInnerActivity {

    private HashMap<SkMenuItem.ITEM_KEY, Class<? extends Fragment>> fragments = new HashMap<>();

    public MainFragmentActivity() {
        super();
        fragments.put(SkMenuItem.ITEM_KEY.SEARCH, SearchResultList.class);
        fragments.put(SkMenuItem.ITEM_KEY.GUESTS, GuestListFragment.class);
        fragments.put(SkMenuItem.ITEM_KEY.BOOKMARKS, BookmarkListFragment.class);
        fragments.put(SkMenuItem.ITEM_KEY.ABOUT, AboutFragment.class);
        fragments.put(SkMenuItem.ITEM_KEY.MATCHES, MatchesList.class);
        fragments.put(SkMenuItem.ITEM_KEY.SPEED_MATCH, SpeedmatchesView.class);
        fragments.put(SkMenuItem.ITEM_KEY.TERMS, TermsFragment.class);
        fragments.put(SkMenuItem.ITEM_KEY.MAILBOX, MailboxFragment.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main_fragment);
        boolean fragmentDetected = false;
        String key = getIntent().getStringExtra("key");
        if( key != null ){
            SkMenuItem.ITEM_KEY eKey = SkMenuItem.ITEM_KEY.valueOf(key);
            if( eKey != null ){
                fragmentDetected = true;
                selectItem(eKey);
            }
        }

        if( !fragmentDetected ){
            selectItem(1);
        }

        Intent intent = getIntent();

        if (intent.hasExtra("showHotList")) {
            BaseServiceHelper.getInstance(SkApplication.getApplication()).runRestRequest(BaseRestCommand.ACTION_TYPE.HOTLIST_COUNT, new SkServiceCallbackListener() {
                @Override
                public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                    JsonObject jsonObject = SkApi.processResult(data);

                    if (jsonObject != null && jsonObject.has("count") && jsonObject.get("count").isJsonPrimitive() && jsonObject.getAsJsonPrimitive("count").getAsInt() > 0) {
                        startActivity(new Intent(MainFragmentActivity.this, HotListView.class));
                        overridePendingTransition(R.anim.hotlist_top_slide_in, R.anim.hotlist_top_slide_out);
                    }
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void selectItem( SkMenuItem.ITEM_KEY key ){
        int position = mAdapter.getPositionByKey(key);

        if( position > 0 ){
            selectItem(position);
        }
    }

    @Override
    protected void selectItem(int position) {

        SkMenuItem item = mAdapter.getItem(position);

        if (item == null) {
            return;
        }

        if( checkCustomItems(item) )
        {
            return;
        }


        SkMenuItem.ITEM_KEY key = SkMenuItem.ITEM_KEY.fromString(item.getKey());

        //getting fragment
        Class fragmentClass = fragments.get(key);

        if( fragmentClass == null ){
            return;
        }

        try {
            fragmentClass.newInstance();
        } catch (InstantiationException e) {
            return;
        } catch (IllegalAccessException e) {
            return;
        }


        // update the main content by replacing fragments
        Fragment fragment = null;
        try {
            Constructor<?> ctor = fragmentClass.getConstructor();
            fragment = (Fragment) ctor.newInstance();
        } catch (Exception e) {
            return;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }

        if( mDrawerLayout.isDrawerOpen(GravityCompat.START) ) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }
}
