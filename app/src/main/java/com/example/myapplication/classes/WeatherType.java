package com.example.myapplication.classes;

public class WeatherType {
    private int id;
    private String main;
    public WeatherType(int weatherID, String weatherDesc) {
        id = weatherID;
        main = weatherDesc;
    }

    public int getId() {
        return id;
    }

    public String getMain() {
        return main;
    }

    @Override
    public String toString() {
        return
                " " + main;

    }
}
