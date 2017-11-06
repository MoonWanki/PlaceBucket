package com.app_project.placebucket;


public class SinglePlace {

    private String id;
    private String name;
    private String address;
    private int imgId;

    public SinglePlace (String id, String name) {
        this.id = id;
        this.name = name;
    }

    public SinglePlace(String id, String name, int imgId) {
        this.id = id;
        this.name = name;
        this.imgId = imgId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public int getImgId() { return imgId; }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }
}
