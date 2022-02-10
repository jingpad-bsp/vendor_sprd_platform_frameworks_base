package com.unisoc.ccsa.rdc.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.SmsManager;
import com.unisoc.ccsa.Log;

import com.unisoc.ccsa.R;
import com.unisoc.ccsa.rdc.manager.PersistentDataManager;
import com.unisoc.ccsa.rdc.manager.SimsManager;
import com.unisoc.ccsa.rdc.receiver.AdminReceiver;

public class CmdPerformerService extends Service {
    public static final String SUB_TAG = "CmdPerformerService";

    public static final int CMD_FORCE_PASSWORD_AND_LOCK_SCREEN = 1;
    public static final int CMD_WIPE_DATA = 2;
    public static final int CMD_EXTRACT_CONTACTS = 3;
    public static final int CMD_EXTRACT_SMS = 4;
    public static final int CMD_SEND_SIM_CHANGE_MESSAGE = 5;
    public static final int CMD_SEND_CONFIRM_RELETIVES_MESSAGE = 6;
    public static final int CMD_LOCK_SCREEN = 7;
    public static final int CMD_REPLY_SMS = 100;
    public static final String SEND_RESULT_ACTION = "reply_sent_result";

    public static final int ARGS_SIZE_1 = 1;
    public static final int ARGS_SIZE_2 = 2;
    public static final int ARGS_SIZE_3 = 3;
    private static final int THREAD_NUM = 3;
    private static final int NOTIFY_STATE_1 = 1;
    private static final int NOTIFY_STATE_2 = 2;

    public static final String EXTRA_FROM_ADDRESS = "extra_from_address";
    public static final String EXTRA_ARGS = "extra_args";
    public static final String EXTRA_CMD = "extra_cmd";

