package com.unisoc.ccsa.rdc;


import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.unisoc.ccsa.R;
import com.unisoc.ccsa.rdc.manager.PersistentDataManager;

public class SettingsActivity extends PreferenceActivity {
    private PasswdDialogPreference pdPref;
    private ReletivesNumSettingPreference rnPref;
    private PersistentDataManager pm;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pm = PersistentDataManager.getInstance();
        this.addPreferencesFromResource(R.xml.rdc_settings);
        pdPref = (PasswdDialogPreference) this.getPreferenceManager().findPreference(
                "editPassword_pref");
        rnPref = (ReletivesNumSettingPreference) this.getPreferenceManager().findPreference(
                "settingReletives_pref");
        pdPref.setPersistentDataManager(pm);
        rnPref.setPersistentDataManager(pm);
    }

    @Override
    protected void onStart() {
        //((RDCApplication) this.getApplication()).setBackend(true);
        super.onStart();
    }

    protected void onSaveInstanceState(Bundle outState) {
    }
}
