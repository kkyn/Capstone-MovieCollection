package com.example.android.fnlprjct;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.example.android.fnlprjct.data.MovieContract.MovieInfoEntry;
import com.example.android.fnlprjct.data.MovieContract.MovieReviewEntry;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MDetails_Fragment1 extends Fragment
                             implements
                             // LoaderManager.LoaderCallbacks<Cursor>,
                              SharedPreferences.OnSharedPreferenceChangeListener
                            , View.OnClickListener
{

    public MDetails_Fragment1() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    // tky comment ---------------------------------------------
    // + Used by the caller, MyFrgmntPgrAdptr/FragmentStatePagerAdapter
    // + To create a new instance of ArticleDetailFragment/Fragment,
    //    providing "num" as an argument.
    //---------------------------------------------------------------
    public static MDetails_Fragment1 newInstance(int itemId) {

        Bundle arguments = new Bundle();
        arguments.putInt(ITEMID_KEY, itemId); // ?? itemId ??
       /// arguments.putInt("MyKey1", itemId); // ?? itemId ??

        MDetails_Fragment1 fragment = new MDetails_Fragment1();

        fragment.setArguments(arguments);
        return fragment;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private static final String LOG_TAG = MDetails_Fragment1.class.getSimpleName();
    private static final String ITEMID_KEY = "ItemID_Key";

    private static final int DETAIL_MOVIE_LOADER = 3;
    private static final int MOVIE_REVIEW_LOADER = 4;

    private Uri mUri;
    private Uri uri;
    private int mItemId;

    static final String DETAIL_URI = "URI";
    static final String MOVIE_ID = "MovieID";
    private String videoId;

    private Details_Adapter dtlsAdapter;
    String trgtShrdElmtTrnstn;
    public boolean isFavouriteEnabled = false;

    ///////////////////////////////////////////////
    private static final String[] PROJECTION_MOVIE_REVIEW =
        new String[]{
            MovieReviewEntry.TABLE_NAME + "." + MovieReviewEntry._ID,
            MovieReviewEntry.TABLE_NAME + "." + MovieReviewEntry.COL_MV_ID,
            MovieReviewEntry.COL_REVIEWER,
            MovieReviewEntry.COL_REVIEWCONTENT
    };
    //public static final int PROJECTION_RATING_ID = 0;
    //public static final int INDX_1_MOVIE_ID = 1;
    public static final int INDX_1_REVIEWER = 2;
    public static final int INDX_1_REVIEWCONTENT = 3;

    ///////////////////////////////////////////////

    /*@BindView(R.id.movietitle_tv)
    TextView movietitle;*/
    @BindView(R.id.moviethumbnail_iv)
    DynamicHeightNetworkImageView  moviethumbnail;
    @BindView(R.id.moviereleasedate_tv)
    TextView moviereleasedate;
    @BindView(R.id.movieratings_tv)
    TextView movieratings;
    @BindView(R.id.moviesynopsis_tv)
    TextView moviesynopsis;
    @BindView(R.id.movievideo_btn)
    Button movievideo;
    @BindView(R.id.favourite_btn)
    ImageButton moviefavourite;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    //Toolbar mToolbar;
    private TextView mTextView;
    //private DynamicHeightNetworkImageView moviethumbnail;
    //-----------------------------------------------------------
    //-- for FetchMoviesDbAsyncTask_1 ... AsyncTask<...> Stuff ----
    //-----------------------------------------------------------

    public class FetchComplete implements FetchMoviesDbAsyncTask.OnAsyncTaskCompletedListener {

        @Override
        public void onAsyncTaskCompleted(String[] result) {

            if (result != null) {
                if (result.length == 0){
                    Toast.makeText(getContext(),"Error -- No Videos Trailer for YouTube" , Toast.LENGTH_LONG).show();
                }
                else {
                    //TODO; update VideoId for youtube
                    videoId = result[0];

                    String mVideoPath = "vnd.youtube:" + videoId;
                    Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mVideoPath));

                    PackageManager pckgMngr = getContext().getPackageManager();

                    // Check for youtube app in device. ??
                    List<ResolveInfo> infos = pckgMngr.queryIntentActivities(mIntent, PackageManager.MATCH_DEFAULT_ONLY);

                    // There is no youtube app in device
                     if (infos.size() == 0) {
                         mIntent = new Intent(Intent.ACTION_VIEW,
                                 Uri.parse("http://www.youtube.com/watch?v=" + videoId));
                    }
                    startActivity(mIntent);
                }
            }
        }
    }

    //-----------------------------------------------------------
    //-----------------------------------------------------------
    private void removeFromFavourites(String movieID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieInfoEntry.COL_FAVOURITES, 0);

        String select = MovieInfoEntry.COL_MV_ID + "=?";
        String[] selectArg = new String[]{movieID};

        int rowId = getActivity().getContentResolver().update(
                    MovieInfoEntry.CONTENT_URI,
                    contentValues,
                    select,
                    selectArg
            );
        getContext().getContentResolver().notifyChange(MovieReviewEntry.CONTENT_URI, null);
    }

    private void saveToFavourites(String movieID) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieInfoEntry.COL_FAVOURITES, 1);

        String select = MovieInfoEntry.COL_MV_ID + "=?";
        String[] selectArg = new String[]{movieID};

        int rowId = getActivity().getContentResolver().update(
                MovieInfoEntry.CONTENT_URI,
                contentValues,
                select,
                selectArg
        );
        getContext().getContentResolver().notifyChange(MovieReviewEntry.CONTENT_URI, null);

    }

    private boolean checkFavourites(String movieID) {

        String selection = MovieInfoEntry.COL_MV_ID + "=?" + " AND " + MovieInfoEntry.COL_FAVOURITES + "=?";
        String[] selectionArg = new String[]{movieID, "1"};

        Boolean favouritesBoolean = false;

        Cursor cursor = getActivity().getContentResolver().query(
                MovieInfoEntry.CONTENT_URI,   // uri
                null,                           // projection  ( get the specified Column(s) )
                selection,                      // selection   ( satisfying the  conditions )
                selectionArg,                   // selectionArg ( conditions of specific values )
                null                            // sort order
        );

        if (cursor != null) {
            if (cursor.moveToFirst() && cursor.getCount() > 0) {
                favouritesBoolean = true;
            }
        }

        cursor.close();

        return favouritesBoolean;
    }

    //-------------------------------------------
    //-- for View.onClickListener, movievideo.setOnClickListener(this);
    //-------------------------------------------
    @Override
    public void onClick(View view) {

        String movieID = mUri.getPathSegments().get(1);

        switch (view.getId()) {

            case R.id.movievideo_btn:

            case R.id.moviethumbnail_iv:

                //...................
                // TODO: call DBAsyncTAsk , call method FetchComplete()
                //
                FetchMoviesDbAsyncTask mTask = new FetchMoviesDbAsyncTask(getContext(), new FetchComplete());
                mTask.execute(movieID);

                /*String mVideoPath = "vnd.youtube:" + videoId;
                Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mVideoPath));
                startActivity(mIntent);*/

                break;

            case R.id.favourite_btn:

                //movieID = mUri.getPathSegments().get(1);

                isFavouriteEnabled = checkFavourites(movieID);

                if (!isFavouriteEnabled) {
                    moviefavourite.setImageResource(android.R.drawable.btn_star_big_on);
                    saveToFavourites(movieID);
                }
                else {
                    moviefavourite.setImageResource(android.R.drawable.btn_star_big_off);
                    removeFromFavourites(movieID);
                }

                break;

            default:
                break;
        }
    }
    //-------------------------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Return the arguments supplied when the fragment was instantiated, if any.
        // ?????? is it needed ???
        Bundle bundle = this.getArguments();
        if (bundle.containsKey(ITEMID_KEY) == true) {
            mItemId = bundle.getInt(ITEMID_KEY);
        /*if (bundle.containsKey("MyKey1") == true) {
            mItemId = bundle.getInt("MyKey1");*/

            uri = MovieInfoEntry.CONTENT_URI;
            uri = ContentUris.withAppendedId(uri, mItemId);
            mUri = uri;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /*Intent intent = getActivity().getIntent();
        mUriString = intent.getDataString();
        mUri = intent.getData();*/

//        Bundle mBundle = this.getArguments();
//        if (mBundle != null) {
//            mUri = mBundle.getParcelable(MDetails_Fragment1.DETAIL_URI);
//        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_details1, container, false);

        RecyclerView rView = (RecyclerView) rootView.findViewById(R.id.recycler_view_moviedetails);

        ButterKnife.bind(this, rootView);

        movievideo.setOnClickListener(this);

      //  moviethumbnail = (DynamicHeightNetworkImageView) rootView.findViewById(R.id.moviethumbnail_iv);
        moviethumbnail.setOnClickListener(this);
        moviefavourite.setOnClickListener(this);

        trgtShrdElmtTrnstn = getString(R.string.shared_name) + mItemId;
        //trgtShrdElmtTrnstn = "photo" + mItemId;

        ViewCompat.setTransitionName(moviethumbnail, trgtShrdElmtTrnstn);
        /// moviethumbnail
        // **************************************
        // --- old way ?? ---
        //mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null.
        //-- To to use setSupportActionBar in fragment, use '((AppCompatActivity)getActivity()).'  --
        //((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // --- new way ?? --- using
        // https://material.io/icons/#ic_arrow_back
        // https://developer.android.com/reference/android/support/v7/widget/Toolbar.html
        if (mToolbar != null ){

            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_1);

            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //----------------------------------------------------------------
                    // Indirect a call to a method via ArticaleDetailActivity/Activity.
                    // supportFinishAfterTransition() -- refers to a method in FragmentActivity.java
                    //----------------------------------------------------------------
                    //getActivityCast().supportFinishAfterTransition();   // onSupportNavigateUp();
                }
            });
        }
        // **************************************
        //++++++++++++++++++++++++++++++++++++++
        // https://developer.android.com/reference/android/support/v4/app/Fragment.html#getLoaderManager()
        // https://developer.android.com/reference/android/support/v4/app/LoaderManager.html
        // getLoaderManager() :- Return the LoaderManager for this fragment, creating it if needed.
        // initLoader :- Ensures a loader is initialized and active
        getLoaderManager().initLoader(DETAIL_MOVIE_LOADER, null, myTry_0);
        getLoaderManager().initLoader(MOVIE_REVIEW_LOADER, null, myTry_1);
        //++++++++++++++++++++++++++++++++++++++

        dtlsAdapter = new Details_Adapter(getContext());

        LinearLayoutManager lytMngr = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        rView.setLayoutManager(lytMngr);
        rView.setAdapter(dtlsAdapter);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();    //Log.d(LOG_TAG, "++++ 3 onStart() --");
    }

    @Override
    public void onPause() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        sp.unregisterOnSharedPreferenceChangeListener(this);
        //sp.unregisterOnSharedPreferenceChangeListener(listener);

        super.onPause();    //Log.d(LOG_TAG, "++++ 5 onPause() --");
    }

    @Override
    public void onResume() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        sp.registerOnSharedPreferenceChangeListener(this);
        //sp.registerOnSharedPreferenceChangeListener(listener);

        super.onResume();
    }

    //-----------------------------------------------------------
    //--- for SharedPreferences.OnSharedPreferenceChangeListener
    //-----------------------------------------------------------
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    //-----------------------------------------------------------
    //-- LoaderManager.LoaderCallbacks<Cursor> ------------------
    //----------- LOADER Stuff ----------------------------------
    //-----------------------------------------------------------
    //    private LoaderManager.LoaderCallbacks<Cursor> myTry_0 = new MyTryLoader(context, mUri);
    //    private LoaderManager.LoaderCallbacks<Cursor> myTry_0 = new MyTryLoader(this.getActivity(), mUri);
    //    private LoaderManager.LoaderCallbacks<Cursor> myTry_0 = new MyTryLoader(getActivity(), mUri);
    //    private LoaderManager.LoaderCallbacks<Cursor> myTry_0 = new MyTryLoader(getContext(), mUri);
    private LoaderManager.LoaderCallbacks<Cursor> myTry_0 = new LoaderManager.LoaderCallbacks<Cursor>()
    {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            String[] mProjection = MyQuery.MovieInfo.PROJECTION;

            return new CursorLoader(getActivity(), mUri, mProjection, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

            if (cursor != null && cursor.moveToFirst()) {

                //movietitle.setText(cursor.getString(MyQuery.MovieInfo.COL_MOVIE_TITLE));
                moviereleasedate.setText(cursor.getString(MyQuery.MovieInfo.COL_RELEASEDATE));
                movieratings.setText(cursor.getString(MyQuery.MovieInfo.COL_MOVIE_RATING));
                moviesynopsis.setText(cursor.getString(MyQuery.MovieInfo.COL_OVERVIEW));

                // ------------------------
                // ---- Using Volley ------
                ImageLoader imageLoader = ImageLoaderHelper.getInstance(getContext()).getImageLoader();

                String stringUrl = cursor.getString(MyQuery.MovieInfo.COL_BACKDROP_PATH);

                Log.d(LOG_TAG, "? ? ? ?  " + stringUrl + " ? ? ? ?");
                // -- thumb-nail-View --
                // .setImageUrl -- define in ImageView
                moviethumbnail.setImageUrl(stringUrl, imageLoader);
                moviethumbnail.setAspectRatio(1.5f);

                scheduleStartPostponedTransition(moviethumbnail);

                /*Picasso.with(getContext()).load(cursor.getString(INDX_BACKDROP_PATH))
                        .placeholder(R.drawable.sample_1)
                        .error(R.drawable.sample_0)
                        .into(moviethumbnail);*/

                int favouriteValue = cursor.getInt(MyQuery.MovieInfo.COL_FAVOURITES);

                if (favouriteValue == 1) {
                    isFavouriteEnabled = true;
                    moviefavourite.setImageResource(android.R.drawable.btn_star_big_on);
                } else {
                    isFavouriteEnabled = false;
                    moviefavourite.setImageResource(android.R.drawable.btn_star_big_off);
                }
                //...................
                // TODO: call DBAsyncTAsk , call method FetchComplete()
                //
                /*FetchMoviesDbAsyncTask mTask = new FetchMoviesDbAsyncTask(getContext(), new FetchComplete());
                mTask.execute(cursor.getString(INDX_MOVIE_ID));*/
                //...................

                cursor.close();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            Log.d(LOG_TAG, "++ onLoaderReset() --");
            loader = null;
        }

        ////////////////////////////////////////////////////////////
        private void scheduleStartPostponedTransition(final View sharedElement) {
            sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        getActivity().startPostponedEnterTransition();
                        return true;
                    }
                });
        }

        ////////////////////////////////////////////////////////////
    };

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private LoaderManager.LoaderCallbacks<Cursor> myTry_1 = new LoaderManager.LoaderCallbacks<Cursor>()
    {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            String[] mProjection = PROJECTION_MOVIE_REVIEW;
            String mvID = mUri.getPathSegments().get(1);
            Uri xUri = ContentUris.withAppendedId(MovieReviewEntry.CONTENT_URI, Long.valueOf(mvID));

            return new CursorLoader(getActivity(), xUri, mProjection, null, null, null);

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            dtlsAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

            dtlsAdapter.swapCursor(null);
        }
    };


}