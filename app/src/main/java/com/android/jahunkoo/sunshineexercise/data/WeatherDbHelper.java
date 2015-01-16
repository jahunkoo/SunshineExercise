package com.android.jahunkoo.sunshineexercise.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.android.jahunkoo.sunshineexercise.data.WeatherContract.WeatherEntry;
import com.android.jahunkoo.sunshineexercise.data.WeatherContract.LocationEntry;

/**
 * weather data를 위한 local database를 관리한다.
 * Created by Jahun Koo on 2015-01-16.
 */
public class WeatherDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "weather.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY," +
                LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                "UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING + ") ON CONFLICT IGNORE"+
                " );";

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL," +
                WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL," +
                WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL," +
                WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +
                WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL," +
                WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL," +
                WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL," +
                WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL," +
                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL," +
                WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL," +
                " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +
                " UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " +
                WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE" +
                ");"; // ON CONFLICT clause는 SQLite만의 문법. 'or'로 바꿔서 읽으면 더 읽기 편할 것이라 얘기함.
                                                                         // 중복되는 값이 발생하면 새로운 것으로 replace됨
                                                                         // https://www.sqlite.org/lang_conflict.html

        db.execSQL(SQL_CREATE_LOCATION_TABLE);
        db.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //로컬 데이터베이스는 단지 온라인 데이터의 캐쉬. 그렇기 때문에 업그레이드(onUpgrade)에서는 기존 데이터를 버리자.
        db.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(db);
    }
}