    private DevicePolicyManager policyManager;
    private PersistentDataManager pdm;
    private ComponentName componentName;
    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;
    private ExecutorService es;
    private Context ctx;
    private int[] notifyStates;
    private static Method sGetLockPatternUtilsMethod;
    /**
     * receive sms sending result intent
     */
    private BroadcastReceiver sendingResultBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(SUB_TAG, "sending Intent onReceive");
            if (intent.getAction().equals(SEND_RESULT_ACTION)) {
                int phoneId = intent.getIntExtra("phoneId", -1);
                String notedImsi = intent.getStringExtra("notedImsi");
                String notedNum = intent.getStringExtra("notedNum");

                if (this.getResultCode() == Activity.RESULT_OK) {
                    pdm.setSimChangingNotedImsi(notedImsi, phoneId);
                    notifyStates[phoneId] = NOTIFY_STATE_2;
                } else {
                    pdm.setSimChangingNotedImsi(null, phoneId);
                    ArrayList<String> args = new ArrayList<String>();

                    args.add(notedImsi);
                    args.add(notedNum);
                    args.add("" + phoneId);
                    Intent it = new Intent(ctx, CmdPerformerService.class);
                    intent.putExtra(CmdPerformerService.EXTRA_CMD,
                            CmdPerformerService.CMD_SEND_SIM_CHANGE_MESSAGE);
                    intent.putExtra(CmdPerformerService.EXTRA_ARGS, args);
                    doCommands(it);
                }
            }
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this;
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_DIM_WAKE_LOCK, SUB_TAG);
        policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, AdminReceiver.class);

        pdm = PersistentDataManager.getInstance();
        if (pdm == null) {
            return;
        }

        notifyStates = new int[SimsManager.getInstance(ctx).getPhoneCnt()];
        es = Executors.newFixedThreadPool(THREAD_NUM);
        this.registerReceiver(sendingResultBroadcastReceiver, new IntentFilter(
                SEND_RESULT_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Method methodSetActiveAdmin;
        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {

                methodSetActiveAdmin = DevicePolicyManager.class.getMethod(
                        "setActiveAdmin", ComponentName.class);
                methodSetActiveAdmin.invoke(policyManager, componentName);
            } else {
                methodSetActiveAdmin = DevicePolicyManager.class.getMethod(
                        "setActiveAdmin", ComponentName.class, boolean.class);
                methodSetActiveAdmin.invoke(policyManager, componentName, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Runnable r = doCommands(intent);
        if (r != null && es != null) {
            es.submit(r);
        }
        doCommands(intent);

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        componentName = null;
        policyManager = null;
        pdm = null;
        es = null;

        unregisterReceiver(sendingResultBroadcastReceiver);
        super.onDestroy();
    }

    private Runnable doCommands(Intent intent) {

        final int cmd = intent.getIntExtra(EXTRA_CMD, 0);
        final String fromAddress = intent.getStringExtra(EXTRA_FROM_ADDRESS);
        final ArrayList<String> args = intent
                .getStringArrayListExtra(EXTRA_ARGS);
        if (!pdm.isOn() && cmd != CMD_LOCK_SCREEN) {
            Log.d(SUB_TAG, "Module is off while the command is coming.");
            return null;
        }

        switch (cmd) {
            case CMD_FORCE_PASSWORD_AND_LOCK_SCREEN:
                if (!verifyIncomingAddr(fromAddress))
                    return null;
                if (args == null) {
                    return null;
                }
                if (!verifyPasswd(args.get(0)))
                    return null;
                if (args.size() != ARGS_SIZE_2) {
                    return null;
                }

                return new Runnable() {
                    @Override
                    public void run() {
                        doResetPassword(args.get(1));
                    }
                };
            case CMD_WIPE_DATA:
                if (!verifyIncomingAddr(fromAddress))
                    return null;
                if (args == null) {
                    return null;
                }
                if (!verifyPasswd(args.get(0)))
                    return null;
                if (args.size() != ARGS_SIZE_1) {
                    return null;
                }

                if (!pdm.getPassword().equals(args.get(0))) {
                    return null;
                }

                Log.d(SUB_TAG, "return wipe data runnable");
                return new Runnable() {
                    @Override
                    public void run() {
                        doWipeData();
                        doReplyShortMessage(
                                CMD_WIPE_DATA,
                                fromAddress,
                                ctx.getResources().getString(
                                        R.string.data_has_been_clean));
                    }
                };
            case CMD_REPLY_SMS:
                if (args == null) {
                    return null;
                }
                if (args.size() != ARGS_SIZE_2) {
                    return null;
                }

                if (!args.get(0).isEmpty() && !args.get(1).isEmpty()) {
                    return new Runnable() {
                        @Override
                        public void run() {
                            doReplyShortMessage(Integer.parseInt(args.get(0)),
                                    args.get(1), pdm.getReletivesNum());
                        }
                    };
                }
                break;
            case CMD_SEND_SIM_CHANGE_MESSAGE:
                if (args == null) {

                    return null;
                }
                if (args.size() != ARGS_SIZE_3) {

                    return null;
                }

                return new Runnable() {
                    @Override
                    public void run() {
                        doSimChange(args.get(0), args.get(1), args.get(2));
                    }
                };
            case CMD_SEND_CONFIRM_RELETIVES_MESSAGE:
                return new Runnable() {
                    @Override
                    public void run() {
                        confirmReletives();
                    }
                };
            case CMD_LOCK_SCREEN:
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (getIsSecure()) {
                                policyManager.lockNow();
                                wakeLock.acquire(10 * 1000);
                            }
                        } catch (Exception e) {
                            return;
                        }
                    }
                };
            default:
                return null;
        }

        return null;
    }

    private boolean verifyPasswd(String passwd) {
        if (pdm.getPassword().equals(passwd)) {
            return true;
        }

        Log.d(SUB_TAG, "password is wrong.");
        return false;
    }

    private boolean verifyIncomingAddr(String addr) {
        if (pdm.getReletivesNum().equals(addr.substring(3))
                || pdm.getReletivesNum().equals(addr)) {
            return true;
        }

        return false;
    }

    private void confirmReletives() {
        if (pdm.getReletivesNum() == null) {
            return;
        }

        doReplyShortMessage(CMD_SEND_CONFIRM_RELETIVES_MESSAGE,
                pdm.getReletivesNum(),
                ctx.getResources().getString(R.string.relative_num_note));
    }

    private synchronized void doSimChange(String currImsi, String currNum,
                                          String phoneId) {
        if (pdm.getReletivesNum() == null) {

            return;
        }

        int id = Integer.parseInt(phoneId);

        if (notifyStates[id] != 0) {

            return;
        }

        for (int i = 0; i < SimsManager.getInstance(ctx).getPhoneCnt(); i++) {
            if (pdm.getSimChangingNotedImsi(i) != null
                    && pdm.getSimChangingNotedImsi(i).equals(currImsi)) {
                notifyStates[id] = NOTIFY_STATE_2;

                return;
            }
        }

        Intent intent = new Intent(SEND_RESULT_ACTION)
                .putExtra("notedImsi", currImsi).putExtra("phoneId", id)
                .putExtra("notedNum", currNum);
        PendingIntent pi = PendingIntent.getBroadcast(this,
                CMD_SEND_SIM_CHANGE_MESSAGE, intent, 0);
        doReplyShortMessage(CMD_SEND_SIM_CHANGE_MESSAGE, pdm.getReletivesNum(),
                ctx.getResources().getString(R.string.sim_change_note), pi, id);
        notifyStates[id] = NOTIFY_STATE_1;
    }

    private void doResetPassword(String password) {
        try {
            policyManager.lockNow();
            policyManager.resetPassword(password,
                    DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void doWipeData() {

        try {

            Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            intent.putExtra("android.intent.extra.WIPE_EXTERNAL_STORAGE", false);

            this.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        policyManager.wipeData(0);
    }

    private void doReplyShortMessage(int cmd, String address, String feedback,
                                     PendingIntent pi, int phoneId) {
        SimsManager sm = SimsManager.getInstance(ctx);

        if (address == null)
            return;

        if (phoneId != -1) {
            SmsManager.getDefault().sendTextMessage(address, null, feedback,
                    pi, null);
        } else {

            SmsManager.getDefault().sendTextMessage(address, null, feedback,
                    null, null);
        }
    }

    private void doReplyShortMessage(int cmd, String address, String feedback) {
        doReplyShortMessage(cmd, address, feedback, null, -1);
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
                instance = constructor.newInstance(getApplicationContext());
                sGetLockPatternUtilsMethod = clazz.getMethod("isSecure", int.class);
            }
        } catch (Exception e) {
            sGetLockPatternUtilsMethod = null;
            Log.e(SUB_TAG, "Exception", e);
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
            Log.e(SUB_TAG, "IllegalAccessException", e);
        } catch (InvocationTargetException e) {
            Log.e(SUB_TAG, "InvocationTargetException", e);
        } catch (NullPointerException e) {
            Log.e(SUB_TAG, "NullPointerException", e);
        }
        return secure;
    }

}
