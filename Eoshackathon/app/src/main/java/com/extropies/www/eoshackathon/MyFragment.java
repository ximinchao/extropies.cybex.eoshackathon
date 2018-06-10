package com.extropies.www.eoshackathon;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by inst on 18-6-9.
 */

public class MyFragment extends Fragment{
    private Button setAccountBtn;
    private View view;
    private String newAccountNameString = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_my, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setAccountBtn = view.findViewById(R.id.setAccountBtn);
        setAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAccountBtnClick();
            }
        });
    }

    public String getNewAccountNameString() {
        return newAccountNameString;
    }

    public void setNewAccountNameString(String input) {
        newAccountNameString = input;
    }

    public void setAccountBtnClick() {
        final View viewDialog = (View)getLayoutInflater().inflate(R.layout.dialog_editview,null);
        final EditText dialogAccountEditText = (EditText) viewDialog.findViewById(R.id.dialog_edittext);
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Set Account");
        builder.setView(viewDialog);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText accountNameET = (EditText)viewDialog.findViewById(R.id.dialog_edittext);
                EditText passwordET = (EditText)viewDialog.findViewById(R.id.dialog_pin_edittext);
                String accountNameString = accountNameET.getText().toString();
                String passwordString = passwordET.getText().toString();
                if (passwordString.equals("123456")) {//just a demo
                    newAccountNameString = accountNameString;
                }
            }
        });
        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog=builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                viewDialog.setFocusable(true);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(dialogAccountEditText, InputMethodManager.SHOW_IMPLICIT);

            }
        });
        dialog.show();
    }

}
