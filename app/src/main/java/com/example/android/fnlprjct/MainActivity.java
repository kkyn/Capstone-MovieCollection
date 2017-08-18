package com.example.android.fnlprjct;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.android.fnlprjct.sync.MSyncAdapter;

public class MainActivity extends AppCompatActivity implements MainFragment.CallBackListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public boolean is2Pane = false;

    ////////////////////////////////////
    @Override   //--- 1
    protected void onStart() {
        super.onStart();
    }
    @Override   //--- 2
    protected void onPause() {
        super.onPause();
    }
    @Override   //--- 3
    protected void onStop() {
        super.onStop();
    }
    @Override   //--- 4
    protected void onRestart() {
        super.onRestart();
    }
    @Override   //--- 5
    protected void onDestroy() {
        super.onDestroy();
    }
    ////////////////////////////////////


    @Override   //--- 2
    protected void onResume() {
        super.onResume();

        FragmentManager fMngr = getSupportFragmentManager();

        MainFragment mainFragment = (MainFragment) fMngr.findFragmentById(R.id.mainfragment_container);

        if ( mainFragment != null ) {
            mainFragment.myRestartLoaderCode();
        }
    }


    @Override    //--- 0
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
        }

        setContentView(com.example.android.fnlprjct.R.layout.activity_main);

        // The view with 'id' only exist within layout 'activity_main.xml' file,
        // if the device is a tablet.
        if (findViewById(R.id.fragmentdetail_container) != null) {
            is2Pane = true;
        }

        MSyncAdapter.initializeSyncAdapter(this);

    }

    //------------------------------------------------
    //--------- Begin, OPTIONS MENU Stuff ------------
    //------------------------------------------------
    @Override public boolean
    onCreateOptionsMenu(Menu menu) {
        // Inflate the 'menu';
        // This add items in 'R.menu.menu_main_activtiy' to the action bar, 'menu' if it is present.
        // @1st param -- Resource ID for an XML layout resource to load (e.g., R.menu.main_activity)
        // @2nd param -- menu, The Menu to inflate into. The items and submenus will be added to this Menu
        this.getMenuInflater().inflate(R.menu.menu_main_activtiy, menu);

        return true;
    }

    @Override public boolean
    onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsPreferenceActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //------------------------------------------------
    //--------- End, OPTIONS MENU Stuff --------------
    //------------------------------------------------

    // -------- Implementing Callback methods called from Fragment ----
    // -------- Declared in Fragment associate with this Activity -----
    //-----------------------------------------------------------------
    @Override
    public void onItemSelectedInRecyclerView(Intent intent, Bundle bundle) {

        startActivity(intent, bundle);
    }

}
