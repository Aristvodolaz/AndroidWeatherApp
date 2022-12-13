package com.example.myapplication.classes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class ForecastArea {


    private List<ForecastLoc> list;
    private boolean isFromAPI;

    public ForecastArea() {
        list = new ArrayList<ForecastLoc>();
        isFromAPI = true;
    }

    public ForecastArea(List<ForecastLoc> forecastList) {
        list = forecastList;
        isFromAPI = true;
    }



    public List<ForecastLoc> getList() {
        return list;
    }

    public boolean getIsFromAPI () { return isFromAPI; }

    public void setList (List<ForecastLoc> forecastList) {
        list = forecastList;
    }

    public void setIsFromAPI(boolean fromDB) {
        isFromAPI = fromDB;
    }

    public static ForecastArea parseJSON(String response) {
        Gson gson = new GsonBuilder().create();
        ForecastArea forecastArea = gson.fromJson(response, ForecastArea.class);
        return forecastArea;
    }
}
