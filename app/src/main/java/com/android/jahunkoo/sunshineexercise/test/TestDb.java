package com.android.jahunkoo.sunshineexercise.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.android.jahunkoo.sunshineexercise.data.WeatherContract.LocationEntry;
import com.android.jahunkoo.sunshineexercise.data.WeatherContract.WeatherEntry;
import com.android.jahunkoo.sunshineexercise.data.WeatherDbHelper;

/**
 * Created by Jahun Koo on 2015-01-16.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db  = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;

        WeatherDbHelper dbHelper = new WeatherDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_CITY_NAME, testCityName);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);

        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);


        String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIndex);

            int nameIndex = cursor.getColumnIndex(LocationEntry.COLUMN_CITY_NAME);
            String name = cursor.getString(nameIndex);


            int latIndex = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LAT);
            double latitude = cursor.getDouble(latIndex);

            int longIndex = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LONG);
            double longitude = cursor.getDouble(longIndex);

            assertEquals(testLocationSetting, location);
            assertEquals(testCityName, name);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);

        }else {
            fail("No values returned :(");
        }

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20150116");
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 123);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "android jam");
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 34);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 80);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.6);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);

        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue( weatherRowId != -1);

        Cursor weatherCursor = db.query(
                WeatherEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if(!weatherCursor.moveToFirst()){
            fail("No weather data returned");
        }

        assertEquals(weatherCursor.getInt(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY)),
                locationRowId);
        assertEquals(weatherCursor.getString(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)),
                "20150116");
        assertEquals(weatherCursor.getDouble(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DEGREES)),
                1.1);
        assertEquals(weatherCursor.getDouble(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY)),
                1.3);
        assertEquals(weatherCursor.getDouble(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE)),
                1.1);
        assertEquals(weatherCursor.getInt(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)),
                80);
        assertEquals(weatherCursor.getInt(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)),
                34);
        assertEquals(weatherCursor.getString(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC)),
                "android jam");
        assertEquals(weatherCursor.getDouble(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED)),
                5.6);
        assertEquals(weatherCursor.getInt(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID)),
                123);

        cursor.close();
        weatherCursor.close();
        dbHelper.close();
    }
}
