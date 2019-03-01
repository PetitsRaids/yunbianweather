package com.example.yunbianweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    public String date;

    @SerializedName("cond_txt_d")
    public String descriptionDay;

    @SerializedName("tmp_max")
    public String maxTmp;

    @SerializedName("tmp_min")
    public String minTmp;

    @SerializedName("wind_sc")
    public String wind;
}
