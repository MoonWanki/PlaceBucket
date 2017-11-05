package com.app_project.placebucket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class LoginActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    LoginButton FBLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FBLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
        callbackManager = CallbackManager.Factory.create();

        // 이미 로그인 돼 있는 상태면 바로 메인액티비티로
        if(AccessToken.getCurrentAccessToken()!=null) {
            // Toast.makeText(getApplicationContext(), "자동 로그인.", Toast.LENGTH_SHORT).show();
            // toastLoginInfo();
            startMainActivity();
        }

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // toastLoginInfo();
                        startMainActivity();
                    }

                    @Override
                    public void onCancel() {}

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if(requestCode==101) {
            if(resultCode==RESULT_OK) {
                String flag = data.getStringExtra("value");
                if(flag.equals("exit"))
                    finish();
            }
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(intent, 101);
    }

    protected void toastLoginInfo() {
        Toast.makeText(getApplicationContext(), "엑세스 토큰:\n" + AccessToken.getCurrentAccessToken().toString(), Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),
                "Facebook ID:\n" + AccessToken.getCurrentAccessToken().getUserId().toString(),
                Toast.LENGTH_SHORT).show();
    }

}
