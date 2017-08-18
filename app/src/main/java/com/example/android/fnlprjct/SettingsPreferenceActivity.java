package com.example.android.fnlprjct;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.example.android.fnlprjct.data.MovieContract.MovieInfoEntry;
import com.example.android.fnlprjct.sync.MSyncAdapter;

//import static com.example.android.myproject_2.MainFragment.*;
//==============================================
//==============================================
/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsPreferenceActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener,
             SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String LOG_TAG = SettingsPreferenceActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // - Add 'general' preferences, defined in the XML file
        // - Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        // Finds a Preference based on its key.
        Preference mPreference = this.findPreference(getString(R.string.pref_key_movies_sortby));

        bindPreferenceSummaryToValue(mPreference);
    }

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //---------++ SETTING FOR implementation of Preference.OnPreferenceChangeListener --------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {

        // Set the listener to watch for value changes.
        // Sets the callback to be invoked when this 'preference' is changed by the user
        // (but before the internal state has been updated).
        // 'this' refers to 'Preference.OnPreferenceChangeListener'
        preference.setOnPreferenceChangeListener(this);

        //++++++++++++++++++++++++++++++
        PreferenceManager preferenceManager = getPreferenceManager();

        // Gets a SharedPreferences instance that points to the default file
        // that is used by the preference framework in the given context.
        // 'preference.getContext()' ++ get the context of this preference
        SharedPreferences sharedPreferences = preferenceManager.getDefaultSharedPreferences(preference.getContext());

        // Retrieve a String value from the preferences.
        Object object = sharedPreferences.getString(preference.getKey(), "");

        // Set the preference summaries
        setPreferenceSummary(preference, object);
        //++++++++++++++++++++++++++++++
    }

    private void setPreferenceSummary(Preference preference, Object value) {

        String mKey = preference.getKey();
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {

            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;

            int index = listPreference.findIndexOfValue(stringValue);
            if (index >= 0) {
                CharSequence charSequence[] = listPreference.getEntries();
                preference.setSummary(charSequence[index]);
                // -- or --
                //preference.setSummary(listPreference.getEntries()[index]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    //
    // implementation of interface Preference.OnPreferenceChangeListener
    //
    // This is called 'BEFORE' the preference is changed.
    // Called when a Preference has been changed by the user.
    // This is called before the state of the Preference is about to be updated and before the state is persisted.
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {

        setPreferenceSummary(preference, value);

        return true;
    }

    /////////////////////////////////////////////////////////////////////
    // Registers a shared preference change listener that gets notified when preferences change
    @Override
    protected void onResume() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    // Unregisters a shared preference change listener
    @Override
    protected void onPause() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------
    //----++ SETTING FOR implementation of SharedPreferences.OnSharedPreferenceChangeListener -----
    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------
    // Implementation of interface SharedPreferences.OnSharedPreferenceChangeListener.
    // This gets called AFTER the preference is changed, which is important because we
    // start our synchronization here

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


        if (key.equals(getString(R.string.pref_key_movies_sortby))) {

            String string = Utility.getPreferredSortSequence(this);

            if (string.equals(getString(R.string.pref_value_movies_sortby_default)) ||
                string.equals(getString(R.string.pref_value_movies_sortby_ratings)) ||
                string.equals(getString(R.string.pref_value_movies_sortby_ownfavorites))
               )
            {
                getContentResolver().notifyChange(MovieInfoEntry.CONTENT_URI, null);
            }

            MSyncAdapter.syncImmediately(this);
        }

    }
}