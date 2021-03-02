package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private Location location;
    private boolean first = true;
    private double lat;
    private double lon;

    public class FetchWeather extends AsyncTask<Double, Void, JSONObject>{
        @Override
        protected JSONObject doInBackground(Double... doubles) {
            String key = "0d753bca148c94c70a853e04ae82d872";
            String link = "https://api.openweathermap.org/data/2.5/weather?lat=" + doubles[0] + "&lon=" + doubles[1] + "&appid=" + key;
            StringBuilder builder = new StringBuilder();
            JSONObject jsonObject = null;
            try {
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                int data = reader.read();
                while(data != -1){
                    char c = (char) data;
                    builder.append(c);
                    data = reader.read();
                }
            } catch (FileNotFoundException e) {
                System.out.println("Please Enter a valid city!!");
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                jsonObject = new JSONObject(builder.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }
    }

    public class FetchWeatherByName extends AsyncTask<String, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(String... strings) {
            String key = "0d753bca148c94c70a853e04ae82d872";
            String link = "https://api.openweathermap.org/data/2.5/weather?q=" + strings[0] + "&appid=" + key;
            StringBuilder builder = new StringBuilder();
            JSONObject jsonObject = null;
            try {
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                int data = reader.read();
                while(data != -1){
                    char c = (char) data;
                    builder.append(c);
                    data = reader.read();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                jsonObject = new JSONObject(builder.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }
    }

    public void buttonAction(View v){
        EditText city = findViewById(R.id.city);
        city.setVisibility(View.VISIBLE);
        refreshInfo();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if((grantResults.length > 0) &&
                (grantResults[0] == PackageManager.PERMISSION_GRANTED))
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                lon = location.getLongitude();
                lat = location.getLatitude();
                refreshInfo();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText city = findViewById(R.id.city);
        TextView error = findViewById(R.id.error);
        city.setVisibility(View.INVISIBLE);
        error.setVisibility(View.INVISIBLE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if(Build.VERSION.SDK_INT > 23){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                lon = location.getLongitude();
                lat = location.getLatitude();
                refreshInfo();
        }
    }

    @SuppressLint("SetTextI18n")
    private void refreshInfo(){
        EditText city = findViewById(R.id.city);
        TextView temp = findViewById(R.id.temp);
        TextView error = findViewById(R.id.error);
        TextView location = findViewById(R.id.location);
        TextView humidity = findViewById(R.id.humidity);
        TextView pressure = findViewById(R.id.pressure);
        TextView visibility = findViewById(R.id.visibility);
        JSONObject tempInfo;
        try {
            JSONObject weatherDetails = null;
            if(first) {
                FetchWeather fetch = new FetchWeather();
                weatherDetails = fetch.execute(lat, lon).get();
                first = false;
                while(weatherDetails == null)
                    if(weatherDetails != null)
                        break;
            }
            else if(!first){
                FetchWeatherByName name = new FetchWeatherByName();
                weatherDetails = name.execute(city.getText().toString()).get();
                if(weatherDetails == null) {
                    error.setVisibility(View.VISIBLE);
                    return;
                }
                else
                    error.setVisibility(View.INVISIBLE);
            }
            tempInfo = (JSONObject) weatherDetails.get("main");
            if(!weatherDetails.get("name").toString().contentEquals(""))
                location.setText(weatherDetails.get("name").toString());
            temp.setText(kelvinToCelsius(tempInfo.get("temp").toString()) + " \u2103");
            humidity.setText(tempInfo.get("humidity") + " %");
            pressure.setText(tempInfo.get("pressure") + " pa");
            visibility.setText(visibilityM(weatherDetails.get("visibility").toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String kelvinToCelsius(String temperatureInK){
        float temp = (float) (Float.parseFloat(temperatureInK) - 273.15);
        DecimalFormat df = new DecimalFormat("###.##");
        return df.format(temp);
    }

    private String visibilityM(String visibility){
        double METERS_IN_KM = 1000.0;
        int temp = Integer.parseInt(visibility);
        if(temp < 1000)
            return visibility + " m";
        double d = temp/METERS_IN_KM;
        return d + " Km";
    }
}