package com.android.jahunkoo.sunshineexercise;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.android.jahunkoo.sunshineexercise.data.WeatherContract.LocationEntry;
import com.android.jahunkoo.sunshineexercise.data.WeatherContract.WeatherEntry;
import com.android.jahunkoo.sunshineexercise.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * 이 클래스 안의 모든 함수들이 실행됨
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
        WeatherDbHelper dbHelper = new WeatherDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = createNorthPoleLocationValues();

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);

        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,
                null,   // 모든 컬럼들 가져옴
                null,   // where절 column
                null,   // where절 값
                null,
                null,
                null
        );
        validateCursor(cursor,values);


        ContentValues weatherValues = createWeatherValues(locationRowId);

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

        validateCursor(weatherCursor,weatherValues);

        dbHelper.close();
    }


    static ContentValues createWeatherValues(long locationRowId){
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

        return weatherValues;
    }

    static ContentValues createNorthPoleLocationValues() {
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, "99705");
        values.put(LocationEntry.COLUMN_CITY_NAME, "North Pole");
        values.put(LocationEntry.COLUMN_COORD_LAT, 64.7488);
        values.put(LocationEntry.COLUMN_COORD_LONG, -147.353);

        return values;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for(Map.Entry<String, Object> entry : valueSet){
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

}
