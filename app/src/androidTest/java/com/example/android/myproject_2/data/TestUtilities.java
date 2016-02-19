package com.example.android.myproject_2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.android.myproject_2.data.MovieContract.MovieInfoEntry;
import com.example.android.myproject_2.data.MovieContract.SortByEntry;
import com.example.android.myproject_2.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your WeatherContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {



    static final String TEST_SORTBY_VALUE = "popularity";

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }



    /*
        Students: You can uncomment this helper function once you have finished creating the
        SortBy part of the MovieContract.
    */
    static ContentValues createMovieInfoValues(long sortByRowId) {

        ContentValues movieInfoValues = new ContentValues();

        movieInfoValues.put(MovieInfoEntry.COL_ID, sortByRowId);

        movieInfoValues.put(MovieInfoEntry.COL_TITLE, "MyMovie");
        movieInfoValues.put(MovieInfoEntry.COL_RELEASEDATE, "ReleaseDate_Today");
        movieInfoValues.put(MovieInfoEntry.COL_OVERVIEW, "MyMovie_Synopsis");

        movieInfoValues.put(MovieInfoEntry.COL_POSTERLINK, "MyMovie_PosterLink");
        movieInfoValues.put(MovieInfoEntry.COL_VIDEOLINK, "MyMovie_VideoLink");

        return movieInfoValues;
    }
    /*
        Students: Use this to create some default MovieSelect values for your database tests.
     */
    static ContentValues createSortByValues() {

        // Create a new map of values, where column names are the keys
        ContentValues MovieSelectValues = new ContentValues();

        MovieSelectValues.put(SortByEntry.COL_SORTBYSETTING, TEST_SORTBY_VALUE);
        MovieSelectValues.put(SortByEntry.COL_MOVIEID, 1L);

        return MovieSelectValues;
    }

    /*
        Students: You can uncomment this function once you have finished creating the
        SortByEntry part of the MovieContract as well as the MovieDbHelperr.
     */
    static long insertMovieInfoValues (Context context) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues dryTestValues = TestUtilities.createSortByValues();
        /// ContentValues dryTestValues = TestUtilities.createMovieInfoValues();

        long sortByRowId;
        sortByRowId = db.insert(SortByEntry.TABLE_NAME, null, dryTestValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", sortByRowId != -1);

        return sortByRowId;
    }


    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
