package com.example.yunbianweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.yunbianweather.gson.AQI;
import com.example.yunbianweather.gson.Weather;
import com.example.yunbianweather.util.HttpUtil;
import com.example.yunbianweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherText = sharedPreferences.getString("weather_id", null);
        if (weatherText != null) {
            Weather weather = Utility.handleWeatherResponse(weatherText);
            String weatherId = weather.basic.cId;
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(AutoUpdateService.this).edit();

            String weatherUrl = "https://free-api.heweather.net/s6/weather?location=" +
                    weatherId + "&key=34fcb36bcc8a42d2b0fe9b549cce8f8c";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather1 = Utility.handleWeatherResponse(responseText);
                    if (weather1 != null && weather1.status.equals("ok")) {
                        editor.putString("weather_id", responseText);
                    }
                }
            });
            weatherUrl = "https://free-api.heweather.net/s6/air/now?location=" +
                    weatherId + "&key=34fcb36bcc8a42d2b0fe9b549cce8f8c";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String aqiText = response.body().string();
                    AQI aqi = Utility.handleAQIResponse(aqiText);
                    if (aqi != null && aqi.status.equals("ok")) {
                        editor.putString("aqi", aqiText);
                    }
                }
            });
            String address = "http://guolin.tech/api/bing_pic";
            HttpUtil.sendOkHttpRequest(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String bingAddress = response.body().string();
                    if (!bingAddress.equals("")) {
                        editor.putString("bing_pic", bingAddress);
                        editor.apply();
                    }
                }
            });
        }
    }
}
