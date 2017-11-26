package com.app_project.placebucket;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class BucketActivity extends AppCompatActivity implements OnConnectionFailedListener {

    private final int REQUEST_CODE_ADD_PLACE = 301;
    private static final int REQUEST_PLACE_PICKER = 300;


    private static final String url_get_place = "http://18.216.36.241/pb/get_place.php";
    private String url_del_place = "http://18.216.36.241/pb/del_place.php";
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

    private ArrayList<SinglePlace> placeArray;

    ListView listView;
    PlaceListAdapter adapter;
    ImageView backButton;

    GoogleApiClient mGoogleApiClient;

    Boolean aBoolean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket);

        bucketImageView = findViewById(R.id.bucket_title_img);
        bucketNameTextView = findViewById(R.id.bucket_title_name);
        plzAddPlace = findViewById(R.id.plzAddPlace);
        listView = findViewById(R.id.list_place);

        aBoolean = false;

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();


        FloatingActionButton fab = findViewById(R.id.fab_bucket);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**
                Intent intent = new Intent(getApplicationContext(), AddPlaceActivity.class);
                intent.putExtra("bno", bucketNo);
                startActivityForResult(intent, REQUEST_CODE_ADD_PLACE);
                 **/
                startPlacePicker();
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

        new LoadAllPlaces().execute(url_get_place + "?bno=" + bucketNo);

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


            view.setImgView(mGoogleApiClient, item.getId());

            return view;
        }
    }

    private void startPlacePicker() {

        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            startActivityForResult(builder.build(this), REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    class LoadAllPlaces extends AsyncTask<String, Void, String> {

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(!aBoolean) {
                pDialog = new ProgressDialog(BucketActivity.this);
                pDialog.setMessage("장소 목록을 불러오는 중입니다...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
                aBoolean = true;
            }
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
                StringBuilder sb = new   StringBuilder();

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

            if(pDialog.isShowing()) {
                pDialog.dismiss();
            }

            try {

                placeArray = new ArrayList<>();
                jsonObject = new JSONObject(result);

                if(jsonObject.getInt(TAG_SUCCESS)==0) {
                    plzAddPlace.setText("장소를 추가하세요.");
                    Toast.makeText(getApplicationContext(), jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                    return;
                }

                plzAddPlace.setText("");

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
                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(final AdapterView<?> adapterView, View view, int i, long l) {
                        viewPlaceMenuDialog(i);
                        return true;
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }


    protected  void viewPlaceMenuDialog(final int position) {
        CharSequence menu[] = new CharSequence[] {"장소를 삭제하시겠습니까?"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(placeArray.get(position).getName());
        builder.setItems(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch(which) {
                    case 0:
                        new DelPlace().execute(url_del_place + "?pid=" + placeArray.get(position).getId()+ "&bno=" + bucketNo);
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    class DelPlace extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(BucketActivity.this);
            pDialog.setMessage("장소를 삭제중입니다...");
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
                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        recreate();


                    } else if (success == 0) {
                        // Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_ADD_PLACE) {
            if(resultCode == Activity.RESULT_OK) {
                new LoadAllPlaces().execute(url_get_place + "?bno=" + bucketNo);
            }
        } else if(requestCode == REQUEST_PLACE_PICKER) {
            if(resultCode == Activity.RESULT_OK) {

                Place place = PlacePicker.getPlace(data, this);
                String pid = place.getId();
                CharSequence pname = place.getName();
                CharSequence paddress = place.getAddress();

                try { addPlace(pid, pname, paddress); } catch (Exception e) { e.printStackTrace(); }
                // new AddPlaceActivity.CheckPlace().execute(url_check_place + "?pid=" + id+ "&bno=" + getIntent().getStringExtra("bno"));

                // new LoadAllPlaces().execute(url_get_place + "?bno=" + bucketNo);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void addPlace(String pid, final CharSequence pname, CharSequence paddress) throws Exception {

        Request request = new Request.Builder()
                .url("http://18.216.36.241/pb/test.php?pid=" + pid
                        + "&pname=" + pname
                        + "&paddress=" + paddress
                        + "&bno=" + bucketNo).build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {

                ResponseBody responseBody = response.body();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                try {
                    JSONObject result = new JSONObject(responseBody.string());
                    int success = result.getInt("success");

                    switch (success) {
                        case 1:
                            try{ pushNewPlace(Profile.getCurrentProfile().getId(), pname); } catch (Exception e) { e.printStackTrace(); }

                            new LoadAllPlaces().execute(url_get_place + "?bno=" + bucketNo);
                            break;
                        case 0:
                            BucketActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "이미 추가된 장소입니다.", Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case -1:
                            break;
                        default:
                            break;
                    }

                } catch (JSONException e) { e.printStackTrace(); }


            }
        });
    }

    public void pushNewPlace(String id, CharSequence pname) throws Exception {
        Request request = new Request.Builder()
                .url("http://18.216.36.241/pb/send_fcm.php?id=" + id
                + "&title=" + pname
                + "&text=" + bucketName + " 버킷에 새로운 장소가 추가되었습니다.").build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {

                ResponseBody responseBody = response.body();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
