package com.example.android.myproject_2;

//import android.app.LoaderManager;
//import android.content.CursorLoader;
//import android.content.Loader;
//import android.support.v4.app.Fragment;
/**/
/* */

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.myproject_2.data.MovieContract.PopularEntry;
import com.example.android.myproject_2.data.MovieContract.RatingEntry;
import com.example.android.myproject_2.sync.MoviesSyncAdapter;

/**
 * A placeholder fragment (Main_Fragment) containing a simple view.
 */
public class Main_Fragment extends Fragment
                        implements LoaderManager.LoaderCallbacks<Cursor>
                                , SharedPreferences.OnSharedPreferenceChangeListener
                             //   ,Movie_RecyclerViewAdapter.MvItemOnClickHandler
{
    // constructor
    public Main_Fragment() {

    }

    public static final String LOG_TAG = Main_Fragment.class.getSimpleName();
    SharedPreferences.OnSharedPreferenceChangeListener listener;
/**/
private RecyclerView                mRecyclerView;
private Movie_RecyclerViewAdapter mvRcyclrVwAdapterVwHldr;
    private int mPosition = RecyclerView.NO_POSITION;
    private long nwPosition = 0;
/**/

//    private Movie_CursorAdapter         moviesCursorAdapter;
//     GridView mGridView;
//    private GridView mGridView;
//    private String sortMoviesBy;
//    private SharedPreferences mShrdPrfrncs =  null;

//    final String POPULARITY_DESC = "popularity.desc";

//    static final String VOTE_AVERAGE_DESC = "vote_average.desc"; // "vote_count.desc"; "vote_average.desc";
//    private final String VOTE_AVERAGE_DESC = "vote_average.desc"; // "vote_count.desc"; "vote_average.desc";
//    private final String POPULARITY_DESC = "popularity.desc";
/*
    private static final int POPULAR_LOADER_ID = 1;
    private static final int RATING_LOADER_ID = 2;

    static final int COLUMN_POPULAR_ID = 0;
    static final int COLUMN_MV_ID = 1;
    static final int COLUMN_KEY_ID = 2;
    static final int COLUMN_POSTERLINK = 3;
    */
    //-----------  new  ---------------------
            static final String VOTE_AVERAGE_DESC;
            static final String POPULARITY_DESC;
    private
    //public
    static final int MOVIE_FRAGMENT_ID = 0;
    static final int POPULAR_LOADER_ID = 1; // constant definition
    static final int RATING_LOADER_ID = 2;
    //private static final int RATING_LOADER_ID = 2;

    static {
     //   POPULAR_LOADER_ID = 1;
     //   RATING_LOADER_ID = 2;
        POPULARITY_DESC = "popularity.desc";
        VOTE_AVERAGE_DESC = "vote_average.desc"; // "vote_count.desc"; "vote_average.desc";
    }

    static final int COLUMN_POPULAR_ID;
    static final int COLUMN_MV_ID;
    static final int COLUMN_KEY_ID;
    static final int COLUMN_POSTERLINK;
    static{
        COLUMN_POPULAR_ID = 0;
        COLUMN_MV_ID = 1;
        COLUMN_KEY_ID = 2;
        COLUMN_POSTERLINK = 3;
    }

    // state the columns of data I want
    private static final String[] POPULAR_PROJECTION = new String[]{

        PopularEntry.TABLE_NAME + "." + PopularEntry._ID
        ,PopularEntry.COL_MV_ID
        ,PopularEntry.COL_KEY_ID
        ,PopularEntry.COL_POSTER
    };
    private static final String[] RATING_PROJECTION = new String[]{

        RatingEntry.TABLE_NAME + "." + RatingEntry._ID
        ,RatingEntry.COL_MV_ID
        ,RatingEntry.COL_KEY_ID
        ,RatingEntry.COL_POSTER
    };

    private static final String SELECTED_INDEX = "selected_index";
    //-------------------------
    //--- Interface stuff -----
    //-------------------------
    CallBackListner mCallBackListner;

    // Container Activity must implement this interface
    public interface CallBackListner {

         void onItemSelectedInRecyclerView(Uri myUri);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallBackListner = (CallBackListner) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CallBackListner");
        }
    }

    //////////////////////////////////////////////////////////////
    // tky, to stay
    //--- Fragment Lifecycle Stuff ----
    //---------------------------------


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "---- 6 onStop() --");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(LOG_TAG, "---- 7 onDestroyView() --");
    }


    @Override // --- 1a ----
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(LOG_TAG, "---- 1a onViewCreated(View view, @Nullable Bundle savedInstanceState) --");
    }

    @Override // --- 0 ----
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

        Log.d(LOG_TAG, "---- 0 onCreate() --");
    }
    @Override // --- 2 ----
    public void onActivityCreated(Bundle savedInstanceState) {

        Log.d(LOG_TAG, "---- 2 onActivityCreated() --");
        // returns a loader-object, there is no need to keep it around.
        // LoaderManager will take of the details.
        // Loaders are uniquely identified, e.g. POPULAR_LOADER_ID
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        Log.d(LOG_TAG, "---- 2 onActivityCreated() / initLoader --");
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        getLoaderManager().initLoader(MOVIE_FRAGMENT_ID, null, this);
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        super.onActivityCreated(savedInstanceState);

    }

    @Override   // --- 3 ---
    public void onStart() {
        super.onStart();

        Log.d(LOG_TAG, "---- 3 onStart() --");

        String sortMoviesBy = Utility.getPreferredSortSequence(getContext());

        Log.d(LOG_TAG, "---- 3 onStart() / restartLoader ... sortMovieBy = " + sortMoviesBy);

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
       // getLoaderManager().restartLoader(MOVIE_FRAGMENT_ID, null, this);
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    }

    @Override //---- 4 ----
    public void onResume() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    //    sp.registerOnSharedPreferenceChangeListener(listener);
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
        Log.d(LOG_TAG, "---- 4 onResume() / registerOnSharedPreferenceChangeListener ----");
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        getLoaderManager().restartLoader(MOVIE_FRAGMENT_ID, null, this); // tky, help maintain position ??
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    }

    @Override //--- 5 ----
    public void onPause() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    //    sp.unregisterOnSharedPreferenceChangeListener(listener);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
        Log.d(LOG_TAG, "---- 5 onPause() / unregisterOnSharedPreferenceChangeListener ----");
    }

    // since we read the location when we create the loader, all we need to do is restart things

    void myRestartLoaderCode(){

        Log.d(LOG_TAG, "----   myRestartLoaderCode() / getLoaderManager().restartLoader(MOVIE_FRAGMENT_ID, null, this) ----");

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        getLoaderManager().restartLoader(MOVIE_FRAGMENT_ID, null, this);
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    }
        /*
        // since we read the location when we create the loader, all we need to do is restart things
        void onLocationChanged() {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
        */
    //---------------------------------------------------------------------------------------------
    //------ SETTING FOR implementation of SharedPreferences.OnSharedPreferenceChangeListener -----
    //---------------------------------------------------------------------------------------------
    // For SharedPreferences.OnSharedPreferenceChangeListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Log.d(LOG_TAG, "-- onSharedPreferenceChanged --");
    //  Toast.makeText(getContext(),"-- Main_Fragment/onActivityCreated/onSharedPreferenceChanged()/key --" + key, Toast.LENGTH_SHORT).show();

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        if ( key.equals(getString(R.string.pref_sortmovies_key)) ) {

            String mString = Utility.getPreferredSortSequence(getContext());

            Log.d(LOG_TAG, "-- onSharedPreferenceChanged ..... key: " + key + " ..... SortSeq: " + mString);

            getLoaderManager().restartLoader(MOVIE_FRAGMENT_ID, null, this);

            Log.d(LOG_TAG, "-- onSharedPreferenceChanged ..... MoviesSyncAdapter.syncImmediately(this) --");

            MoviesSyncAdapter.syncImmediately(getContext());

        }
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    }

    //--------------------------------------------------------------
    //-- LoaderManager.LoaderCallbacks<Cursor> --
    //----------- LoaderCursor Stuff (begin) -----------------------
    //--------------------------------------------------------------
    /*
    * Callback that's invoked when the system has initialized the Loader and
    * is ready to start the query. This usually happens when initLoader() is
    * called. The loaderID argument contains the ID value passed to the
    * initLoader() call.
    */
    // @param int id --- refers to MOVIE_LOADER
    // @param Bundle args ---
    @Override   // --- 1 ---
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        ////Log.d(LOG_TAG, "-- onCreateLoader() --");

        //+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+
        String sortMoviesBy = Utility.getPreferredSortSequence(getContext());
        Uri mUri;
        String[] mProjection;

        if (sortMoviesBy.equals(getString(R.string.pref_sortmovies_default_value))){
            mUri = PopularEntry.CONTENT_URI;
            mProjection = POPULAR_PROJECTION;
            Log.d(LOG_TAG, "----   onCreateLoader()/ popularity.desc/ " + mUri.toString());
        }
        else {
            mUri = RatingEntry.CONTENT_URI;
            mProjection = RATING_PROJECTION;
            Log.d(LOG_TAG, "----   onCreateLoader()/ rate_average.desc/ " + mUri.toString());
        }

        return new CursorLoader(
                getActivity(),              // context
                mUri,                       // Uri
                mProjection,                // projection
                null,                       // selection
                null,                       // selectionArg
                null
                //sortOrder                 // sortOrder
        );
        //+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+x+
    }

    // Called when a previously created loader has finished its load.
    /*
    * Defines the callback that CursorLoader calls
    * when it's finished its query
    */
    @Override //--- 5 ---
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        String mStrng;
        //Log.d(LOG_TAG, "--  onLoadFinished() --");

        Log.d(LOG_TAG, "----   onLoadFinished(), loader ID : " + loader.getId() + " --");
        if (cursor == null) {Log.d(LOG_TAG, "----   onLoadFinished(), cursor,NULL --");}
        else                {Log.d(LOG_TAG, "----   onLoadFinished(), cursor,NOT NULL -- cursor count : " + cursor.getCount() + " --");
           // mStrng = cursor.getString(0);
          //  Log.d(LOG_TAG, "-- cursor count : " + cursor.getCount() + " --");
        }

        //++++++++++++++++++++++++++++++++++++++
        //++++++++++++++++++++++++++++++++++++++
        // import a new set of cursor structured/found in 'Cursor' into the RecyclerViewAdapter.
        /*
         * Moves the query results into the adapter, causing the
         * RecyclerView fronting this adapter to re-display
        */
        mvRcyclrVwAdapterVwHldr.swapCursor(cursor);

        //xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
        if (mPosition != RecyclerView.NO_POSITION) {
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
        //xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
        //++++++++++++++++++++++++++++++++++++++
    }

    // Called when a previously created loader is being reset, and thus making its data unavailable.
    /*
    * Invoked when the CursorLoader is being reset. For example, this is
    * called if the data in the provider changes and the Cursor becomes stale.
    */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        Log.d(LOG_TAG, "----   onLoadReset() --");
        /* Clears out the adapter's reference to the Cursor.
        * This prevents memory leaks.
        */
        mvRcyclrVwAdapterVwHldr.swapCursor(null);
    }

    //--------------------------------------------------------------
    //----------- LoaderCursor Stuff (End) -------------------------
    //--------------------------------------------------------------
    //------------------------------------------------
    //----------- MENU Stuff (Begin)------------------
    // ------------------------------------------------
    // tky, to stay
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_moviefragment, menu);
    //    inflater.inflate(com.example.android.myproject_2.R.menu.menu_moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        /*
        // tky, to add, new, begin
        if (id == R.id.most_popular) {
            FetchMoviesDb_AsyncTask mtask = new FetchMoviesDb_AsyncTask(getActivity());
            mtask.execute(POPULARITY_DESC);
            return true;
        }
        return super.onOptionsItemSelected(item);
        // tky, to add, new, end
        */
        /*
        if (id == R.id.highest_rated) {

            FetchMoviesAsyncTask myAsyncTask = new FetchMoviesAsyncTask();
            myAsyncTask.execute(VOTE_AVERAGE_DESC);

            return true;
        } else
        */
        //if (id == com.example.android.myproject_2.R.id.most_popular) {
        if (id==R.id.most_popular){

            Log.d(LOG_TAG, " ---- Picked most popular in item List");

            // .makeText(getContext()," -- Main_Fragment/onOptionsItemSelected() --", Toast.LENGTH_SHORT).show();

            return true;
        }
        return super.onOptionsItemSelected(item);

    }
    //--------------- MENU Stuff (End)----------------
    //------------------------------------------------

    private Uri getTheURI4SortSeq (){
        String sortType = Utility.getPreferredSortSequence(getContext());
        Uri tryUri;

        if (sortType.equals(getString(R.string.pref_sortmovies_default_value))){
            tryUri = PopularEntry.CONTENT_URI;
        }else                                    {tryUri = RatingEntry.CONTENT_URI;}
        //++++++++++++++++++++++++++
        // if (sortType.equals("popularity.desc")) {tryUri = PopularEntry.CONTENT_URI;}
        // else                                    {tryUri = RatingEntry.CONTENT_URI;}
        //++++++++++++++++++++++++++
        return tryUri;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_INDEX, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    private long getNewPosition(int position) {
        return mvRcyclrVwAdapterVwHldr.getItemId(position);
    }
    /* When the system is ready for the Fragment to appear, this displays
     * the Fragment's View
     */
    @Override //--- 1 ---
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RecyclerView.LayoutManager mLayoutManager;
        //return super.onCreateView(inflater, container, savedInstanceState);
        Log.d(LOG_TAG, "---- 1 onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) --");

        //****************
        // tky comment ....
        // Implementation the interface, 'NAME'/MvItemOnClickHandler
        // with the method-name/myOnClick found within the interface declaration.
        Movie_RecyclerViewAdapter.MvItemOnClickHandler mvRvAdptr_OnClickHander =
                new Movie_RecyclerViewAdapter.MvItemOnClickHandler() {
                    @Override
                    public void myOnClick(Movie_RecyclerViewAdapter.Movie_RvVwHldr vh) {
                        mPosition = vh.getAdapterPosition();
                        nwPosition = getNewPosition(mPosition);
                        Log.d(LOG_TAG, "-------- " + nwPosition);
                        Uri tryUri = getTheURI4SortSeq();
                        tryUri = ContentUris.withAppendedId(tryUri,nwPosition );
//                        tryUri = ContentUris.withAppendedId(tryUri,mPosition + 1 );
                  //      String sortType = Utility.getPreferredSortSequence(getContext());
                  ///   Toast.makeText(getContext(),"-- MvItemOnClickHandler() / myOnClick() --: " + mPosition + " -- " + sortType + " -- ", Toast.LENGTH_SHORT).show();
                  //      Log.d(LOG_TAG, "-- MvItemOnClickHandler().myOnClick(),  mPosition:" + mPosition + "  sortType:" + sortType );
                  //      Log.d(LOG_TAG, "-- tryUri : " + tryUri.toString());
                        mCallBackListner.onItemSelectedInRecyclerView(tryUri);
                        //-- or --
                        // ((CallBackListner) getActivity()).onItemSelectedInRecyclerView(tryUri);



                    }
        };
        mvRcyclrVwAdapterVwHldr = new Movie_RecyclerViewAdapter(getContext(), mvRvAdptr_OnClickHander);
        //****************
       // mvRcyclrVwAdapterVwHldr = new Movie_RecyclerViewAdapter(getContext());

    //  View rootView = inflater.inflate(R.layout.fragment_movie_top, container, false);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_id4_movies);

        //mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));

        mLayoutManager = new GridLayoutManager(getContext(), 2);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mvRcyclrVwAdapterVwHldr);
        mRecyclerView.setHasFixedSize(true);


        //+++++++++++++++++++++++++++++
        if ((savedInstanceState != null) && savedInstanceState.containsKey(SELECTED_INDEX)) {
            mPosition = savedInstanceState.getInt(SELECTED_INDEX);
        }
        //+++++++++++++++++++++++++++++
    //    Toast.makeText(getContext()," -- 1 Main_Fragment/onCreateView() --", Toast.LENGTH_SHORT).show();
        return rootView;
    }
//    //---Toast.makeText(getContext()," -- 1 onCreateView() --", Toast.LENGTH_SHORT).show();
//    @Override //---- 2 ----
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        Log.d(LOG_TAG, "-- 2 onCreateView() --");
//
//        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//
//        // arrayListOfMovieInfo = new ArrayList<MoviesSelectedInfo>();
//        //moviesCursorAdapter = new Movie_CursorAdapter(getActivity(), arrayListOfMovieInfo);
//        moviesCursorAdapter = new Movie_CursorAdapter(getActivity(), null, 0);
//
//        // tky add, tky try
//        //View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//        //View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//
//        mGridView = (GridView) rootView.findViewById(R.id.grid_view);
//
//        // Attach the Movies-Content-in-Cursor --> to --> the Grid-View
//        mGridView.setAdapter(moviesCursorAdapter);
//
//        // tky remove, try
//        /*
//        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("aMovieKey", arrayListOfMovieInfo.get(position));
//
//                Intent intent = new Intent(getActivity(), MovieDetails_Activity.class);
//                intent.putExtras(bundle);
//
//                startActivity(intent);
//
//            }
//        });
//        */
//        return rootView;
//    }
}
//------------------------------------------------------------

