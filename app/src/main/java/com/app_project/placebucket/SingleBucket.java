package com.app_project.placebucket;

public class SingleBucket {

    private String no;
    private String name;
    private int imgId;

    public SingleBucket(String no, String name) {
        this.no = no;
        this.name = name;
    }

    public SingleBucket(String no, String name, int imgId) {
        this.no = no;
        this.name = name;
        this.imgId = imgId;
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

    public int getImgId() { return imgId; }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

}
