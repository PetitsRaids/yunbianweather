package com.example.yunbianweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yunbianweather.gson.AQI;
import com.example.yunbianweather.gson.Forecast;
import com.example.yunbianweather.gson.Suggestions;
import com.example.yunbianweather.gson.Weather;
import com.example.yunbianweather.util.HttpUtil;
import com.example.yunbianweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ImageView background;

    private ScrollView scrollView;

    private TextView titleCity, titleUpdateTime;

    private TextView degreeNow, weatherInfoNow;

    private LinearLayout forecastLayout, suggestionLayout;

    private TextView airQualityText, aqiText, pm25Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        background = findViewById(R.id.background);
        scrollView = findViewById(R.id.scroll_view);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.update_time);
        degreeNow = findViewById(R.id.temperature_now);
        weatherInfoNow = findViewById(R.id.weather_info);
        forecastLayout = findViewById(R.id.forecast_layout);
        suggestionLayout = findViewById(R.id.suggestion_layout);
        airQualityText = findViewById(R.id.air_quality_text);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm_text);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        String weatherInfo = sharedPreferences.getString("weather", null);
        String aqiInfo = sharedPreferences.getString("aqi", null);
        String bingPic = sharedPreferences.getString("bing_pic", null);
        if (weatherInfo == null){
            scrollView.setVisibility(View.INVISIBLE);
            requestWeather(getIntent().getStringExtra("weather_id"));
        }else {
            Weather weather = Utility.handleWeatherResponse(weatherInfo);
            showWeatherInfo(weather);
        }
        if (aqiInfo == null){
            requestAQI(getIntent().getStringExtra("weather_id"));
        }else{
            AQI aqi = Utility.handleAQIResponse(aqiInfo);
            showAqiInfo(aqi);
        }
        if (bingPic == null){
            requestBingPicture();
        }else{
            Glide.with(this).load(bingPic).into(background);
        }
    }

    private void requestWeather(String weatherId){
        String address = "https://free-api.heweather.net/s6/weather?location=" +
                weatherId + "&key=34fcb36bcc8a42d2b0fe9b549cce8f8c";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(()->
                    Toast.makeText(WeatherActivity.this,
                            "天气数据获取失败", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseTest = response.body().string();
                Weather weather = Utility.handleWeatherResponse(responseTest);
                runOnUiThread(()->{
                    if (weather != null && weather.status.equals("ok")){
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather", responseTest);
                        editor.apply();
                        showWeatherInfo(weather);
                    }else{
                        Toast.makeText(WeatherActivity.this, "天气数据获取失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void requestAQI(String weatherId){
        String address = "https://free-api.heweather.net/s6/air/now?location="
                + weatherId + "&key=34fcb36bcc8a42d2b0fe9b549cce8f8c";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(()->
                    Toast.makeText(WeatherActivity.this, "空气质量信息获取失败", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                AQI aqi = Utility.handleAQIResponse(responseText);
                runOnUiThread(()->{
                    if (aqi != null && aqi.status.equals("ok")){
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("aqi", responseText);
                        editor.apply();
                        showAqiInfo(aqi);
                    }else {
                        Toast.makeText(WeatherActivity.this, "空气质量获取失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void requestBingPicture(){
        String address = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(()->
                    Toast.makeText(WeatherActivity.this,
                            "必应图片获取失败！", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", responseText);
                editor.apply();
                runOnUiThread(()->{
                    Glide.with(WeatherActivity.this).load(responseText).into(background);
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime;
        String degree = weather.now.temperatureNow;
        String weatherInfo = weather.now.weatherNow;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeNow.setText(degree);
        weatherInfoNow.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dataText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxTmp = view.findViewById(R.id.max_text);
            TextView minTmp = view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            infoText.setText(forecast.descriptionDay);
            maxTmp.setText(forecast.maxTmp);
            minTmp.setText(forecast.minTmp);
            forecastLayout.addView(view);
        }
        suggestionLayout.removeAllViews();
        for (Suggestions suggestion : weather.suggestions){
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.suggestion_item, suggestionLayout, false);
            TextView exponentType = view.findViewById(R.id.suggestion_type);
            TextView descriptionView = view.findViewById(R.id.suggestion_description);
            String exponent = lifestyleJudge(suggestion.type) + "：" + suggestion.liveIndex;
            exponentType.setText(exponent);
            descriptionView.setText(suggestion.drescription);
            suggestionLayout.addView(view);
        }
        scrollView.setVisibility(View.VISIBLE);
    }

    private void showAqiInfo(AQI aqi){
        String airQuality = aqi.airNow.quality;
        String aqiIndex = aqi.airNow.aqi;
        String pm25 = aqi.airNow.pm25;
        airQualityText.setText(airQuality);
        aqiText.setText(aqiIndex);
        pm25Text.setText(pm25);
    }

    private String lifestyleJudge(String code) {
        switch (code) {
            case "comf":
                return "舒适度指数";
            case "drsg":
                return "穿衣指数";
            case "cw":
                return "洗车指数";
            case "flu":
                return "感冒指数";
            case "sport":
                return "运动指数";
            case "trav":
                return "旅游指数";
            case "uv":
                return "紫外线指数";
            case "air":
                return "空气污染扩散条件指数";
            case "ac":
                return "空调开启指数";
            case "ag":
                return "过敏指数";
            case "al":
                return "太阳镜指数";
            case "mu":
                return "化妆指数";
            case "airc":
                return "晾晒指数";
            case "ptfc":
                return "交通指数";
            case "fsh":
                return "钓鱼指数";
            case "spi":
                return "防晒指数";
            default:
                return null;
        }
    }
}
