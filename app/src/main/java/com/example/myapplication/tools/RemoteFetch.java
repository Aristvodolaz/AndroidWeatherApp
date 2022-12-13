package com.example.myapplication.tools;



import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.myapplication.DBase.ForecastContract;
import com.example.myapplication.OpenWeatherClient;
import com.example.myapplication.classes.Coord;
import com.example.myapplication.classes.ForecastArea;
import com.example.myapplication.classes.ForecastLoc;
import com.example.myapplication.classes.WeatherMain;
import com.example.myapplication.classes.WeatherType;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class RemoteFetch {
    private static final String OPEN_WEATHER_BULK_API = "http://api.openweathermap.org/data/2.5/box/city?bbox=";
    private static final String OPEN_WEATHER_BASE_URL = "http://api.openweathermap.org/";
    private static final String OPEN_WEATHER_API_KEY = "7e39bf2aa01fb82aa7de51598487ff9c";


    public static Observable<ForecastArea> getConcatenatedResponse (SQLiteDatabase sqLiteDatabase,
                                                                    double lonLeft, double latBottom,
                                                                    double lonRight, double latTop, int zoom) {


        Observable<ForecastArea> callToDb = (RemoteFetch.getForecastFromDB(sqLiteDatabase, lonLeft, latBottom, lonRight, latTop))
                .filter(new Func1<ForecastArea, Boolean>() {
                    @Override
                    public Boolean call(ForecastArea forecastArea) {
                        return( forecastArea.getList().size() > 0 );
                    }});

        Observable<ForecastArea> callToApi = RemoteFetch.getForecastResponse(lonLeft, latBottom, lonRight,latTop, zoom);
        Observable<ForecastArea> call = Observable.concat(callToDb, callToApi);

        final BehaviorSubject<ForecastArea> behavior = BehaviorSubject.create();

        call.subscribe(new Subscriber<ForecastArea>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                behavior.onError(e);
                Log.e("RXJavaErr", e.getMessage());
            }

            @Override
            public void onNext(ForecastArea forecastArea) {
                if (behavior.hasValue()) {

                    ForecastArea oldForecastArea = behavior.getValue();
                    List<ForecastLoc> oldList = oldForecastArea.getList();
                    List<ForecastLoc> newList = forecastArea.getList();


                    for (int i=0; i<oldList.size();i++) {
                        for (int j = 0; j<newList.size(); ) {

                            if (newList.get(j).getCoord().getLat() == oldList.get(i).getCoord().getLat()
                                    && newList.get(j).getCoord().getLon() == oldList.get(i).getCoord().getLon()
                                    && newList.get(j).getTime() <= oldList.get(i).getTime() )
                                newList.remove(j);
                            else j++;
                        }
                    }
                    forecastArea.setList(newList);
                }
                behavior.onNext(forecastArea);
            }

        });


        return behavior;
    }

    public static Observable<ForecastArea> getForecastResponse (double lonLeft, double latBottom,
                                                                double lonRight, double latTop, int zoom) {

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(OPEN_WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(rxAdapter);

        Retrofit retrofit = builder.build();
        OpenWeatherClient client = retrofit.create(OpenWeatherClient.class);

        Log.d("RXJavaNot", OPEN_WEATHER_BULK_API + Double.toString(lonLeft) + "," + Double.toString(latBottom)
                + "," + Double.toString(lonRight) + "," + Double.toString(latTop) + "," + Integer.toString(zoom)
                + "&appid=" + OPEN_WEATHER_API_KEY);

        return client.forecastsForArea(Double.toString(lonLeft) + "," + Double.toString(latBottom)
                + "," + Double.toString(lonRight) + "," + Double.toString(latTop) + ","
                + Integer.toString(zoom), OPEN_WEATHER_API_KEY).cache();
    }

    public static Observable<ForecastArea> getForecastFromDB (SQLiteDatabase sqLiteDatabase, double lonLeft, double latBottom,
                                                              double lonRight, double latTop) {

        String variable = "AND ";
        if(lonLeft > 0 && lonRight <0) variable = "OR "; // if the screen crosses the 180-longitude
        String whereClause = ForecastContract.ForecastLocDB.LATITUDE_NAME + ">? AND "
                + ForecastContract.ForecastLocDB.LATITUDE_NAME + "<? AND "
                + ForecastContract.ForecastLocDB.LONGITUDE_NAME + ">? " + variable
                + ForecastContract.ForecastLocDB.LONGITUDE_NAME + "<?";


        Cursor cursor = sqLiteDatabase.query(ForecastContract.ForecastLocDB.TABLE_NAME, null, whereClause,
                new String[] { Double.toString(latBottom),  Double.toString(latTop),
                        Double.toString(lonLeft), Double.toString(lonRight)}, null, null,
                ForecastContract.ForecastLocDB._ID);

        ArrayList<ForecastLoc> forecasts = new ArrayList<ForecastLoc>();

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ForecastContract.ForecastLocDB.LOC_NAME_NAME));
            @SuppressLint("Range") double lat = cursor.getDouble(cursor.getColumnIndex(ForecastContract.ForecastLocDB.LATITUDE_NAME));
            @SuppressLint("Range") double lon = cursor.getDouble(cursor.getColumnIndex(ForecastContract.ForecastLocDB.LONGITUDE_NAME));
            @SuppressLint("Range") double temp = cursor.getDouble(cursor.getColumnIndex(ForecastContract.ForecastLocDB.TEMPERATURE_NAME));
            @SuppressLint("Range") int weatherID = cursor.getInt(cursor.getColumnIndex(ForecastContract.ForecastLocDB.WEATHER_ID_NAME));
            @SuppressLint("Range") String weatherDesc = cursor.getString(cursor.getColumnIndex(ForecastContract.ForecastLocDB.WEATHER_DESCRIPTION_NAME));
            @SuppressLint("Range") long timeCreated = cursor.getInt(cursor.getColumnIndex(ForecastContract.ForecastLocDB.FORECAST_TIME_NAME));
            ;
            Coord coord = new Coord(lat, lon);
            WeatherMain weatherMain = new WeatherMain(temp);
            ArrayList<WeatherType> weatherTypes = new ArrayList<WeatherType>();
            weatherTypes.add(new WeatherType(weatherID, weatherDesc));

            forecasts.add(new ForecastLoc(name, coord, weatherMain, weatherTypes, timeCreated));

        }

        ForecastArea forecastArea = new ForecastArea(forecasts);
        forecastArea.setIsFromAPI(false);
        cursor.close();

        return Observable.just(forecastArea);
    }


}
