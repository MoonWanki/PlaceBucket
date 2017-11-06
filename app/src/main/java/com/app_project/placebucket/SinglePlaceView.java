package com.app_project.placebucket;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class SinglePlaceView extends RelativeLayout {

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

    public void setImgView(int rsc) {
        //수정 필요. 지금은 R.drawable 값 받아서 세팅해주는 상태.

        imgView.setImageResource(rsc);
        imgView.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY);

    }

    public void setPidView(String id) {
        idView.setText(id);
    }

    public void setPnameView(String name) {

        nameView.setText(name);
        nameView.setTextColor(Color.parseColor("#FFFFFF"));
    }
}
