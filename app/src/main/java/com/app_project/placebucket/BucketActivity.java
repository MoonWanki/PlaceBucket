package com.app_project.placebucket;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class BucketActivity extends AppCompatActivity implements OnConnectionFailedListener {

    private static final int PICK_FROM_ALBUM = 400;
    private final int REQUEST_CODE_ADD_PLACE = 301;
    private static final int REQUEST_PLACE_PICKER = 300;

    String upLoadServerUri = "http://18.216.36.241/pb/upload.php";

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
    ImageView settingButton;

    GoogleApiClient mGoogleApiClient;
    int serverResponseCode = 0;

    Boolean aBoolean;

    class SetBucketTitleImg extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... voids) {

            Bitmap b = null;

            try {
                Log.i("mytag", "접속");
                String add = "http://18.216.36.241/pb/uploads/" + bucketNo + ".jpg";
                String def = "http://18.216.36.241/pb/uploads/default.jpg";
                URL url = null;
                url = new URL(add);
                try {
                    b = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                }catch (FileNotFoundException e) {
                    url = new URL(def);
                    b = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return b;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            bucketImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket);

        bucketImageView = findViewById(R.id.bucket_title_img);

        bucketNameTextView = findViewById(R.id.bucket_title_name);
        plzAddPlace = findViewById(R.id.plzAddPlace);
        listView = findViewById(R.id.list_place);
        settingButton = findViewById(R.id.set_bucket);

        aBoolean = false;

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewBucketSettingDialog();
            }
        });


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

        new SetBucketTitleImg().execute();

        new LoadAllPlaces().execute(url_get_place + "?bno=" + bucketNo);

    }

    /* public static void setImage(final ImageView imageView, String id) {

        class GetImage extends AsyncTask<String, Void, Bitmap> {

            @Override
            protected Bitmap doInBackground(String... params) {
                String id = params[0];
                String add = "http://18.216.36.241/pb/uploads/" + id + ".jpg";
                URL url = null;
                Bitmap image = null;
                try {
                    url = new URL(add);
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return image;
            }

            @Override
            protected void onPostExecute(Bitmap b) {
                super.onPostExecute(b);
                imageView.setImageBitmap(b);
            }
        }

        GetImage gi = new GetImage();
        gi.execute(id);
    }*/

    protected void viewBucketSettingDialog() {
        CharSequence menu[] = new CharSequence[] {"배경 사진 설정", "버킷 이름 편집", "멤버 초대"};

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(bucketName);
        builder.setItems(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch(which) {
                    case 0:
                        checkPermissionToAccessAlbum();
                        break;
                    case 1:
                        viewBucketNameEditor();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    private void viewBucketNameEditor() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        View mView = layoutInflaterAndroid.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(mView);

        final EditText userInputDialogEditText = mView.findViewById(R.id.userInputDialog);
        userInputDialogEditText.setText(bucketName);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("완료", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        updateBucketName(userInputDialogEditText.getText().toString());
                    }
                })

                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
    }

    private void updateBucketName(final String name) {

        Request request = new Request.Builder()
                .url("http://18.216.36.241/pb/set_bucket_name.php?name=" + name
                        + "&bno=" + bucketNo).build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {

                ResponseBody responseBody = response.body();

                try {
                    JSONObject result = new JSONObject(responseBody.string());
                    if(result.getInt("success")==1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bucketNameTextView.setText(name);
                            }
                        });
                    }

                } catch (JSONException e) { e.printStackTrace(); }


            }
        });

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

                try {
                    addPlace(pid, pname, paddress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // new AddPlaceActivity.CheckPlace().execute(url_check_place + "?pid=" + id+ "&bno=" + getIntent().getStringExtra("bno"));

                // new LoadAllPlaces().execute(url_get_place + "?bno=" + bucketNo);
            }
        } else if (requestCode == PICK_FROM_ALBUM) {
            Uri uri = data.getData();
            Log.d("mytag", getRealPathFromURI(uri));

            new uploadFile().execute(getRealPathFromURI(uri));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void checkPermissionToAccessAlbum() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            doTakeAlbumAction();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "승인됨", Toast.LENGTH_LONG).show();
                    doTakeAlbumAction();
                } else {
                    Toast.makeText(this, "거부됨", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }    }

    public String getRealPathFromURI(Uri contentUri) {
        int column_index = 0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }
        return cursor.getString(column_index);
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
                          //  try{ pushNewPlace(Profile.getCurrentProfile().getId(), pname); } catch (Exception e) { e.printStackTrace(); }

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

    public void doTakeAlbumAction() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    public class uploadFile extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!pDialog.isShowing()) {
                pDialog = new ProgressDialog(BucketActivity.this);
                pDialog.setMessage("사진 업로드 중입니다...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {

            String fileName = strings[0];

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(strings[0]);

            if (!sourceFile.isFile()) {
                Log.e("uploadFile", "Source File not exist :");
                return null;
            } else {

                try {
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(upLoadServerUri);

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + bucketNo + ".jpg" + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("mytag", "HTTP Respdonse is : "
                            + serverResponseMessage + ": " + serverResponseCode);

                    if(serverResponseCode == 200){
                        Log.e("mytag", "success " + fileName);
                    }

                    //close the streams
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (Exception e) {

                    e.printStackTrace();
                    Log.e("mytag", "Exception : "+ e.getMessage(), e);
                }

            } // End else block

            return String.valueOf(serverResponseCode);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            pDialog.dismiss();

            new SetBucketTitleImg().execute();
        }
    }
}
