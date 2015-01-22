package com.android.jahunkoo.sunshineexercise;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.jahunkoo.sunshineexercise.data.WeatherContract;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */

/**
 * Created by Jahun Koo on 2015-01-15.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    boolean mBindingPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "in SettingsActivity onCreate");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferencesSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferencesSummaryToValue(findPreference(getString(R.string.pref_units_key)));
    }

    private void bindPreferencesSummaryToValue(Preference preference){
        mBindingPreference = true;

        preference.setOnPreferenceChangeListener(this);

        //current value
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(),""));

        mBindingPreference = false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();

        // Are we starting the preference activity?
        if( !mBindingPreference ){
            if(preference.getKey().equals(getString(R.string.pref_location_key))){
                FetchWeatherTask weatherTask = new FetchWeatherTask(this);
                String location = newValue.toString();
                weatherTask.execute(location);
            }else{
                getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
            }
        }

        if(preference instanceof ListPreference){ //ListPreference면 새로운값의 위치가 0보다 큰 경우에만 setSummary
            ListPreference listPreference = (ListPreference) preference;
            int preIndex = listPreference.findIndexOfValue(stringValue);
            if(preIndex >= 0){
                preference.setSummary(stringValue);
            }
        }else{ //ListPreference만 아니면 그냥 setSummary
            preference.setSummary(stringValue);
        }
        return true;
    }

    @Override
    protected void onStart() {
        Log.v(LOG_TAG, "in SettingsActivity onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.v(LOG_TAG, "in SettingsActivity onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.v(LOG_TAG, "in SettingsActivity onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.v(LOG_TAG, "in SettingsActivity onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(LOG_TAG, "in SettingsActivity onDestroy");
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
