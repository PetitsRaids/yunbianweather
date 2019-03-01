package com.example.yunbianweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestions {

    @SerializedName("brf")
    public String liveIndex;

    @SerializedName("txt")
    public String description;

    public String type;
}
