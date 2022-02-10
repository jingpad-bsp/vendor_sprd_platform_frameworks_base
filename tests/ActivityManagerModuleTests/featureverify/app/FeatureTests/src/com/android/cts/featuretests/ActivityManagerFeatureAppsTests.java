package com.android.cts.featuretests;

import android.app.ActivityDebugConfigs;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.test.AndroidTestCase;
import android.os.SystemClock;
import android.util.Log;

import com.android.compatibility.common.util.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

public class ActivityManagerFeatureAppsTests extends AndroidTestCase {

    public static final String TAG = "ActivityManagerFeatureAppsTests";
    private static final String LOG_SEPARATOR = "LOG_SEPARATOR";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected static class LogSeparator {
        private final String mUniqueString;

        private LogSeparator() {
            mUniqueString = UUID.randomUUID().toString();
        }

        @Override
        public String toString() {
            return mUniqueString;
        }
    }

    /**
     * Inserts a log separator so we can always find the starting point from where to evaluate
     * following logs.
     * @return Unique log separator.
     */
    protected LogSeparator separateLogs() {
        final LogSeparator logSeparator = new LogSeparator();
        executeShellCommand("log -t " + LOG_SEPARATOR + " " + logSeparator);
        return logSeparator;
    }

    protected static String[] getDeviceLogsForComponents(
            LogSeparator logSeparator, String... logTags) {
        String filters = LOG_SEPARATOR + ":I ";
        for (String component : logTags) {
            filters += component + ":I ";
        }
        final String[] result = executeShellCommand("logcat -v brief -d " + filters + " *:S")
                .split("\\n");
        if (logSeparator == null) {
            return result;
        }

        // Make sure that we only check logs after the separator.
        int i = 0;
        boolean lookingForSeparator = true;
        while (i < result.length && lookingForSeparator) {
            if (result[i].contains(logSeparator.toString())) {
                lookingForSeparator = false;
            }
            i++;
        }
        final String[] filteredResult = new String[result.length - i];
        for (int curPos = 0; i < result.length; curPos++, i++) {
            filteredResult[curPos] = result[i];
        }
        return filteredResult;
    }

    public void assertInLogcat(String logTag, String entry, LogSeparator logSeparator) {
        final Pattern pattern = Pattern.compile("(.+)" + entry);
        for (int retry = 1; retry <= 5; retry++) {
            final String[] lines = getDeviceLogsForComponents(logSeparator, logTag);
            for (int i = lines.length - 1; i >= 0; i--) {
                final String line = lines[i];
                Log.d(TAG, line);
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    return;
                }
            }
            Log.d(TAG, "Waiting for " + entry + "... retry=" + retry);
            SystemClock.sleep(500);
        }
        fail("Waiting for " + entry + " failed");
    }

    protected static Instrumentation getInstrumentation() {
        return InstrumentationRegistry.getInstrumentation();
    }

    private static void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {}
    }

    public static String executeShellCommand(String command) {
        Log.d(TAG, "Shell command: " + command);
        try {
            return SystemUtil
                    .runShellCommand(getInstrumentation(), command);
        } catch (IOException e) {
            //bubble it up
            Log.e(TAG, "Error running shell command: " + command);
            throw new RuntimeException(e);
        }
    }

    public void testLongPressPowerReboot() throws Exception {
        //KEYCODE_POWER == 26, 5 denote 5s
        executeShellCommand("input keyevent --longpress 5 26");
        //wait a while to guarantee UI stably
        sleep(3000);

        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());

        UiObject restart = uiDevice.findObject(new UiSelector().textMatches("Restart|重新启动"));
	restart.click();
	UiObject ok = new UiObject(new UiSelector().textMatches("OK|是"));
	if (ok != null) ok.click();
    }

    public void testActivityDebugConfigsPackageBundle() throws RuntimeException {
        // Reset at first.
        executeShellCommand("cmd activity log_switch reset");

        final String DEBUG_KEY = "DEBUG_BUNDLE";
        final String PACKAGE_NAME = "com.android.cts.featuretests";

        testActivityDebugConfigsPackageBundle(PACKAGE_NAME, DEBUG_KEY,
                true /*ignoreEmptyExtra*/, true /*expectedResult*/);
        testActivityDebugConfigsPackageBundle(PACKAGE_NAME, DEBUG_KEY,
                false /*ignoreEmptyExtra*/, false /*expectedResult*/);
        testActivityDebugConfigsPackageBundle(PACKAGE_NAME, DEBUG_KEY,
                true /*ignoreEmptyExtra*/, false /*expectedResult*/, "reset" /*rawCommand*/);
    }

    private void testActivityDebugConfigsPackageBundle(String packageName, String debugKey,
            boolean ignoreEmptyExtra, boolean expectedResult) {
        testActivityDebugConfigsPackageBundle(packageName, debugKey, ignoreEmptyExtra,
                expectedResult, null /*rawCommand*/);
    }

    private void testActivityDebugConfigsPackageBundle(String packageName, String debugKey,
            boolean ignoreEmptyExtras, boolean expectedResult, String rawCommand) {
        final String PACKAGE_NAME = packageName;
        final String DEBUG_KEY = debugKey;
        final boolean IGNORE_EMPTY_EXTRAS = ignoreEmptyExtras;
        final boolean EXPECTED = expectedResult;

        final ActivityDebugConfigs.ConfigChangedListener listener =
                new ActivityDebugConfigs.ConfigChangedListener() {
            private boolean mFirst = true;
            @Override
            public void onDebugConfigChanged(ActivityDebugConfigs config) {
                Log.d(TAG, "Receive DebugConfig: " + config);
                // Ignore the intial state
                if (config == null) {
                    mFirst = false;
                    return;
                }

                if (config.getExtras() != null) {
                    // Ignore the case of reset
                    assertFalse("Config should contains extras",
                            !mFirst && !IGNORE_EMPTY_EXTRAS && config.getExtras().isEmpty());
                    mFirst = false;
                    if (config.getExtras().isEmpty()) return;

                    assertTrue("Should contains key of extras named: " + DEBUG_KEY,
                            config.getExtras().containsKey(DEBUG_KEY));

                    assertEquals(DEBUG_KEY + " must be " + EXPECTED,
                            EXPECTED, config.getExtras().getBoolean(DEBUG_KEY, false));
                    ActivityDebugConfigs.removeConfigChangedListener(this);
                }

                synchronized (this) {
                    notify();
                }
            }
        };
        ActivityDebugConfigs.addConfigChangedListener(listener);

        if (rawCommand == null) {
            rawCommand = "pkg-bundle " + PACKAGE_NAME + " --ez " + DEBUG_KEY + " " + EXPECTED;
        }
        executeShellCommand("cmd activity log_switch pkg-bundle " + rawCommand);

        synchronized (listener) {
            try {
                listener.wait(2000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void testPluginAddon() throws Exception {
        final LogSeparator logSeparator = separateLogs();
        executeShellCommand("am start -n test.sprd.helloworld/.HelloWorldActivity");
        //entry need according to the future and choose the special string which can clear the feature is OK
        assertInLogcat("AddonManager", "plugin.sprd.helloworld(.*)", logSeparator);
    }
}
