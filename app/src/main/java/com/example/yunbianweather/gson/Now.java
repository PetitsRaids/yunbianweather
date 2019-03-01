package com.example.yunbianweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("cond_txt")
    public String weatherNow;

    @SerializedName("tmp")
    public String temperatureNow;

    @SerializedName("hum")
    public String relativeHumidity;

    @SerializedName("vis")
    public String visibility;
}
