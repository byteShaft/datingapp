package com.mydatingapp.ui.search;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

import com.mydatingapp.R;
import com.mydatingapp.ui.base.SkBaseInnerActivity;
import com.mydatingapp.ui.search.fragments.EditFormFragment;

/**
 * Created by jk on 1/15/15.
 */
public class EditProfileActivity extends SkBaseInnerActivity {
    EditFormFragment fragment;

    public EditProfileActivity() {
        //super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState,R.layout.activity_search_filter);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        fragment = new EditFormFragment();

        fragmentTransaction.replace(R.id.filter, fragment);
        fragmentTransaction.commit();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setEmulateBackButton(true);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home || id == android.R.id.home || id == R.id.homeAsUp) {
            this.finish();
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
}
