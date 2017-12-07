package com.app_project.placebucket;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
        noView = (TextView) findViewById(R.id.bucket_no);
        nameView = (TextView) findViewById(R.id.bucket_name);
    }

    public void setBgImgView(int rsc) {
        //수정 필요. 지금은 R.drawable 값 받아서 세팅해주는 상태.

        imgView.setImageResource(rsc);
        imgView.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY);

    }

    public void setBnoView(String no) {
        noView.setText(no);
    }

    public void setBnameView(String name) {

        nameView.setText(name);
        nameView.setTextColor(Color.parseColor("#FFFFFF"));
    }

    public ImageView getImgView() {
        return imgView;
    }
}
