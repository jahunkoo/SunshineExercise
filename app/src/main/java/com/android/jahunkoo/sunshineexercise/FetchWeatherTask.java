package com.android.jahunkoo.sunshineexercise;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.android.jahunkoo.sunshineexercise.data.WeatherContract;
import com.android.jahunkoo.sunshineexercise.data.WeatherContract.LocationEntry;
import com.android.jahunkoo.sunshineexercise.data.WeatherContract.WeatherEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

/**
 * Created by Jahun Koo on 2015-01-20.
 */
public class FetchWeatherTask extends AsyncTask<String,Void,Void> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private final Context mContext;

    public FetchWeatherTask(Context mContext) {
        this.mContext = mContext;
    }

    private long addLocation(String locationSetting, String cityName, double lat, double lon) {

        //First, check if the location with this city name exist in the db
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                new String[]{LocationEntry._ID}, //반환되는 data column명이다.  만일 null 이면 모든 column을 반환한다.
                LocationEntry.COLUMN_LOCATION_SETTING + " = ?", //query에 대한 조건절
                new String[]{locationSetting}, //selection에 ?가 포함되는 경우에 값을 치환한다.
                null);

        if(cursor.moveToFirst()) {
            int locationIdIndex = cursor.getColumnIndex(LocationEntry._ID);
            return cursor.getLong(locationIdIndex);
        }else {
            ContentValues locationValues = new ContentValues();
            locationValues.put(LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(LocationEntry.COLUMN_COORD_LONG, lon);

            Uri locationInsertUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues);

            return ContentUris.parseId(locationInsertUri);
        }
    }

    private void getWeatherDataFromJson(String forecastJsonStr, int numDays, String locationSetting) throws JSONException {

        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_COORD_LAT = "lat";
        final String OWM_COORD_LONG = "lon";

        final String OWM_LIST = "list";
        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WIND_SPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);
        JSONObject coordJson = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = coordJson.getLong(OWM_COORD_LAT);
        double cityLongitude = coordJson.getLong(OWM_COORD_LONG);

        Log.v(LOG_TAG, cityName + ", with coord:" + cityLatitude + " " + cityLongitude);

        long locationID = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

        Vector<ContentValues> cvVector = new Vector<ContentValues>(weatherArray.length());

        for(int i=0; i<weatherArray.length();i++){

            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;

            JSONObject dayForecast = weatherArray.getJSONObject(i);


            dateTime = dayForecast.getLong(OWM_DATETIME);
            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WIND_SPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationID);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT,
                    WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cvVector.add(weatherValues);
        }
        if(cvVector.size() >0) {
            ContentValues[] cvArray =new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, cvArray);
        }
    }


    @Override
    protected Void doInBackground(String... params) {

        if(params.length == 0){
            return null;
        }
        String locationQuery = params[0];

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        try{
            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM,params[0])
                    .appendQueryParameter(FORMAT_PARAM,format)
                    .appendQueryParameter(UNITS_PARAM,units)
                    .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if(buffer.length() == 0){
                return null;
            }
            forecastJsonStr = buffer.toString();

        }catch (IOException e){
            Log.e(LOG_TAG, "Error", e);
            return null;
        }finally{
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            getWeatherDataFromJson(forecastJsonStr, numDays, locationQuery);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        //json파싱부분에서 에러 날 경우 null리턴됨
        return null;
    }

}
