package com.app_project.placebucket;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.GoogleMap;

import android.support.v4.app.FragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class AddPlaceActivity extends AppCompatActivity implements OnConnectionFailedListener{
    ImageView mImageView;
    ImageView backButton;
    TextView mText1;
    TextView mText2;
    public GoogleApiClient mGoogleApiClient;
    public GoogleMap mMap;
    private ProgressDialog pDialog;
    private static final String url_check_place = "http://18.216.36.241/pb/check_place.php";
    private static final String url_add_place = "http://18.216.36.241/pb/add_place.php";
    private static final int REQUEST_PLACE_PICKER = 1;
    private static  Place place ;
    private static  String id ;
    private static  CharSequence name;
    private static CharSequence address ;
    private static CharSequence attributions ;

   // TextView mViewName;
    //TextView mViewAddress;
    //TextView mViewAttributions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mText1 = findViewById(R.id.textview1);
        mText2 = findViewById(R.id.textview2);
        mImageView = findViewById(R.id.imageView2);
        backButton = findViewById(R.id.back_button_2);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });



        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            startActivityForResult(builder.build(this), REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == REQUEST_PLACE_PICKER) {

            if (resultCode == Activity.RESULT_OK) {
                // The user has selected a place. Extract the name and address.
                place = PlacePicker.getPlace(data, this);
                id = place.getId();
                name = place.getName();
                address = place.getAddress();
               attributions = place.getAttributions();

                if (attributions != null) {
                    //mViewAttributions.setText(attributions);
                }
                new CheckPlace().execute(url_check_place + "?pid=" + id+ "&bno=" + getIntent().getStringExtra("bno"));
            } else if (resultCode == Activity.RESULT_CANCELED) {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(AddPlaceActivity.this);
                alert_confirm.setMessage("그만할꺼?").setCancelable(false).setPositiveButton("ㅇㅇ",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();  // 'YES'
                            }
                        }).setNegativeButton("ㄴㄴ",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                recreate();// 'No'
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();


            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    class CheckPlace extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(AddPlaceActivity.this);
            pDialog.setMessage("장소 정보를 확인 중입니다...");
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


                    } else if (success == 0) {
                         Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), url_add_place + "?pid=" + id + "&bno=" + getIntent().getStringExtra("bno") + "&pname="+name +"&paddress="+address, Toast.LENGTH_LONG).show();
                        new AddPlace().execute(url_add_place + "?pid=" + id + "&bno=" + getIntent().getStringExtra("bno") + "&pname="+name +"&paddress="+address);
                        //Toast.makeText(getApplicationContext(),Profile.getCurrentProfile().getId(), Toast.LENGTH_LONG).show();


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

    class AddPlace extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(AddPlaceActivity.this);
            pDialog.setMessage(" 장소를 등록 중입니다...");
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
                            new PhotoTask(500,500).execute(id);
                            setResult(Activity.RESULT_OK);
                            //finish();

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

    class PhotoTask extends AsyncTask<String,Void ,AttributedPhoto> {

        private int mHeight;

        private int mWidth;

        public PhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }

        /**
         * Loads the first photo for a place id from the Geo Data API.
         * The place id must be the first (and only) parameter.
         */
        @Override
        protected void onPreExecute() {
            // Display a temporary image to show while bitmap is loading.
            mImageView.setImageResource(R.drawable.bucketdefault);
        }

        @Override
        protected AttributedPhoto doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            AttributedPhoto attributedPhoto = null;

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mGoogleApiClient, placeId).await();

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                if (result.getPhotoMetadata().getCount() > 0 && !isCancelled()) {
                    // Get the first bitmap and its attributions.
                    PlacePhotoMetadata photo = result.getPhotoMetadata().get(0);
                    CharSequence attribution = photo.getAttributions();
                    // Load a scaled bitmap for this photo.
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                            .getBitmap();

                    attributedPhoto = new AttributedPhoto(attribution, image);
                }
                // Release the PlacePhotoMetadataBuffer.
                photoMetadataBuffer.release();
            }
            return attributedPhoto;
        }

                // Create a new AsyncTask that displays the bitmap and attribution once loaded.

                    @Override
                    protected void onPostExecute(AttributedPhoto attributedPhoto) {
                        if (attributedPhoto != null) {
                            Toast.makeText(getApplicationContext(), "이미지 로드 됨", Toast.LENGTH_SHORT).show();

                            // Photo has been loaded, display it.
                            mImageView.setImageBitmap(attributedPhoto.bitmap);
                            Toast.makeText(getApplicationContext(), attributedPhoto.attribution, Toast.LENGTH_SHORT).show();
                            // Display the attribution as HTML content if set.
                            if (attributedPhoto.attribution == null) {
                                mText1.setVisibility(View.GONE);
                            } else {
                                mText1.setVisibility(View.VISIBLE);
                                mText1.setText(Html.fromHtml(attributedPhoto.attribution.toString()));
                            }

                        }
                    }
                }
        /**
         * Holder for an image and its attribution.
         */
        class AttributedPhoto {

            public final CharSequence attribution;

            public final Bitmap bitmap;

            public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
                this.attribution = attribution;
                this.bitmap = bitmap;
            }
        }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
    }
}
