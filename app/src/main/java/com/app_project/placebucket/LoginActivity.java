package com.app_project.placebucket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final String url_check_user = "http://18.216.36.241/pb/check_user.php";
    private static final String url_add_user = "http://18.216.36.241/pb/add_user.php";

    private final int REQUEST_CODE_MAIN = 100;
    CallbackManager callbackManager;
    LoginButton FBLoginButton;


    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FBLoginButton = findViewById(R.id.fb_login_button);
        FBLoginButton.setReadPermissions(Arrays.asList("user_friends"));
        callbackManager = CallbackManager.Factory.create();


        // 이미 로그인 돼 있는 상태면 바로 메인액티비티로
        if(Profile.getCurrentProfile()!=null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivityForResult(intent, REQUEST_CODE_MAIN);
        }


        new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if(oldProfile==null && currentProfile!=null) {

                    new CheckUser().execute(url_check_user + "?id=" + currentProfile.getId());

                }
            }

        };

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {}

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class CheckUser extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("유저 정보를 확인 중입니다...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String uri = strings[0];
            BufferedReader bufferedReader = null;


            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                StringBuilder sb = new StringBuilder();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String json;
                while((json=bufferedReader.readLine())!=null) {
                    sb.append(json + "\n");
                }

                return sb.toString().trim();

            } catch(Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            pDialog.dismiss();

            if(result!=null) {

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int success = jsonObject.getInt(MainActivity.TAG_SUCCESS);

                    if (success == 1) {
                        // Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_MAIN);

                    } else if (success == 0) {
                        // Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();

                        new AddUser().execute(url_add_user + "?id=" + Profile.getCurrentProfile().getId() + "&name=" + Profile.getCurrentProfile().getName());
                        // Toast.makeText(getApplicationContext(),Profile.getCurrentProfile().getId(), Toast.LENGTH_LONG).show();


                    } else if (success == -1) {
                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(getApplicationContext(), "JSON response is null.", Toast.LENGTH_SHORT).show();
        }
    }

    class AddUser extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("유저 정보를 등록중입니다...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String uri = strings[0];
            BufferedReader bufferedReader = null;


            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                StringBuilder sb = new StringBuilder();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String json;
                while((json=bufferedReader.readLine())!=null) {
                    sb.append(json + "\n");
                }

                return sb.toString().trim();

            } catch(Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            pDialog.dismiss();

            if(result!=null) {

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int success = jsonObject.getInt(MainActivity.TAG_SUCCESS);

                    if (success == 1) {
                        // Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_MAIN);

                    } else if (success == 0) {
                        Toast.makeText(getApplicationContext(), "registration failed", Toast.LENGTH_LONG).show();

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(getApplicationContext(), "JSON response is null.", Toast.LENGTH_SHORT).show();
        }
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
