package com.example.yunbianweather.gson;

import com.google.gson.annotations.SerializedName;

public class AQI {

    @SerializedName("air_now_city")
    public AirNow airNow;

    public String status;

    public class AirNow{
        public String aqi;

        public String pm25;

        @SerializedName("qlty")
        public String quality;
    }
}
