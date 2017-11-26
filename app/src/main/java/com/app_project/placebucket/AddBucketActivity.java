package com.app_project.placebucket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class AddBucketActivity extends AppCompatActivity {

    private static final String url_add_bucket = "http://18.216.36.241/pb/add_bucket.php";
    private static final String url_set_member = "http://18.216.36.241/pb/set_member.php";
    private static final String url_get_latest_bucket = "http://18.216.36.241/pb/get_latest_bucket.php";
    private String userId;

    JSONObject jsonResponse;
    JSONArray jsonFriendsDataArray;
    ImageView backButton;
    TextView choiceFinishedButton;
    ListView listView;
    EditText makeBucketName;

    InputMethodManager imm;

    ArrayList<HashMap<String, String>> mapArray;
    ArrayList<String> nameArray;
    ArrayList<String> idArray;
    ArrayAdapter adapter;

    private ProgressDialog pDialog;

    private static final String TAG = "MyTag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bucket);

        makeBucketName = findViewById(R.id.makeBucketName);
        mapArray = new ArrayList<>();
        nameArray = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, nameArray);
        userId = AccessToken.getCurrentAccessToken().getUserId();
        listView = findViewById(R.id.friendListView);
        listView.setAdapter(adapter);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        final TextView textView = findViewById(R.id.loadingFriendsText);
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                makeBucketName.clearFocus();
                hideKeyboard();
            }
        });

        choiceFinishedButton = findViewById(R.id.choiceFinishedButton);
        choiceFinishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(makeBucketName.getText().toString().length()==0) {
                    Snackbar.make(view, "버킷 이름을 입력해주세요!", Snackbar.LENGTH_SHORT).show();
                } else if (nameArray.size()==0) {
                    Toast.makeText(getApplicationContext(), "참여 멤버를 선택해주세요!", Toast.LENGTH_SHORT).show();
                } else {
                    hideKeyboard();

                    idArray = new ArrayList<>();


                    SparseBooleanArray checked = listView.getCheckedItemPositions();
                    if(listView.getCheckedItemCount()==0) return;

                    for(int i=0 ; i < nameArray.size() ; i++) {
                        if(checked.get(i)) {
                            idArray.add(mapArray.get(i).get("id"));
                        }
                    }

                    new AddBucket().execute(url_add_bucket + "?bname=" + makeBucketName.getText().toString());

                }
            }
        });

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {

                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {

                            jsonResponse = response.getJSONObject();
                            jsonFriendsDataArray = jsonResponse.getJSONArray("data");

                            String id, name;

                            for (int i = 0; i < jsonFriendsDataArray.length(); i++) {
                                JSONObject j = jsonFriendsDataArray.getJSONObject(i);
                                id = j.getString("id");
                                name = j.getString("name");

                                HashMap<String, String> map = new HashMap<>();
                                map.put("id", id);
                                map.put("name", name);

                                mapArray.add(map);
                                nameArray.add(name);
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if(mapArray.isEmpty()) {
                            textView.setText("친구가 없습니다.");
                        } else {
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }


                }


        ).executeAsync();

    }

    public void pushNewBucket(String id) throws Exception {
        Request request = new Request.Builder()
                .url("http://18.216.36.241/pb/push_new_bucket.php?id=" + id + "&bname=" + makeBucketName.getText().toString()).build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {

                ResponseBody responseBody = response.body();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                Log.d(TAG, responseBody.string());
            }
        });
    }

    class AddBucket extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(AddBucketActivity.this);
            pDialog.setMessage("버킷을 등록하는 중입니다...");
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
                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        new GetNewBucket().execute(url_get_latest_bucket);

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

    class GetNewBucket extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(AddBucketActivity.this);
            pDialog.setMessage("설정 내용을 적용하는 중입니다...");
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
            String bno="";

            if(result!=null) {

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    bno = jsonObject.getString(MainActivity.TAG_BNO);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
                Log.d(TAG, "JSON Object is null");

            String ids = "&id[]=" + Profile.getCurrentProfile().getId();

            /** push to me
            // try{ pushNewBucket(Profile.getCurrentProfile().getId()); } catch (Exception e) { e.printStackTrace(); }
            **/

            for(int i=0 ; i<idArray.size() ; i++) {

                ids += "&id[]=" + idArray.get(i);

                try{ pushNewBucket(idArray.get(i)); } catch (Exception e) { e.printStackTrace(); }
            }
            new AddMemberToBucket().execute(url_set_member + "?mode=add&bno=" + bno + ids);

        }
    }

    class AddMemberToBucket extends AsyncTask<String, Void, String> {

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
                        setResult(Activity.RESULT_OK);
                        finish();

                    } else if (success == -1) {
                        Log.d(TAG, jsonObject.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
                Log.d(TAG, "JSON Object is null");
        }
    }

    private void hideKeyboard()
    {
        imm.hideSoftInputFromWindow(makeBucketName.getWindowToken(), 0);
    }

}
