package com.app_project.placebucket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.Profile;



public class AddBucketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bucket);

        String name = Profile.getCurrentProfile().getName();
        Toast.makeText(getApplicationContext(), "현재 사용자 이름: " + name, Toast.LENGTH_LONG).show();
    }
}
