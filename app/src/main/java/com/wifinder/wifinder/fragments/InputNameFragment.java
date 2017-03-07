package com.wifinder.wifinder.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.wifinder.wifinder.R;
import com.wifinder.wifinder.interfaces.InputNameInterface;

/**
 * Created by yogaub on 25/11/15.
 */
public class InputNameFragment extends DialogFragment {

    private InputNameInterface inputNameListener;
    public EditText inputNameEditText;
    private String userName;


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.input_name_fragment, null);
        inputNameEditText = (EditText) layout.findViewById(R.id.input_dialog_editname);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (inputNameEditText == null)
                            Log.d("INPUTNAMEDBG", "inputNameEditText IS NULL");
                        inputNameListener.onDialogPositiveClick(InputNameFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }



    //Needed to initialize the listener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            inputNameListener = (InputNameInterface) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement InputNameInterface");
        }
    }

}
