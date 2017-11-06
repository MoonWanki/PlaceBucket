package com.app_project.placebucket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;


public class AddBucketActivity extends AppCompatActivity {

    private String userId;

    JSONObject jsonResponse;
    JSONObject jsonFriends;
    JSONArray jsonFriendsData;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bucket);


        userId = AccessToken.getCurrentAccessToken().getUserId();

        TextView textView = findViewById(R.id.selectFriendsText);
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {

                        try {

                            jsonResponse = response.getJSONObject();

                            jsonFriendsData = jsonResponse.getJSONArray("data");



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
        ).executeAsync();
    }
}
