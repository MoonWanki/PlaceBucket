package com.app_project.placebucket;

import android.graphics.Bitmap;

public class SingleBucket {

    private String no;
    private String name;
    private Bitmap bitmap;

    public SingleBucket(String no, String name) {
        this.no = no;
        this.name = name;
    }

    public SingleBucket(String no, String name, Bitmap bitmap) {
        this.no = no;
        this.name = name;
        this.bitmap = bitmap;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getImage() { return bitmap; }

    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

}
