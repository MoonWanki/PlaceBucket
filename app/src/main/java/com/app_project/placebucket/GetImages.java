package com.app_project.placebucket;

/**
 * Created by androider on 2017-12-08.
 */
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GetImages implements Runnable {

    ArrayList<String> nos;
    ArrayList<Bitmap> images = new ArrayList<>();

    public ArrayList<Bitmap> getImages(){
        return images;
    }

    public Bitmap getBitmap(int index) {
        // return SinglePlaceView.scaleCenterCrop(images.get(index), 500, 1080);
        return images.get(index);
    }

    public void setNos(ArrayList<String> i){
        this.nos = i;
    }

    @Override
    public void run() {
        try {
            for(int i = 0 ; i < nos.size() ; i++) {

                String add = "http://18.216.36.241/pb/uploads/" + nos.get(i) + ".jpg";
                String def = "http://18.216.36.241/pb/uploads/default.jpg";
                InputStream is = null;
                URL url = null;
                url = new URL(add);
                try {
                    Log.e("mytag", "try");
                    images.add(BitmapFactory.decodeStream(url.openConnection().getInputStream()));
                }catch (FileNotFoundException e) {
                    url = new URL(def);
                    images.add(BitmapFactory.decodeStream(url.openConnection().getInputStream()));

                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
