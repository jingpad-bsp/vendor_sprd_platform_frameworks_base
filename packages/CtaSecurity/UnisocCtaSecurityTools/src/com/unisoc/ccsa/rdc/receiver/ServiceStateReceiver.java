package com.unisoc.ccsa.rdc.receiver;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.telephony.ServiceState;

import com.unisoc.ccsa.rdc.manager.PersistentDataManager;
import com.unisoc.ccsa.rdc.manager.SimsManager;
import com.unisoc.ccsa.rdc.service.CmdPerformerService;

public class ServiceStateReceiver extends BroadcastReceiver {
    private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private final static String ACTION_SERVICE_STATE_CHANGED = "android.intent.action.SERVICE_STATE";
    private final static String SUB_TAG = "ServiceStateReceiver";
    public static final String EXTRA_ARGS = CmdPerformerService.EXTRA_ARGS;
    public static final String EXTRA_PHONE_ID = "phone_id";
    private SimsManager sm = null;
    private PersistentDataManager pm = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int phoneId = -1;
        if (action.startsWith(ACTION_SERVICE_STATE_CHANGED)) {
            if (action.length() == ACTION_SERVICE_STATE_CHANGED.length() + 1) {
                String id = action.substring(action.length() - 1);
                phoneId = Integer.parseInt(id);
            }

            Method mNewFromBundle;
            ServiceState ss = null;

            try {
                mNewFromBundle = ServiceState.class.getMethod("newFromBundle",
                        Bundle.class);
                ss = (ServiceState) mNewFromBundle.invoke(null,
                        intent.getExtras());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (ss != null && ss.getState() == ServiceState.STATE_IN_SERVICE) {
                pm = PersistentDataManager.getInstance();
                sm = SimsManager.getInstance(context);
                sm.onSimChanged();
                if (phoneId == -1) {
                    phoneId = intent.getIntExtra(EXTRA_PHONE_ID, -1);
                }
                notifyService(context, phoneId);
            }
        }
    }

    private void notifyService(Context ctx, int phoneId) {
        int matchFlag = 0;

        if (phoneId != -1) {
            String currImsi = sm.getIMSIs(phoneId);
            for (int j = 0; j < sm.getPhoneCnt(); j++) {
                String savedImsi = pm.getBindingImsi(j);

                if (savedImsi == null || !savedImsi.equals(currImsi)) {
                    matchFlag++;
                    continue;
                }

                matchFlag = 0;
                break;
            }

            if (matchFlag > 0) {

                ArrayList<String> args = new ArrayList<String>();
                args.add(sm.getIMSIs(phoneId));
                args.add(sm.getPhoneNums(phoneId));
                args.add("" + phoneId);
                Intent intent = new Intent(ctx, CmdPerformerService.class);
                intent.putExtra(CmdPerformerService.EXTRA_CMD,
                        CmdPerformerService.CMD_SEND_SIM_CHANGE_MESSAGE);
                intent.putExtra(CmdPerformerService.EXTRA_ARGS, args);

                if (ctx != null) {
                    ctx.startService(intent);
                }
            }
            return;
        }


        for (int i = 0; i < sm.getPhoneCnt(); i++) {
            String currImsi = sm.getIMSIs(i);
            if (currImsi != null) {

                for (int j = 0; j < sm.getPhoneCnt(); j++) {
                    String savedImsi = pm.getSimChangingNotedImsi(j);

                    if (savedImsi == null || !savedImsi.equals(currImsi)) {
                        matchFlag++;
                        continue;
                    }

                    matchFlag = 0;
                    break;
                }

                if (matchFlag > 0) {

                    ArrayList<String> args = new ArrayList<String>();
                    args.add(sm.getIMSIs(i));
                    args.add(sm.getPhoneNums(i));
                    args.add("" + i);
                    Intent intent = new Intent(ctx, CmdPerformerService.class);
                    intent.putExtra(CmdPerformerService.EXTRA_CMD,
                            CmdPerformerService.CMD_SEND_SIM_CHANGE_MESSAGE);
                    intent.putExtra(CmdPerformerService.EXTRA_ARGS, args);

                    if (ctx != null) {
                        ctx.startService(intent);
                    }
                }
            }
        }
    }
}
