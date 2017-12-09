package com.app_project.placebucket;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;


public class SinglePlaceView extends RelativeLayout {

    private static final String TAG = "MyTag";

    GoogleApiClient mGoogleApiClient;


    ImageView imgView;
    TextView idView;
    TextView nameView;

    public SinglePlaceView(Context context) {
        super(context);
        init(context);
    }

    public SinglePlaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.single_place, this, true);

        imgView = (ImageView) findViewById(R.id.img_place);
        idView = (TextView) findViewById(R.id.place_id);
        nameView = (TextView) findViewById(R.id.place_name);
    }

    public void setImgView(GoogleApiClient googleApiClient, String id) {

        mGoogleApiClient = googleApiClient;

        new PhotoTask().execute(id);



    }

    public void setPidView(String id) {
        idView.setText(id);
    }

    public void setPnameView(String name) {

        nameView.setText(name);
        nameView.setTextColor(Color.parseColor("#FFFFFF"));
    }

    class PhotoTask extends AsyncTask<String,Void ,AttributedPhoto> {


        /**
         * Loads the first photo for a place id from the Geo Data API.
         * The place id must be the first (and only) parameter.
         */
        @Override
        protected void onPreExecute() {
            // Display a temporary image to show while bitmap is loading.
            imgView.setImageResource(R.drawable.placedefault);
            imgView.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY);

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
                    Bitmap image = photo.getPhoto(mGoogleApiClient).await()
                            .getBitmap();

                    image = scaleCenterCrop(image, 400, 1080);

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

                // Photo has been loaded, display it.
                imgView.setImageBitmap(attributedPhoto.bitmap);

                // Display the attribution as HTML content if set.
                if (attributedPhoto.attribution == null) {
                } else {
                    Log.d(TAG, attributedPhoto.attribution.toString());
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

    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }
}
