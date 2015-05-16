package com.nilhcem.bblfr.ui.splashscreen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import com.nilhcem.bblfr.R;

public class ErrorDialogFragment extends DialogFragment {

    public static final String TAG = ErrorDialogFragment.class.getSimpleName();
    private static final String ARG_NETWORK_ISSUE = "networkIssue";

    private boolean mNetworkIssue;

    public static ErrorDialogFragment create(boolean networkIssue) {
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_NETWORK_ISSUE, networkIssue);
        fragment.setArguments(bundle);
        fragment.setCancelable(false);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkIssue = getArguments().getBoolean(ARG_NETWORK_ISSUE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(mNetworkIssue ? R.string.splash_error_network_title : R.string.splash_error_unknown_title)
                .setMessage(mNetworkIssue ? R.string.splash_error_network_content : R.string.splash_error_unknown_content)
                .setPositiveButton(R.string.splash_error_button_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exit(getActivity(), dialog);
                    }
                })
                .setNegativeButton(R.string.splash_error_button_prefs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                        exit(getActivity(), dialog);
                    }
                })
                .create();
    }

    private void exit(FragmentActivity activity, DialogInterface dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
        if (activity != null) {
            activity.finish();
        }
    }
}
