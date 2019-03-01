package com.example.yunbianweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yunbianweather.db.City;
import com.example.yunbianweather.db.County;
import com.example.yunbianweather.db.Province;
import com.example.yunbianweather.util.HttpUtil;
import com.example.yunbianweather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressBar progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;  //省列表

    private List<City> cityList;  //市列表

    private List<County> countyList;  //县列表

    private Province selectedProvince;  //选中的省列表

    private City selectedCity;  //选中的市列表

    private int currentLevel;  //当前选中的级别

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_btn);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (currentLevel == LEVEL_PROVINCE) {
                selectedProvince = provinceList.get(position);
                queryCity();
            } else if (currentLevel == LEVEL_CITY) {
                selectedCity = cityList.get(position);
                queryCounty();
            }else if (currentLevel == LEVEL_COUNTY){
                if (getActivity() instanceof MainActivity){
                    Intent intent = new Intent(getContext(), WeatherActivity.class);
                    intent.putExtra("weather_id", countyList.get(position).getWeatherId());
                    startActivity(intent);
                    getActivity().finish();
                }else if (getActivity() instanceof WeatherActivity){
                    WeatherActivity activity = (WeatherActivity) getActivity();
                    activity.drawerLayout.closeDrawers();
                    activity.swipeRefresh.setRefreshing(true);
                    activity.requestWeather(countyList.get(position).getWeatherId());
                }
            }
        });
        backButton.setOnClickListener((view) -> {
            switch (currentLevel) {
                case LEVEL_COUNTY:
                    queryCity();
                    break;
                case LEVEL_CITY:
                    queryProvinces();
                default:
                    break;
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryCity() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceid = ?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
            queryFromServer(address, "city");
        }
    }

    private void queryCounty() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?",
                String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            String address = "http://guolin.tech/api/china/" +
                    selectedProvince.getProvinceCode() +
                    "/" +
                    selectedCity.getCityCode();
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    closeProgressDialog();
                    Toast.makeText(getContext(), "加载失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                switch (type) {
                    case "province":
                        result = Utility.handleProvinceResponse(responseText);
                        break;
                    case "city":
                        result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                        break;
                    case "county":
                        result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                        break;
                    default:
                        break;
                }
                if (result) {
                    getActivity().runOnUiThread(() -> {
                        closeProgressDialog();
                        switch (type) {
                            case "province":
                                queryProvinces();
                                break;
                            case "city":
                                queryCity();
                                break;
                            case "county":
                                queryCounty();
                                break;
                            default:
                                break;
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressBar(getActivity());
            progressDialog.setVisibility(View.VISIBLE);
        }
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.setVisibility(View.INVISIBLE);
        }
    }
}
