package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.DBase.ForecastContract;
import com.example.myapplication.DBase.ForecastDbHelper;
import com.example.myapplication.classes.ForecastArea;
import com.example.myapplication.classes.ForecastLoc;
import com.example.myapplication.classes.WeatherType;
import com.example.myapplication.tools.RemoteFetch;
import com.example.myapplication.tools.Utilites;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import com.example.myapplication.classes.WeatherMain;
import static com.example.myapplication.R.id.map;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, View.OnClickListener {


        private Button button;
        private TextView mDisplayText;
        private MapFragment mMapFragment;
        private GoogleMap googleMap;
        private HashMap<String, MarkerOptions> mMarkerOptions;

        private SQLiteDatabase mainDB;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            button = (Button)findViewById(R.id.button);
            button.setOnClickListener(this);

            mMapFragment = (MapFragment) getFragmentManager().findFragmentById(map);
            mMapFragment.getMapAsync(this);

            ForecastDbHelper dbHelper = new ForecastDbHelper(this);
            mainDB = dbHelper.getWritableDatabase();

            try {
                mainDB.beginTransaction();
                mainDB.delete(ForecastContract.ForecastLocDB.TABLE_NAME, null,null);
                mainDB.setTransactionSuccessful();
            }
            catch (SQLException e) {

                Log.e("DBError", e.getStackTrace().toString() );
            }
            finally {
                mainDB.endTransaction();
            }

        }

        @Override
        public void onMapReady(GoogleMap map) {
            googleMap = map;

            final LatLngBounds.Builder builder = new LatLngBounds.Builder();

            builder.include(new LatLng(0,0));
            builder.include(new LatLng(10,10));

            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);

                    googleMap.animateCamera(cu);
                }
            });
            googleMap.setOnCameraMoveStartedListener(this);


        }

        @Override
        public void onCameraMoveStarted(int i) {
            googleMap.setOnCameraIdleListener(this);
            googleMap.setOnCameraMoveStartedListener(null);
        }

        @Override
        public void onCameraIdle() {
            googleMap.setOnCameraIdleListener(null);
            try {
                googleMap.clear();
                mMarkerOptions = new HashMap<String, MarkerOptions>();

                double mapZoom = googleMap.getCameraPosition().zoom;
                LatLngBounds bounds = (googleMap.getProjection().getVisibleRegion()).latLngBounds;
                LatLng ne = bounds.northeast;
                LatLng sw = bounds.southwest;

                setSubscriber(sw.longitude, sw.latitude, ne.longitude, ne.latitude, (int) mapZoom);
            }
            catch (Error e) {
                Log.e("MAPError", e.getMessage());
            }
            googleMap.setOnCameraMoveStartedListener(this);
        }

        public void setSubscriber (double lonLeft, double latBottom, double lonRight, double latTop, int zoom) {


            Scheduler executionThread = Schedulers.io();
            Scheduler mainThread = AndroidSchedulers.mainThread();


            Observable<ForecastArea> behaviorSubject = RemoteFetch.getConcatenatedResponse(mainDB,
                    lonLeft, latBottom, lonRight,latTop, zoom);

            behaviorSubject
                    .subscribeOn(executionThread)
                    .observeOn(mainThread)
                    .subscribe(new Subscriber<ForecastArea>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            // handle error
                        }

                        @Override
                        public void onNext(ForecastArea forecastArea) {
                             drawMarkers(forecastArea.getList());

                            if (forecastArea.getIsFromAPI()) uploadForecastsToDB(forecastArea.getList());
                        }
                    });

        }


        private void drawMarkers (List<ForecastLoc> listForecasts) {


            for(ForecastLoc forecast: listForecasts) {

                LatLng pos = new LatLng(forecast.getCoord().getLat() , forecast.getCoord().getLon());
                int icon_num = Utilites.getIconResourceForWeatherCondition(forecast.getWeather().get(0).getId());

                Log.d("MAPNot", forecast.getName() + " " + forecast.getCoord().getLat() + " " + forecast.getCoord().getLon());

                String titleForecast = "...";
                BitmapDescriptor iconImg = BitmapDescriptorFactory.fromResource( icon_num );

                String fCastName = forecast.getName();
                if (mMarkerOptions.containsKey(fCastName)) {
                    mMarkerOptions.get(fCastName).icon(iconImg);
                }
                else {

                    MarkerOptions marker = new MarkerOptions().position(pos).title(titleForecast).icon(iconImg);
                    mMarkerOptions.put(fCastName, marker);
                    googleMap.addMarker(marker);
                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @SuppressLint("ResourceType")
                        @Override
                        public boolean onMarkerClick(@NonNull Marker marker) {
                            Toast.makeText(mMapFragment.getActivity().getApplicationContext(),forecast.toString(),  Toast.LENGTH_LONG).show();
                       return false;
                        }
                    });
                }
            }
            Log.d("MAPNot", "-");

        }

        private void uploadForecastsToDB (List<ForecastLoc> listForecasts) {
            try {
                mainDB.beginTransaction();

                ContentValues cv;
                for(ForecastLoc forecast: listForecasts) {
                    cv = new ContentValues();

                    cv.put(ForecastContract.ForecastLocDB.LOC_NAME_NAME, forecast.getName());
                    double lat = forecast.getCoord().getLat();
                    double lon = forecast.getCoord().getLon();
                    cv.put(ForecastContract.ForecastLocDB.LATITUDE_NAME, lat);
                    cv.put(ForecastContract.ForecastLocDB.LONGITUDE_NAME, lon);
                    cv.put(ForecastContract.ForecastLocDB.TEMPERATURE_NAME, forecast.getMain().getTemp());
                    cv.put(ForecastContract.ForecastLocDB.WEATHER_ID_NAME, forecast.getWeather().get(0).getId());
                    cv.put(ForecastContract.ForecastLocDB.WEATHER_DESCRIPTION_NAME, forecast.getWeather().get(0).getMain());
                    cv.put(ForecastContract.ForecastLocDB.FORECAST_TIME_NAME, forecast.getTime());
                    //long ins = mainDB.insert(ForecastContract.ForecastLocDB.TABLE_NAME, null, cv);

                    long result = mainDB.insertWithOnConflict(ForecastContract.ForecastLocDB.TABLE_NAME,
                            null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                    if (result==-1) mainDB.update(ForecastContract.ForecastLocDB.TABLE_NAME, cv,
                            ForecastContract.ForecastLocDB.LATITUDE_NAME + "=? AND " +
                                    ForecastContract.ForecastLocDB.LONGITUDE_NAME + "=?",
                            new String[] { Double.toString(lat),  Double.toString(lon)});

                }

                mainDB.setTransactionSuccessful();
            }
            catch (SQLException e) {

                Log.e("DBError", e.getMessage() );
            }
            finally {
                mainDB.endTransaction();
            }

        }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                Intent intent = new Intent(this, MainActivity2.class);
                startActivity(intent);
            default:
                break;
        }
    }
}
