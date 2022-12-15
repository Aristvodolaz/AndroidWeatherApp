package com.example.myapplication.classes;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Locale;

public class ForecastLoc {
    private String name;
    private Coord coord;

    private WeatherMain main;
    private List<WeatherType> weather;

    @SerializedName("dt")
    private long time;

    public ForecastLoc (String locName, Coord coordinates,
                        WeatherMain weatherMain, List<WeatherType> weatherTypes, long timeCreated) {
        name = locName;
        coord = coordinates;
        main = weatherMain;
        weather = weatherTypes;
        time = timeCreated;
    }

    public String getName() {
        return name;
    }

    public Coord getCoord() {
        return coord;
    }

    public WeatherMain getMain() {
        return main;
    }

    public List<WeatherType> getWeather() {
        return weather;
    }

    public long getTime() { return time; }


    @Override
    public String toString() {

        return
                "City: " + getName()+ "\nTemerature: " + getMain() +
                "\nWeather now: " + getWeather().toString().toLowerCase(Locale.ROOT).replaceAll("^\\[|\\]$", "")
                ;
    }
}

