package com.unisoc.ccsa.rdc.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.unisoc.ccsa.Log;

import com.unisoc.ccsa.rdc.manager.PersistentDataManager;
import com.unisoc.ccsa.rdc.manager.SimsManager;

public class BootReceiver extends BroadcastReceiver {
    public static final String SUB_TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(SUB_TAG, "on BOOT_COMPLETED");
        int cnt = SimsManager.getInstance(context).getPhoneCnt();
        for (int i = 0; i < cnt; i++) {
            PersistentDataManager.getInstance().setSimChangingNotedImsi(null, i);
        }
    }
}
