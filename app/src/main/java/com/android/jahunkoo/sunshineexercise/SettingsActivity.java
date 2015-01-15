package com.android.jahunkoo.sunshineexercise;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Created by Jahun Koo on 2015-01-15.
 */
public class SettingsActivity extends PreferenceActivity
implements Preference.OnPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferencesSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferencesSummaryToValue(findPreference(getString(R.string.pref_units_key)));
    }

    private void bindPreferencesSummaryToValue(Preference preference){
        preference.setOnPreferenceChangeListener(this);

        //current value
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(),""));

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();

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


}
