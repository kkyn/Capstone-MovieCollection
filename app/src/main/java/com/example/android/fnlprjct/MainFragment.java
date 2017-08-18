package com.example.android.fnlprjct;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.fnlprjct.adapter.MainRcyclrVwAdapter;
import com.example.android.fnlprjct.data.MovieContract;
import com.example.android.fnlprjct.data.MovieContract.MovieInfoEntry;
import com.example.android.fnlprjct.sync.MSyncAdapter;
import com.example.android.fnlprjct.ui.ChangeYearDialogFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment (MainFragment) containing a simple view.
 */
public class MainFragment extends Fragment
                            implements LoaderManager.LoaderCallbacks<Cursor>
                            , SharedPreferences.OnSharedPreferenceChangeListener
                            , SwipeRefreshLayout.OnRefreshListener
{
    // constructor
    public MainFragment() {
        setHasOptionsMenu(true);
    }

    public static final String LOG_TAG = MainFragment.class.getSimpleName();

    private MainRcyclrVwAdapter rvAdapter;
    private int mPosition = RecyclerView.NO_POSITION;
    private long itemId = 0;
    private Uri uri;

    // (step1) Declare variables for Firebase-Ads, Firebase-Analytics
    AdRequest adRequest;
    FirebaseAnalytics mFirebaseAnalytics;

    private static final int MOVIE_FRAGMENT_ID = 0; // constant definition

    public static final int COLUMN_POSTERLINK;
    public static final int COLUMN_MOVIE_TITLE;

    static {
        COLUMN_POSTERLINK = 3;
        COLUMN_MOVIE_TITLE = 4;
    }
    public static final int DIALOG_REQUEST_CODE = 1;
    public static final String DIALOG = "changeyear";
    public static final String DIALOG_KEY = "changeyear_key";

    // https://developer.android.com/reference/android/app/Fragment.html
    // Start-A-Fragment-In-A-Fragment .... Start Dialog in Fragment
    // Step 1 : ( Instantiate a 'source'-Fragment,
    //              reference 'this' fragment as the 'return'/'target'-Fragment  )
    private void showChangeYearDialog(){

        FragmentManager fm = getFragmentManager();

        ChangeYearDialogFragment
            chngyrDialog = ChangeYearDialogFragment.newInstance();
            chngyrDialog.setTargetFragment(this, DIALOG_REQUEST_CODE); // 1 : say is Constants.DIALOG_REQUEST_CODE
            chngyrDialog.show(fm, DIALOG);

    }
    // Start-A-Fragment-In-A-Fragment .... Start Dialog in Fragment
    // Step 3 : ( Retrieve data from 'source'-fragment )
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DIALOG_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getExtras().containsKey(DIALOG_KEY)) {

                    actionBar.setSubtitle(updateActionBarTitle());

                    onRefresh();
                }
            }
        }
    }

    private static final String SELECTED_INDEX = "selected_index";
    ActionBar actionBar;

    @BindView(R.id.toolbar) Toolbar tool_bar;
    @BindView(R.id.edit_fab) FloatingActionButton editfab;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.main_recyclerview) RecyclerView mainRyclrVw;
    @BindView(R.id.adView) AdView bannerView; // ... need instance of 'google-service.json' to work

    //-------------------------
    //--- Interface stuff -----
    //-------------------------
    CallBackListener mainCallBackListener;

    // Container Activity must implement this interface
    public interface CallBackListener {

        void onItemSelectedInRecyclerView(Intent intent, Bundle bundle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mainCallBackListener = (CallBackListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CallBackListener");
        }
    }

    //---------------------------------
    //--- Fragment Lifecycle Stuff ----
    //---------------------------------

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override // --- 6 ----
    public void onStop() {
        super.onStop(); //Log.d(LOG_TAG, "---- 6 onStop() --");
    }

    @Override // --- 7 ----
    public void onDestroyView() {
        super.onDestroyView();  //Log.d(LOG_TAG, "---- 7 onDestroyView() --");
    }

    /** Called before the fragment is destroyed */
    @Override
    public void onDestroy() {
        if (bannerView != null) {
            bannerView.destroy();
        }
        super.onDestroy();
    }

    @Override // --- 0 ----
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add this line in order for this fragment to handle menu events.
        // Report that this fragment would like to participate in populating the
        // options menu by receiving a call to onCreateOptionsMenu(Menu, MenuInflater)
        // and related methods.
        setHasOptionsMenu(true);

        // Firebase-Analytics ---- (step2) Obtain a FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

    }

    // onViewCreated() is called immediately after onCreateView() method.
    @Override // --- 1b ----
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);  //Log.d(LOG_TAG, "---- 1a onViewCreated(View view, @Nullable Bundle savedInstanceState) --");

        if (tool_bar != null ) {

            // Sets the Toolbar to act as the ActionBar for this Activity window.
            // Make sure the tool_bar exists in the activity and is not null
            ((AppCompatActivity) getActivity()).setSupportActionBar(tool_bar); // <!-- to set the Toolbar to act as the ActionBar  -->

            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.label_main_activity));
                actionBar.setSubtitle(updateActionBarTitle());
            }

        }
    }

    @Override // --- 2 ----
    public void onActivityCreated(Bundle savedInstanceState) {

        // returns a loader-object, there is no need to keep it around.
        // LoaderManager will take care of the details.
        // Loaders are uniquely identified, e.g. POPULAR_LOADER_ID
        // Prepare the loader.
        // Either re-connect with an existing one or start a new one.
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        getLoaderManager().initLoader(MOVIE_FRAGMENT_ID, null, this);
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        super.onActivityCreated(savedInstanceState);

    }

    @Override   // --- 3 ---
    public void onStart() {
        super.onStart();
    }

    /** Called when returning to the fragment */
    @Override //---- 4 ----
    public void onResume() {

        SharedPreferences
            sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            // Registers a callback to be invoked when a change happens to a preference.
            sp.registerOnSharedPreferenceChangeListener(this);

        super.onResume();

        // This update the title & subtitle, after from PerferenceActivity call
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(getString(R.string.label_main_activity));
            actionBar.setSubtitle(updateActionBarTitle());
        }

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        getLoaderManager().restartLoader(MOVIE_FRAGMENT_ID, null, this); //  help maintain position ??
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        // Resume the AdView.
        if (bannerView != null) {
            bannerView.resume();
        }
    }

    /** Called when leaving the fragment */
    @Override //--- 5 ----
    public void onPause() {

        SharedPreferences
            sp = PreferenceManager.getDefaultSharedPreferences(getContext()/*getActivity()*/);
            // Unregisters a previous callback
            sp.unregisterOnSharedPreferenceChangeListener(this);

        // Pause the AdView.
        if (bannerView != null) {
            bannerView.pause();
        }
        super.onPause();
    }

    void myRestartLoaderCode() {

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        getLoaderManager().restartLoader(MOVIE_FRAGMENT_ID, null, this);
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    }

    //---------------------------------------------------------------------------------------------
    //------ SETTING FOR implementation of SharedPreferences.OnSharedPreferenceChangeListener -----
    //---------------------------------------------------------------------------------------------
    // For SharedPreferences.OnSharedPreferenceChangeListener
    // Called when a shared preference is changed, added, or removed.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.pref_key_movies_sortby))) {

            getLoaderManager().restartLoader(MOVIE_FRAGMENT_ID, null, this);


            MSyncAdapter.syncImmediately(getActivity());
            //MSyncAdapter.syncImmediately(getContext());

            Intent intent = new Intent();
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

            getActivity().sendBroadcast(intent);
        }

    }

    //--------------------------------------------------------------
    //-- LoaderManager.LoaderCallbacks<Cursor> --
    //----------- (begin) LoaderCursor Stuff -----------------------
    //--------------------------------------------------------------
    /*
    * Callback that's invoked when the system has initialized the Loader and
    * is ready to start the query. This usually happens when initLoader() is
    * called. The loaderID argument contains the ID value passed to the
    * initLoader() call.
    */
    // int id --- refers to MOVIE_LOADER
    @Override   // --- 1 ---
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortMoviesBy = Utility.getPreferredSortSequence(getContext());
        String searchYear = Utility.getPreferredYear(getContext());

        Uri uri;
        String[] projection;
        String selection;
        String[] selectionArg;
        String sortOrder;

        uri = MovieInfoEntry.CONTENT_URI;

        if (sortMoviesBy.equals(getString(R.string.pref_value_movies_sortby_default))) {

            projection = MyQuery.Popularity.PROJECTION;
            selection = MovieInfoEntry.COL_YEAR + "=?";                                       //
            selectionArg = new String[]{searchYear};
            sortOrder = MovieInfoEntry.COL_VOTE_COUNT + " DESC";

        } else if (sortMoviesBy.equals(getString(R.string.pref_value_movies_sortby_ratings))) {

            projection = MyQuery.VoteAverage.PROJECTION;
            selection = MovieInfoEntry.COL_YEAR + "=?";
            selectionArg = new String[]{searchYear};
            sortOrder = MovieInfoEntry.COL_VOTE_AVERAGE + " DESC";

        } else {  // sortMoviesBy.equals(getString(R.string.pref_movies_sortby_favourites))

            projection = MyQuery.Favourites.PROJECTION;
            selection = MovieInfoEntry.COL_FAVOURITES + "=?";
            selectionArg = new String[]{"1"};
            sortOrder = MovieInfoEntry.COL_VOTE_AVERAGE + " DESC";
        }

        // https://www.w3schools.com/sql/
        // https://developer.android.com/reference/android/content/ContentResolver.html
        // #query(android.net.Uri,%20java.lang.String[],%20java.lang.String,%20java.lang.String[],%20java.lang.String)
        // https://developer.android.com/reference/android/content/CursorLoader.html
        // CursorLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
        //
        return new CursorLoader(
            getActivity(),           // context
            uri,                     // uri
            projection,              // projection
            selection,               // selection
            selectionArg,            // selectionArg
            sortOrder                // sortOrder
        );
    }

    // Called when a previously created loader has finished its load.
    /*
    * Defines the callback that CursorLoader calls
    * when it finishes its query
    */
    @Override //--- 5 ---
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // import a new set of cursor structured/found in 'Cursor' into the RecyclerViewAdapter.
        /*
         * Moves the query results into the adapter, causing the
         * RecyclerView fronting this adapter to re-display
         */
        rvAdapter.swapCursor(cursor); // notifyDataSetChanged() is called in swapCursor()

        /*if (mPosition != RecyclerView.NO_POSITION) {
            mainRyclrVw.smoothScrollToPosition(mPosition);
        }*/
    }

    // Called when a previously created loader is being reset, and thus making its data unavailable.
    /*
    * Invoked when the CursorLoader is being reset. For example, this is
    * called if the data in the provider changes and the Cursor becomes stale.
    */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Clears out the adapter's reference to the Cursor.
        // This prevents memory leaks.
        rvAdapter.swapCursor(null);
    }
    //--------------------------------------------------------------
    //----------- (End) LoaderCursor Stuff -------------------------
    //--------------------------------------------------------------

    //-----------------------------------------------------------
    //----------- (Begin) OPTIONS MENU Stuff---------------------
    // ----------------------------------------------------------

    // Initialize the contents of the Fragment host's standard options menu.
    @Override public void
    onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_main_fragment, menu);
    }

    // This hook is called whenever an item in your options menu is selected.
    // Return false to allow normal menu processing to proceed, true to consume it here.
    @Override public boolean
    onOptionsItemSelected(MenuItem item) {

        // get the file, SharedPreferences
        // Gets a SharedPreferences instance that points to the default file
        // that is used by the preference framework in the given context.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Create a new Editor for these preferences, through which you can make modifications to
        // the data in the preferences and atomically commit those changes back to the SharedPreferences object.
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int id = item.getItemId();

        if (id == R.id.most_popular) {
            // Set a String value in the preferences editor, to be written back once commit() or apply() are called.
            editor.putString(getString(R.string.pref_key_movies_sortby), getString(R.string.pref_value_movies_sortby_default));

            // Commit your preferences changes back from this Editor to the SharedPreferences object it is editing.
            editor.apply(); // editor.commit();

            actionBar.setSubtitle(updateActionBarTitle());
            return true;
        }
        else if (id == R.id.most_rated) {
            editor.putString(getString(R.string.pref_key_movies_sortby), getString(R.string.pref_value_movies_sortby_ratings));
            editor.apply();

            actionBar.setSubtitle(updateActionBarTitle());
            return true;
        }
        else if (id == R.id.my_favorites) {
            editor.putString(getString(R.string.pref_key_movies_sortby), getString(R.string.pref_value_movies_sortby_ownfavorites));
            editor.apply();

            actionBar.setSubtitle(updateActionBarTitle());
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private String updateActionBarTitle() {

        // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
        String year = Utility.getPreferredYear(getContext());
        String sortMoviesBy = Utility.getPreferredSortSequence(getContext());
        String category;
        int option;
        // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
        if (sortMoviesBy.equals(getString(R.string.pref_value_movies_sortby_default))) {

            category = getString(R.string.most_popular);
            option = 1;
        } else if (sortMoviesBy.equals(getString(R.string.pref_value_movies_sortby_ratings))) {

            category = getString(R.string.highest_rated);
            option = 1;
        } else {  // sortMoviesBy.equals(getString(R.string.pref_movies_sortby_favourites))

            category = getString(R.string.my_favorites);
            option = 2;
        }
        // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

        String title;
        if (option == 1) {
            title = year + " : " + category;
        } else {
            title = category;
        }
        return title;
    }
    //----------------------------------------------------------
    //--------------- (End) OPTIONS MENU Stuff -----------------
    //----------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_INDEX, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    /* When the system is ready for the Fragment to appear, this displays
     * the Fragment's View
     */
    @Override //--- 1a ---
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //GridLayoutManager gridlm;
        //RecyclerView.LayoutManager layoutManager;

        StaggeredGridLayoutManager stgrdgrdlm;

        //************************************************************************************************
        //*******Begin, Instantiate a Listener for MainRcyclrVwAdapter.ItemClickListener ****************
        //************************************************************************************************
        //
        // Implementation the interface, 'NAME'/ItemClickListener
        // with the method-name/onClick0 found within the interface declaration.
        MainRcyclrVwAdapter.ItemClickListener listener =
                        new MainRcyclrVwAdapter.ItemClickListener() {

                @Override
                public void onClick0(MainRcyclrVwAdapter.MainRcyclrVwViewHolder viewHolder) {

                    mPosition = viewHolder.getAdapterPosition();

                    itemId = rvAdapter.getItemId(mPosition);

                    String srcViewSharedElementTransition = getString(R.string.shared_name) + itemId;

                    viewHolder.poster_networkimageview.setTransitionName(srcViewSharedElementTransition);

                    //-----------------------------------------
                    // Firebase-Analytics ---- (step3) generate FirebaseAnalytics.logEvent
                    // Begin, movie-selection event
                    //  recordMovieImage(viewHolder);
                    String movieName = rvAdapter.getItemName(mPosition);
                    Bundle mbundle = new Bundle();
                    mbundle.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(itemId));
                    mbundle.putString(FirebaseAnalytics.Param.ITEM_NAME, movieName );
                    mbundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "selection");

                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, mbundle);
                    // End, movie-selection event
                    //-----------------------------------------

                    //-----------------------------------------
                    ImageView posterImageview = viewHolder.poster_networkimageview;
                    //DynamicHeightNetworkImageView posterImageview = viewHolder.poster_networkimageview;

                    final Pair<View, String> pair1 = Pair.create((View) posterImageview, viewHolder.poster_networkimageview.getTransitionName());
                    //final Pair<View, String> pair1 = new Pair<>((View)posterImageview, viewHolder.poster_networkimageview.getTransitionName());

                    // http://guides.codepath.com/android/shared-element-activity-transition
                    ActivityOptionsCompat option = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), pair1);

                    Bundle bundle = option.toBundle();

                    uri = MovieContract.MovieInfoEntry.CONTENT_URI;
                    uri = ContentUris.withAppendedId(uri, itemId);

                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.setData(uri);
                    //-----------------------------------------

                    mainCallBackListener.onItemSelectedInRecyclerView(intent, bundle);

                }
            };
        //************************************************************************************************
        //*******End  , Instantiate a Listener for MainRcyclrVwAdapter.ItemClickListener ****************
        //************************************************************************************************

        rvAdapter = new MainRcyclrVwAdapter(getContext(), listener);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, rootView);

        //gridlm = new GridLayoutManager(getContext(), 2);
        //mainRyclrVw.setLayoutManager(gridlm);
        // --or--
        stgrdgrdlm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        mainRyclrVw.setLayoutManager(stgrdgrdlm);
        mainRyclrVw.setAdapter(rvAdapter);
        mainRyclrVw.setHasFixedSize(true);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(SELECTED_INDEX)) {
            mPosition = savedInstanceState.getInt(SELECTED_INDEX);
        }

        // +++++++++++++++++++++++++++++++++++++++++++++
        editfab.setContentDescription(getString(R.string.descriptor_changeyear));
        editfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showChangeYearDialog();
            }
        });

        // +++++++++++++++++++++++++++++++++++++++++++++
        // 'connect'/'bind' instance in xml with implementation
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        // +++++++++++++++++++++++++++++++++++++++++++++
        //--- Initialize the Mobile Ads SDK -----
        MobileAds.initialize(getActivity(), getString(R.string.my_admob_ap_id));

        //--- Begin, AdMob's:  AdRequest.Builder,  BannerAd stuff ------
        adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .build();
        bannerView.loadAd(adRequest);
        //--- End,  AdMob's:  AdRequest.Builder,  BannerAd stuff ------

        return rootView;
    }


    // ------------------------------------------------------
    // ---- Begin -------------------------------------------
    //  DEFINITION/IMPLEMENTATION for -----------------------
    //      interface SwipeRefreshLayout.OnRefreshListener --
    // ------------------------------------------------------
    @Override
    public void onRefresh() {

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        getLoaderManager().restartLoader(MOVIE_FRAGMENT_ID, null, this);
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // enables progress visibility
        swipeRefreshLayout.setRefreshing(true);

        if (!networkUp()){


            Toast toast = Toast.makeText(getActivity(), getString(R.string.error_no_network), Toast.LENGTH_LONG); //.show();
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // disables progress visibility
                    swipeRefreshLayout.setRefreshing(false);

                }
            }, 4000);
        }
        else {

            swipeRefreshLayout.setRefreshing(false);

            //---------------------------------------------------
            Intent intent = new Intent();
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

            getActivity().sendBroadcast(intent);
            //---------------------------------------------------
        }
    }

    // Check for network connectivity
    //
    private boolean networkUp() {

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()){
            return true;
        }
        else return false;
    }
    // ------------------------------------------------------
    // ---- End ---------------------------------------------
    // ------------------------------------------------------


}