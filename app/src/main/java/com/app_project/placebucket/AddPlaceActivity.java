package com.app_project.placebucket;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

public class AddPlaceActivity extends AppCompatActivity implements OnConnectionFailedListener{

    ImageView backButton;
    public GoogleApiClient mGoogleApiClient;

    private static final int REQUEST_PLACE_PICKER = 1;

    TextView mViewName;
    TextView mViewAddress;
    TextView mViewAttributions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        mViewName = findViewById(R.id.mViewName);
        mViewAddress = findViewById(R.id.mViewAddress);
        mViewAttributions = findViewById(R.id.mViewAttributions);

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
                final Place place = PlacePicker.getPlace(data, this);
                final String id = place.getId();
                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                final CharSequence attributions = place.getAttributions();

                if (attributions != null) {
                    mViewAttributions.setText(attributions);
                }

                mViewName.setText(name);
                mViewAddress.setText(address);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish(); // 틈새 보임 ㅅㅂ
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
    }
}
