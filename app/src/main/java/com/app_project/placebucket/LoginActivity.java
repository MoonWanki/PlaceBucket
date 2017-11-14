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

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private final int REQUEST_CODE_MAIN = 100;
    CallbackManager callbackManager;
    LoginButton FBLoginButton;
    Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FBLoginButton = findViewById(R.id.fb_login_button);
        FBLoginButton.setReadPermissions(Arrays.asList("user_friends"));
        callbackManager = CallbackManager.Factory.create();


        // 이미 로그인 돼 있는 상태면 바로 메인액티비티로
        if(Profile.getCurrentProfile()!=null) {
            Toast.makeText(getApplicationContext(), "세션 있음", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivityForResult(intent, REQUEST_CODE_MAIN);
        }

        /**
        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {


                //Listen for changes to the profile or for a new profile: update your
                //user data, and launch the main activity afterwards. If my user has just logged in,
                //I make sure to update his information before launching the main Activity.

                Toast.makeText(getApplicationContext(), "FB Profile Changed\n"+ currentProfile.getId(), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivityForResult(intent, REQUEST_CODE_MAIN);
            }
        };
        profileTracker.startTracking();
         */

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            ProfileTracker profileTracker;
            @Override
            public void onSuccess(LoginResult loginResult) {

                profileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                        if(currentProfile!=null) {
                            Toast.makeText(getApplicationContext(), currentProfile.getName(), Toast.LENGTH_LONG).show();
                            profile = currentProfile;
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivityForResult(intent, REQUEST_CODE_MAIN);
                        }
                    }

                };

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

        if(requestCode==REQUEST_CODE_MAIN) {
            if(resultCode==RESULT_OK) {
                String flag = data.getStringExtra("value");
                if(flag.equals("exit"))
                    finish();
            }
        }
    }

}
