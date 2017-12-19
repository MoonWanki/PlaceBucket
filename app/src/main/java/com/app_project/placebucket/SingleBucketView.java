package com.app_project.placebucket;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class SingleBucketView extends RelativeLayout {

    ImageView imgView;
    TextView noView;
    TextView nameView;

    public SingleBucketView(Context context) {
        super(context);
        init(context);
    }

    public SingleBucketView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.single_bucket, this, true);

        imgView = (ImageView) findViewById(R.id.img_bucket);
        // noView = (TextView) findViewById(R.id.bucket_no);
        nameView = (TextView) findViewById(R.id.bucket_name);
    }

    public void setBgImgView(Bitmap bitmap) {
        //수정 필요. 지금은 R.drawable 값 받아서 세팅해주는 상태.


        imgView.setImageBitmap(scaleCenterCrop(bitmap, 520, 1080));
        imgView.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY);
    }


    public void setBnoView(String no) {
        // noView.setText(no);
    }

    public void setBnameView(String name) {

        nameView.setText(name);
        nameView.setTextColor(Color.parseColor("#FFFFFF"));
    }

    public ImageView getImgView() {
        return imgView;
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
