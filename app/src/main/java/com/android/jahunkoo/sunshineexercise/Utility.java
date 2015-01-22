package com.android.jahunkoo.sunshineexercise;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.jahunkoo.sunshineexercise.data.WeatherContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Jahun Koo on 2015-01-20.
 */
public class Utility {


    public static String getPreferredLocation(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if( !isMetric ) {
            temp = 9*temperature/5 + 32;
        }else {
            temp = temperature;
        }
        return context.getString(R.string.format_temperature, temp);
    }

    static String formatDate(String dateString) {
        Log.d("koo","dateString:"+dateString);
        Date date = WeatherContract.getDateFromDb(dateString);
        Log.d("koo","format dateString:"+DateFormat.getDateInstance().format(date));
        return DateFormat.getDateInstance().format(date);
    }

    public static final String DATE_FORMAT = "yyyyMMdd";

    public static String getFriendlyDayString(Context context, String dateStr) {

        Date todayDate = new Date();
        String todayStr = WeatherContract.getDbDateString(todayDate);
        Date inputDate = WeatherContract.getDateFromDb(dateStr);

        if(todayStr.equals(dateStr)) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateStr)));
        }else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 7);
            String weekFutureString = WeatherContract.getDbDateString(cal.getTime());

            if(dateStr.compareTo(weekFutureString) < 0) {
                return getDayName(context, dateStr);
            }else {
                // SimpleDateFormat API
                // http://docs.oracle.com/javase/7/docs/api/
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }

    }

    public static String getDayName(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);

        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();

            if(WeatherContract.getDbDateString(todayDate).equals(dateStr)){
                return context.getString(R.string.today);
            }else{
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                Date tomorrowDate = cal.getTime();
                if(WeatherContract.getDbDateString(tomorrowDate).equals(dateStr)) {
                    return context.getString(R.string.tomorrow);
                }else{
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Convert db date format to the format "Month day", e.g "June 24"
     * @param context
     * @param dateStr
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);

        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd"); //MMMM은 June 으로 나타남
            String monthDayString = monthDayFormat.format(inputDate);
            return monthDayString;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if(Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        }else{
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 || degrees < 22.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    /**
    * Helper method to provide the icon resource id according to the weather condition id returned
    * by the OpenWeatherMap call.
    * @param weatherId from OpenWeatherMap API response
    * @return resource id for the corresponding icon. -1 if no relation is found.
    */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
    * Helper method to provide the art resource id according to the weather condition id returned
    * by the OpenWeatherMap call.
    * @param weatherId from OpenWeatherMap API response
    * @return resource id for the corresponding image. -1 if no relation is found.
    */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
                if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
            } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
            } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
            } else if (weatherId == 511) {
            return R.drawable.art_snow;
            } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
            } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_rain;
            } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
            } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
            } else if (weatherId == 800) {
            return R.drawable.art_clear;
            } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
            } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
            }
        return -1;
    }

}
