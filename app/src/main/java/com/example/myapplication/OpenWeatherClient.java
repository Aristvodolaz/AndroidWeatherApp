package com.example.myapplication;

import com.example.myapplication.classes.ForecastArea;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface OpenWeatherClient {
    @GET("data/2.5/box/city")
    Observable<ForecastArea> forecastsForArea(@Query("bbox") String coordsZoom, @Query("appid") String apiKey);
}
