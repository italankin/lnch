package com.italankin.lnch.util.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.italankin.lnch.R;

public class MessageDialogFragment extends DialogFragment {

    public static MessageDialogFragment newInstance(CharSequence title, CharSequence message) {
        Bundle args = new Bundle();
        args.putCharSequence(ARG_TITLE, title);
        args.putCharSequence(ARG_MESSAGE, message);
        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";

    private OnDismissListener onDismissListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDismissListener) {
            onDismissListener = (OnDismissListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onDismissListener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        return new AlertDialog.Builder(getContext())
                .setTitle(args.getCharSequence(ARG_TITLE))
                .setMessage(args.getCharSequence(ARG_MESSAGE))
                .setOnDismissListener(dialog -> {
                    if (onDismissListener != null) {
                        onDismissListener.onDismiss(getTag());
                    }
                })
                .setPositiveButton(R.string.ok, null)
                .create();
    }

    public interface OnDismissListener {

        void onDismiss(String tag);
    }
}
