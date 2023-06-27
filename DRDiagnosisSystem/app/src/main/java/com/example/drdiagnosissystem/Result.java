package com.example.drdiagnosissystem;

import android.graphics.Bitmap;

import java.io.InputStream;

public class Result {
//    private String name;
//    private int age;
    private String image;
    private String stage;
    private String date;

    public Result(){

    }
    public Result(String image, String stage, String date) {
        this.image = image;
        this.stage = stage;
        this.date = date;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
