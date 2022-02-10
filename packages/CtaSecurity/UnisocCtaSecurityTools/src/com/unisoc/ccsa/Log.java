package com.unisoc.ccsa;

public class Log {
    private static final String TAG = "CCSA:";
    private static boolean DEBUG = true;

    public static void i(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.i(TAG + tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            android.util.Log.e(TAG + tag, msg, tr);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.e(TAG + tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            android.util.Log.e(TAG + tag, msg, tr);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.w(TAG + tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            android.util.Log.w(TAG + tag, msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.d(TAG + tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            android.util.Log.d(TAG + tag, msg, tr);
        }
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.d(TAG + tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            android.util.Log.v(TAG + tag, msg, tr);
        }
    }
}
