package com.app_project.placebucket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


public class BucketActivity extends AppCompatActivity {

    private final int REQUEST_CODE_ADD_PLACE = 301;

    private String url_get_place = "http://18.216.36.241/pb/get_place.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PLACES = "places";
    private static final String TAG_PID = "Pid";
    private static final String TAG_PNAME = "Pname";

    private String bucketName;
    private String bucketNo;

    ImageView bucketImageView;
    TextView bucketNameTextView;
    TextView plzAddPlace;

    private ProgressDialog pDialog;

    private JSONObject jsonObject;
    private JSONArray jsonArray;

    private ArrayList<SinglePlace> placeArray = new ArrayList<>();

    ListView listView;
    PlaceListAdapter adapter;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket);

        bucketImageView = findViewById(R.id.bucket_title_img);
        bucketNameTextView = findViewById(R.id.bucket_title_name);
        plzAddPlace = findViewById(R.id.plzAddPlace);
        listView = findViewById(R.id.list_place);

        FloatingActionButton fab = findViewById(R.id.fab_bucket);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddPlaceActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_PLACE);
            }
        });

        backButton = findViewById(R.id.back_button_3);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        bucketNo = intent.getStringExtra(MainActivity.TAG_BNO);
        bucketName = intent.getStringExtra(MainActivity.TAG_BNAME);


        bucketImageView.setImageResource(intent.getIntExtra(MainActivity.TAG_IMG, 0));
        bucketImageView.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY);

        bucketNameTextView.setText(bucketName);

        url_get_place += "?bno=" + bucketNo;
        new LoadAllPlaces().execute(url_get_place);

    }

    class PlaceListAdapter extends BaseAdapter {

        ArrayList<SinglePlace> items;

        public void setList(ArrayList<SinglePlace> items) {
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
            SinglePlaceView view = new SinglePlaceView(getApplicationContext());
            SinglePlace item = items.get(i);
            view.setPidView(item.getId());
            view.setPnameView(item.getName());
            view.setImgView(item.getImgId());

            return view;
        }
    }

    class LoadAllPlaces extends AsyncTask<String, Void, String> {

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(BucketActivity.this);
            pDialog.setMessage("장소 목록을 불러오는 중입니다...");
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
                con.setDoInput(true);
                con.setDoOutput(true);
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

                if(jsonObject.getInt(TAG_SUCCESS)==0) {
                    plzAddPlace.setText("장소를 추가하세요.");
                    Toast.makeText(getApplicationContext(), jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                    return;
                }

                jsonArray = jsonObject.getJSONArray(TAG_PLACES);
                // updating UI from Background Thread

                for(int i=0 ; i<jsonArray.length() ; i++) {
                    JSONObject j = jsonArray.getJSONObject(i);
                    String pId = j.getString(TAG_PID);
                    String pName = j.getString(TAG_PNAME);

                    SinglePlace p = new SinglePlace(pId, pName);

                    placeArray.add(p);

                    placeArray.get(i).setImgId(R.drawable.placedefault);
                }


                adapter = new PlaceListAdapter();
                adapter.setList(placeArray);

                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Toast.makeText(getApplicationContext(), placeArray.get(i).getName() + " 입니다.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_ADD_PLACE && resultCode == Activity.RESULT_OK) {
            new LoadAllPlaces().execute(url_get_place);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
