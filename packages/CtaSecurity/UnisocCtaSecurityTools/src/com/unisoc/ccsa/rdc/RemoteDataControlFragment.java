package com.unisoc.ccsa.rdc;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import com.unisoc.ccsa.Log;

import com.unisoc.ccsa.R;
import com.unisoc.ccsa.rdc.manager.PersistentDataManager;
import com.unisoc.ccsa.rdc.service.CmdPerformerService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class RemoteDataControlFragment extends PreferenceFragment {
    private static final String TAG = "RemoteDataControl";
    private static RemoteDataControlFragment mRemoteDataControlFragment = null;
    private RDCSwitchPreference mSwithPref;
    private SettingsPreference mSettingsPref;
    private PersistentDataManager mPdm;
    private boolean verid = false;
    private static Method sGetLockPatternUtilsMethod;

    public RemoteDataControlFragment() {
        // Required empty public constructor
    }

    public static RemoteDataControlFragment getInstance() {
        if (mRemoteDataControlFragment == null) {
            mRemoteDataControlFragment = new RemoteDataControlFragment();
        }
        return mRemoteDataControlFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rdc_main);
        mPdm = PersistentDataManager.getInstance();
        mPdm.setSharedPreferences(getActivity().getSharedPreferences("rdc_data", 0));

        mSwithPref = (RDCSwitchPreference) this.getPreferenceManager().findPreference("RDCswitch_pref");
        mSettingsPref = (SettingsPreference) this.getPreferenceManager().findPreference("settings_pref");

        mSwithPref.setPersistentDataManager(mPdm, mPdm.isOn());
        mSettingsPref.setPersistentDataManager(mPdm);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "hidden:" + hidden + " secure:" + getIsSecure());

        if (!hidden && getIsSecure() && !verid) {

            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.lc_verify);
            builder.setMessage(R.string.lc_verify_mesg);
            builder.setPositiveButton(R.string.ok, listener);
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("reset_pref")) {
            Intent it = new Intent(getActivity().getApplicationContext(), GuideDialogActivity.class);
            it.putExtra("type", GuideDialogActivity.TYPE_DELETE_INFO);
            startActivity(it);
        } else if (preference.getKey().equals("lock_pref")) {
            Intent it = new Intent(getActivity().getApplicationContext(), GuideDialogActivity.class);
            it.putExtra("type", GuideDialogActivity.TYPE_LOCK_INFO);
            startActivity(it);
        } else if (preference.getKey().equals("reletives_pref")) {
            Intent it = new Intent(getActivity().getApplicationContext(), GuideDialogActivity.class);
            it.putExtra("type", GuideDialogActivity.TYPE_RELATIVES_INFO);
            startActivity(it);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private boolean getIsSecure() {
        Class<?> clazz = null;
        Object instance = null;
        boolean secure = false;

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                clazz = Class.forName("com.android.internal.widget.LockPatternUtils");
                instance = clazz.newInstance();
                sGetLockPatternUtilsMethod = clazz.getMethod("isSecure");
            } else {
                clazz = Class.forName("com.android.internal.widget.LockPatternUtils");
                Constructor constructor = clazz.getConstructor(Context.class);
                instance = constructor.newInstance(getActivity());
                sGetLockPatternUtilsMethod = clazz.getMethod("isSecure", int.class);
            }
        } catch (Exception e) {
            sGetLockPatternUtilsMethod = null;
            Log.e(TAG, "Exception", e);
        }
        try {
            if (sGetLockPatternUtilsMethod != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    secure = (Boolean) sGetLockPatternUtilsMethod.invoke(instance);
                } else {
                    secure = (Boolean) sGetLockPatternUtilsMethod.invoke(instance, 0);
                }
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException", e);
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException", e);
        }
        return secure;
    }

    private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                getActivity().startService(new Intent(getActivity(),
                        CmdPerformerService.class).putExtra(
                        CmdPerformerService.EXTRA_CMD,
                        CmdPerformerService.CMD_LOCK_SCREEN));
                verid = true;
            }
        }
    };
}
