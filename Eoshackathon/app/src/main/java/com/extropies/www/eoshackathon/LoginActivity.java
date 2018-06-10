package com.extropies.www.eoshackathon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent; 
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.extropies.www.eoshackathon.Utils.SharePreferenceUtils;

/**
 * Created by inst on 18-6-9.
 */

public class LoginActivity extends Activity {
    private String currentAccountName;
    private EditText accountEdittext;
    private EditText passwordEdittext;
    private String oldAccountName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//
        setContentView(R.layout.activity_login);

        accountEdittext = findViewById(R.id.loginUserName);
        passwordEdittext = findViewById(R.id.loginUserPassword);

        if (SharePreferenceUtils.contains(this, "AccountName")) {
            currentAccountName = (String) SharePreferenceUtils.get(this, "AccountName", "");
            accountEdittext.setText(currentAccountName);
            passwordEdittext.setFocusable(true);
            passwordEdittext.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(passwordEdittext,InputMethodManager.SHOW_IMPLICIT);
            oldAccountName = currentAccountName;
        } else {
            currentAccountName = "";
            accountEdittext.setFocusable(true);
            accountEdittext.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(accountEdittext,InputMethodManager.SHOW_IMPLICIT);
            oldAccountName = currentAccountName;
        }

        passwordEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String accountString = accountEdittext.getText().toString();
                String passwordString = passwordEdittext.getText().toString();
                if (accountString.equals("")) {
                    accountEdittext.setFocusable(true);
                    accountEdittext.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(accountEdittext,InputMethodManager.SHOW_IMPLICIT);
                    return false;
                }
                if (passwordString.equals("")) {
                    passwordEdittext.setFocusable(true);
                    passwordEdittext.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(passwordEdittext,InputMethodManager.SHOW_IMPLICIT);
                    return false;
                } else if (passwordString.equals("123456")) {//just a demo
                    if (!oldAccountName.equals(accountString)){
                        SharePreferenceUtils.put(LoginActivity.this,"AccountName",accountString);
                    }
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtra("userName", accountString);
                    CheckBox checkBox = findViewById(R.id.cardWalletCheckbox);
                    if (checkBox.isChecked()) {
                        intent.putExtra("coldWallet", "true");
                    } else {
                        intent.putExtra("coldWallet", "false");
                    }
                    startActivity(intent);
                    return false;
                }
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        System.out.println("===== LoginActivity onResume" );
        super.onResume();
        passwordEdittext.setText("");
    }

    public void loginBtnClick(View view) {
        String accountString = accountEdittext.getText().toString();
        if (accountString.equals("")) {
            accountEdittext.setFocusable(true);
            accountEdittext.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(accountEdittext,InputMethodManager.SHOW_IMPLICIT);
            return;
        }
        String passwordString = passwordEdittext.getText().toString();
        if (passwordString.equals("")) {
            passwordEdittext.setFocusable(true);
            passwordEdittext.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(passwordEdittext,InputMethodManager.SHOW_IMPLICIT);
            return;
        } else if (passwordString.equals("123456")) {//just a demo
            if (!oldAccountName.equals(accountString)){
                SharePreferenceUtils.put(this.getApplicationContext(),"AccountName",accountString);
            }
            CheckBox checkBox = findViewById(R.id.cardWalletCheckbox);
            Intent intent = new Intent(this.getApplicationContext(),MainActivity.class);
            intent.putExtra("userName", accountString);
            if (checkBox.isChecked()) {
                intent.putExtra("coldWallet", "true");
            } else {
                intent.putExtra("coldWallet", "false");
            }
            startActivity(intent);
        }
    }
}
