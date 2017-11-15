package com.app_project.placebucket;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE_BUCKET = 201;
    private final int REQUEST_CODE_ADD_BUCKET = 202;

    private final long FINISH_INTERVAL_TIME = 1500;
    private long backPressedTime = 0;

    TextView plzAddBucket;
    Button logoutButton;
    FloatingActionButton floatingActionButton;
    ListView listView;
    BucketListAdapter adapter;

    // Progress Dialog
    private ProgressDialog pDialog;

    private ArrayList<SingleBucket> bucketArray = new ArrayList<>();

    private static final String url_get_bucket = "http://18.216.36.241/pb/get_bucket.php";

    // JSON Node names
    protected static final String TAG_SUCCESS = "success";
    protected static final String TAG_BUCKETS = "buckets";
    protected static final String TAG_BNO = "Bno";
    protected static final String TAG_BNAME = "Bname";
    protected static final String TAG_IMG = "img";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        plzAddBucket = findViewById(R.id.plzAddBucket);
        logoutButton = findViewById(R.id.logoutButton);
        floatingActionButton = findViewById(R.id.fab_main);
        listView = findViewById(R.id.list_bucket);

        String name = Profile.getCurrentProfile().getName();
        String id = Profile.getCurrentProfile().getId();
        Toast.makeText(getApplicationContext(), "이름: " + name + "\nID: " + id, Toast.LENGTH_LONG).show();

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
                startActivityForResult(intent, REQUEST_CODE_ADD_BUCKET);
            }
        });

        // new GetUser().execute(url_get_user);

        new LoadAllBuckets().execute(url_get_bucket + "?uid=" + Profile.getCurrentProfile().getId());

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
            pDialog.setMessage("버킷 목록을 불러오는 중입니다...");
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

            pDialog.dismiss();

            if(result!=null) {

                try {

                    JSONObject jsonObject = new JSONObject(result);

                    int success = jsonObject.getInt(TAG_SUCCESS);

                    if (success == 0) {
                        plzAddBucket.setText("버킷을 추가하세요.");
                        return;
                    } else if(success == 1) {

                        JSONArray jsonArray = jsonObject.getJSONArray(TAG_BUCKETS);
                        // updating UI from Background Thread

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject j = jsonArray.getJSONObject(i);
                            String bno = j.getString(TAG_BNO);
                            String bname = j.getString(TAG_BNAME);

                            SingleBucket b = new SingleBucket(bno, bname);

                            bucketArray.add(b);
                        }

                        bucketArray.get(0).setImgId(R.drawable.img1);
                        bucketArray.get(1).setImgId(R.drawable.img2);
                        bucketArray.get(2).setImgId(R.drawable.img3);
                        bucketArray.get(3).setImgId(R.drawable.img4);

                        adapter = new BucketListAdapter();
                        adapter.setList(bucketArray);


                        listView.setAdapter(adapter);

                        pDialog.dismiss();

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                SingleBucket b = (SingleBucket) adapter.getItem(i);
                                Intent intent = new Intent(getApplicationContext(), BucketActivity.class);
                                intent.putExtra(TAG_BNO, b.getNo());
                                intent.putExtra(TAG_BNAME, b.getName());
                                intent.putExtra(TAG_IMG, b.getImgId());
                                startActivityForResult(intent, REQUEST_CODE_BUCKET);
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "JSON response is null.", Toast.LENGTH_SHORT).show();
            }

        }

    }


    class GetUser extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            return null;
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

        if(requestCode==REQUEST_CODE_BUCKET) {

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
