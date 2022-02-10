package com.unisoc.ccsa.rdc.manager;

import java.lang.reflect.Method;

import android.telephony.TelephonyManager;
import android.content.Context;
import com.unisoc.ccsa.Log;

public class SimsManager {
    private final static String SUB_TAG = "SimsManager";
    public final static int SIM_VALID = 0;
    public final static int SIM_INVALID = 1;
    public final static int SIM_ABSENT = 2;
    private static final int SIM_COUNT = 2;

    private static SimsManager sm = null;
    private TelephonyManager tm[] = null;
    private int simStates[] = null;
    private String phoneNums[] = null;
    private String IMSIs[] = null;
    private int phoneCnt = 0;

    private Context mContext;


    public int getSimStates(int phoneId) {
        return simStates[phoneId];
    }

    public String getPhoneNums(int phoneId) {
        return phoneNums[phoneId];
    }

    public String getIMSIs(int phoneId) {
        return IMSIs[phoneId];
    }

    public int getPhoneCnt() {
        return phoneCnt;
    }

    SimsManager(Context context) {
        mContext = context;
        init();
    }

    public static SimsManager getInstance(Context context) {
        if (null == sm) {
            sm = new SimsManager(context);
        }

        return sm;
    }

    private boolean init() {

        Method methodGetPhoneCount = null;

        try {
            methodGetPhoneCount = TelephonyManager.class.getMethod("getPhoneCount");

            phoneCnt = (Integer) methodGetPhoneCount.invoke(null);
            Log.d(SUB_TAG, "phone count = " + phoneCnt);
        } catch (NoSuchMethodException e) {
            phoneCnt = 1;
        } catch (Exception e) {
            return false;
        }

        if (phoneCnt < 1) {
            Log.d(SUB_TAG, "phone count less than 1");
            return false;
        }

        try {
            tm = new TelephonyManager[phoneCnt];
            simStates = new int[phoneCnt];
            for (int i = 0; i < phoneCnt; i++) {
                tm[i] = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            }
        } catch (Exception e) {
            return false;
        }

        phoneNums = new String[phoneCnt];
        IMSIs = new String[phoneCnt];

        onSimChanged();

        return true;
    }

    public void onSimChanged() {
        updateSimState();
        updateSimInfo();
    }

    private void updateSimState() {
        int state[] = new int[SIM_COUNT];
        for (int i = 0; i < phoneCnt; i++) {
            state[i] = tm[i].getSimState();

            switch (state[i]) {
                case TelephonyManager.SIM_STATE_READY:
                    simStates[i] = SIM_VALID;
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                case TelephonyManager.SIM_STATE_UNKNOWN:
                case TelephonyManager.SIM_STATE_ABSENT:
                    simStates[i] = SIM_ABSENT;
                    break;
                default:
                    simStates[i] = SIM_INVALID;
                    break;
            }

            Log.d(SUB_TAG, "sim" + i + " state is " + simStates[i]);
        }
    }

    public void updateSimInfo() {
        for (int i = 0; i < phoneCnt; i++) {
            if (simStates[i] == SIM_VALID) {
                IMSIs[i] = tm[i].getSubscriberId();
                phoneNums[i] = tm[i].getLine1Number();
                Log.d(SUB_TAG, "IMSI[" + i + "]: " + IMSIs[i] + "      Number[" + i + "]: " + phoneNums[i]);
            } else {
                IMSIs[i] = null;
                phoneNums[i] = null;
            }
        }
    }

    public void saveCurrentIMSIs() {
        for (int i = 0; i < phoneCnt; i++) {
            if (IMSIs[i] != null) {
                PersistentDataManager.getInstance().bindImsi(IMSIs[i], i);
            } else {
                PersistentDataManager.getInstance().bindImsi(null, i);
            }
        }
    }

    public void saveNotedIMSIs() {
        for (int i = 0; i < phoneCnt; i++) {
            if (IMSIs[i] != null) {
                PersistentDataManager.getInstance().setSimChangingNotedImsi(IMSIs[i], i);
            } else {
                PersistentDataManager.getInstance().setSimChangingNotedImsi(null, i);
            }
        }
    }

}
