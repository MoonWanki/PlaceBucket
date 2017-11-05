package com.app_project.placebucket;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.Profile;
import com.facebook.ProfileManager;
import com.facebook.login.LoginManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Attributes;

public class MainActivity extends AppCompatActivity {

    private final long FINISH_INTERVAL_TIME = 1500;
    private long backPressedTime = 0;

    TextView plzAddBucket;
    Button logoutButton;
    FloatingActionButton floatingActionButton;
    ListView listView;
    BucketListAdapter adapter;

    // Progress Dialog
    private ProgressDialog pDialog;

    JSONParser jParser = new JSONParser();

    JSONObject jsonObject;
    JSONArray jsonArray;

    ArrayList<SingleBucket> bucketArray = new ArrayList<>();

    // url to get all products list
    private static String url_get_buckets = "http://18.216.36.241/pb/get_bucket.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BUCKETS = "buckets";
    private static final String TAG_BNO = "Bno";
    private static final String TAG_BNAME = "Bname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        plzAddBucket = (TextView) findViewById(R.id.PlzAddBucket);
        logoutButton = (Button) findViewById(R.id.logoutButton);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        listView = (ListView) findViewById(R.id.list_bucket);

        /**
         * Profile.fetchProfileForCurrentAccessToken();
         * String name = Profile.getCurrentProfile().getName();
         * Toast.makeText(getApplicationContext(), "이름: " + name, Toast.LENGTH_LONG).show();
         * welcome.setText(currentProfile.getName().toString() + "님 환영합니다!");
         * */

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewLogoutDialog();
            }
        });

        // Floating Button - go to AddBucketActivity
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddBucketActivity.class);
                startActivityForResult(intent, 101);
            }
        });

        new LoadAllBuckets().execute(url_get_buckets);

    }

    class BucketListAdapter extends BaseAdapter {

        ArrayList<SingleBucket> items;

        public void setList(ArrayList<SingleBucket> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View v, ViewGroup viewGroup) {
            SingleBucketView view = new SingleBucketView(getApplicationContext());
            SingleBucket item = items.get(i);
            view.setBnoView(item.getNo());
            view.setBnameView(item.getName());
            view.setBgImgView(item.getImgId());

            return view;
        }
    }


    class LoadAllBuckets extends AsyncTask<String, Void, String> {

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading buckets. Plz wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // getting All products from url
        protected String doInBackground(String... params) {

            String uri = params[0];
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

        protected void onPostExecute(String result) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();


            try {
                jsonObject = new JSONObject(result);

                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

                if(jsonObject.getInt(TAG_SUCCESS)==0) {
                    plzAddBucket.setText("버킷을 추가하세요.");
                    return;
                }

                jsonArray = jsonObject.getJSONArray(TAG_BUCKETS);
                // updating UI from Background Thread

                for(int i=0 ; i<jsonArray.length() ; i++) {
                    JSONObject j = jsonArray.getJSONObject(i);
                    String bno = j.getString(TAG_BNO);
                    String bname = j.getString(TAG_BNAME);

                    SingleBucket b = new SingleBucket(bno, bname);

                    if(i%4==0) b.setImgId(R.drawable.img1);
                    else if(i%4==1) b.setImgId(R.drawable.img2);
                    else if(i%4==2) b.setImgId(R.drawable.img3);
                    else b.setImgId(R.drawable.img4);

                    bucketArray.add(b);
                }

                adapter = new BucketListAdapter();
                adapter.setList(bucketArray);

                listView.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }



    protected void viewLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("정말 로그아웃 하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LoginManager.getInstance().logOut();
                finish();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==101) {

        }
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            Intent intent = new Intent();
            String strFlag = "exit";
            intent.putExtra("value", strFlag);
            setResult(RESULT_OK, intent);
            super.onBackPressed();
        }
        else
        {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
