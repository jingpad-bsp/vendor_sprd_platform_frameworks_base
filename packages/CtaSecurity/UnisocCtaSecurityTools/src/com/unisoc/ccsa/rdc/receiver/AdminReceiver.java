package com.unisoc.ccsa.rdc.receiver;


import java.util.ArrayList;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import com.unisoc.ccsa.Log;

import com.unisoc.ccsa.R;
import com.unisoc.ccsa.rdc.service.CmdPerformerService;

public class AdminReceiver extends DeviceAdminReceiver {
    public static final String SUB_TAG = "AdminManager";

    @Override
    public DevicePolicyManager getManager(Context context) {
        return super.getManager(context);
    }

    @Override
    public ComponentName getWho(Context context) {
        return super.getWho(context);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.d(SUB_TAG, "device manager has been disabled.");
        super.onDisabled(context, intent);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return super.onDisableRequested(context, intent);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.d(SUB_TAG, "device manager has been enabled.");
        super.onEnabled(context, intent);
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        super.onPasswordSucceeded(context, intent);
    }

    @Override
    public IBinder peekService(Context myContext, Intent service) {
        return super.peekService(myContext, service);
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        Log.d(SUB_TAG, "password has been changed.");
        Intent cmdIntent = new Intent(context, CmdPerformerService.class);
        cmdIntent.putExtra(CmdPerformerService.EXTRA_CMD, CmdPerformerService.CMD_REPLY_SMS);
        ArrayList<String> argsList = new ArrayList<String>();
        argsList.add("" + CmdPerformerService.CMD_FORCE_PASSWORD_AND_LOCK_SCREEN);
        argsList.add(context.getResources().getString(R.string.lock_password_change_success));
        cmdIntent.putExtra(CmdPerformerService.EXTRA_ARGS, argsList);

        super.onPasswordChanged(context, intent);
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
    }

}
