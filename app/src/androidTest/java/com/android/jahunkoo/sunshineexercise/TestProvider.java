package com.android.jahunkoo.sunshineexercise;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import com.android.jahunkoo.sunshineexercise.data.WeatherContract.LocationEntry;
import com.android.jahunkoo.sunshineexercise.data.WeatherContract.WeatherEntry;
import com.android.jahunkoo.sunshineexercise.data.WeatherDbHelper;

/**
 * Created by Jahun Koo on 2015-01-16.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

/*
    public void testDeleteDb() throws Throwable{
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }
*/

    public void deleteAllRecords(){
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void setUp() {
        deleteAllRecords();
    }

    public void testInsertReadProvider() {
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestDb.createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        assertTrue(locationRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor(cursor, testValues);

        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,   // leaving "columns" null just returns all the columns.
                null,   // cols for "where" clause
                null,   // values for "where" clause
                null    // sort order
        );
        TestDb.validateCursor(cursor, testValues);

        ContentValues weatherValues = TestDb.createWeatherValues(locationRowId);

        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(WeatherEntry.CONTENT_URI, weatherValues);
        assertTrue( weatherInsertUri != null);

        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,   //Table to Query
                null,   //leaving "columns" null just returns all the columns.
                null,   //cols for "where" clause
                null,   //values for "where" clause
                null    // columns to group by
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        // Problem
        addAllContentValues(weatherValues, testValues);

        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TestDb.TEST_LOCATION),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor(weatherCursor, weatherValues);


        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        dbHelper.close();
    }

    public void testGetType() {
        // content://com.example.jahunkoo.sunshineexample/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.jahunkoo.sunshineexample/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.jahunkoo.sunshineexample/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }


    public void testUpdateLocation() {
        ContentValues values = TestDb.createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
                new String[] {Long.toString(locationRowId)});

        assertEquals(count,1);

        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null,
                null,
                null
        );

        TestDb.validateCursor(cursor, updatedValues);
    }

    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source){
        for(String key : source.keySet()){
            destination.put(key, source.getAsString(key));
        }
    }

    static final String KALAMAZOO_LOCATION_SETTING = "kalamazoo";
    static final String KALAMAZOO_WEATHER_START_DATE = "20140625";

    long locationRowId;

    static ContentValues createKalamazooWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, KALAMAZOO_WEATHER_START_DATE);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.5);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 85);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 35);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Cats and Dogs");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 3.4);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 42);

        return weatherValues;
    }

    static ContentValues createKalamazooLocationValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, KALAMAZOO_LOCATION_SETTING);
        testValues.put(LocationEntry.COLUMN_CITY_NAME, "Kalamazoo");
        testValues.put(LocationEntry.COLUMN_COORD_LAT, 42.2917);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, -85.5872);

        return testValues;
    }

    public void insertKalamazooData() {
        ContentValues kalamazooLocationValues = createKalamazooLocationValues();
        Uri locationInsertUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, kalamazooLocationValues);
        assertTrue(locationInsertUri != null);

        locationRowId = ContentUris.parseId(locationInsertUri);

        ContentValues kalamazooWeatherValues = createKalamazooWeatherValues(locationRowId);
        Uri weatherInsertUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, kalamazooWeatherValues);
        assertTrue(weatherInsertUri != null);
    }

    public void testUpdateAndReadWeather() {
        insertKalamazooData();
        String newDescription = "Cats and Frogs (don't warn the tadpoles!)";

        ContentValues kalamazooUpdate = new ContentValues();
        kalamazooUpdate.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

//update가 가능한가?? 어느 레코드에(where)절이 없는데 어떻게 update가 가능하지???
        mContext.getContentResolver().update(WeatherEntry.CONTENT_URI, kalamazooUpdate, null, null);

        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
        kalamazooAltered.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

        TestDb.validateCursor(weatherCursor, kalamazooAltered);
    }

    /**
     * 문제가 많은 메서드.
     * http://forums.udacity.com/questions/100212444/i-cannot-make-sense-of-the-advanced-test?page=1&focusedAnswerId=100212800#100212800
     */
    public void testRemoveHumidityAndReadWeather() {
     insertKalamazooData();

     mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI,
             WeatherEntry.COLUMN_HUMIDITY + " = " + locationRowId, null);

        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
        kalamazooAltered.remove(WeatherEntry.COLUMN_HUMIDITY);

        TestDb.validateCursor(weatherCursor, kalamazooAltered);
        //int idx = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY);
        //assertEquals(-1, idx);
    }

}
