package com.example.android.fnlprjct.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.fnlprjct.MainFragment;
import com.example.android.fnlprjct.R;
import com.example.android.fnlprjct.Utility;
import com.example.android.fnlprjct.data.MovieContract.MovieInfoEntry;
import com.example.android.fnlprjct.sync.MSyncAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;


//  https://developer.android.com/reference/android/app/DialogFragment.html
//  https://developer.android.com/guide/topics/ui/dialogs.html
//  https://developer.android.com/reference/android/app/AlertDialog.Builder.html
//  https://developer.android.com/reference/android/widget/TextView.html

public class ChangeYearDialogFragment extends DialogFragment
                implements SharedPreferences.OnSharedPreferenceChangeListener
{

    private static final String LOG_TAG = ChangeYearDialogFragment.class.getSimpleName();

    // Empty constructor required for DialogFragment
    public ChangeYearDialogFragment(){
    }

    public static ChangeYearDialogFragment
    newInstance(){
        ChangeYearDialogFragment cyDialog = new ChangeYearDialogFragment();
        return cyDialog;
    }

    //----------------------------------------------------------------
    // Start-A-Fragment-In-A-Fragment .... Start Dialog in Fragment
    // Step 2 : ( Return data to 'source'/'target'-fragment )
    private void sendBackResult() {
        Bundle bundle = new Bundle();
        bundle.putString(MainFragment.DIALOG_KEY, enterYear_et.getText().toString());

        Intent intent = new Intent().putExtras(bundle);

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);

    }

    @BindView(R.id.year_dialog) EditText enterYear_et;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        SharedPreferences
            sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences
            sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        // +++++++++++ View Inflater Stuff ++++++++++++++
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        View view = layoutInflater.inflate(R.layout.dialogfragment_changeyear, null);

        ButterKnife.bind(this, view);

        String year = Integer.toString(Utility.getThisYearValue());
        enterYear_et.setHint(getActivity().getString(R.string.dialog_hint, year));

        // reference: stackoverflow.com -- 9596010 --
        // Set a special listener to be called when an action is performed on the edittext view.
        // Interface definition for a callback to be invoked when an action is performed on the editor.
        enterYear_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            // Called when an action is being performed.
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                /* When I(the user) press the 'Done' option in the softkeyboard,
                 the app's dialogFragment will perform the same sequence
                 as when the user select the 'submit' option in the dialog UI.
                */
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                    (actionId == EditorInfo.IME_ACTION_DONE)
                    )
                {

                    String stringEnteredYear = enterYear_et.getText().toString();
                    int intEnteredYear = Integer.parseInt(stringEnteredYear);
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.error_not_beyond_this_year), Toast.LENGTH_LONG); //.show();

                    if (intEnteredYear > Utility.getThisYearValue()) {
                        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();

                        return true;    // if you have consumed the action
                    } else {
                        searchMoviesYear();
                        sendBackResult();
                        return true;    // if you have consumed the action
                    }
                } else {
                    return false;       // if you have NOT consumed the action
                }
            }
        });

        // +++++++++++ AlertDialog Builder Stuff ++++++++++++++
        dialogBuilder.setView(view);
        dialogBuilder.setTitle(R.string.dialog_title);
        dialogBuilder.setMessage(getString(R.string.dialog_comment));

        dialogBuilder.setPositiveButton(
            getString(R.string.dialog_submit),
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String stringEnteredYear = enterYear_et.getText().toString();
                    int intEnteredYear = Integer.parseInt(stringEnteredYear);
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.error_not_beyond_this_year), Toast.LENGTH_LONG); //.show();

                    if (intEnteredYear > Utility.getThisYearValue()) {
                        toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0); // Gravity.CENTER|Gravity.LEFT
                        toast.show();

                    } else {
                        searchMoviesYear();
                        sendBackResult();
                    }
                }
            });

        dialogBuilder.setNegativeButton(
            getString(R.string.dialog_cancel), null
        );

        Dialog dialog = dialogBuilder.create();

        // ++++++++++++++++++++++++++++++++++++++++++++++++++

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return dialog;
    }

    // -------------------------------------------------
    private void searchMoviesYear() {

        String yearKey = getResources().getString(R.string.pref_key_year);
        String year = enterYear_et.getText().toString();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(yearKey, year);
        editor.apply();

        dismissAllowingStateLoss();

    }



    //---------------------------------------------------------------------------------------------
    //------ SETTING FOR implementation of SharedPreferences.OnSharedPreferenceChangeListener -----
    //---------------------------------------------------------------------------------------------
    // For SharedPreferences.OnSharedPreferenceChangeListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getResources().getString(R.string.pref_key_year))) {

            getContext().getContentResolver().notifyChange(MovieInfoEntry.CONTENT_URI, null);

            MSyncAdapter.syncImmediately(getActivity());
        }
    }
}
