package android.featureverify.cts;

import com.android.compatibility.common.tradefed.build.CompatibilityBuildHelper;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestResult.TestStatus;
import com.android.tradefed.build.IBuildInfo;
import com.android.tradefed.config.ConfigurationException;
import com.android.tradefed.config.OptionSetter;
import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.log.LogUtil.CLog;
import com.android.tradefed.result.CollectingTestListener;
import com.android.tradefed.result.TestDescription;
import com.android.tradefed.result.TestResult;
import com.android.tradefed.result.TestRunResult;
import com.android.tradefed.testtype.DeviceTestCase;
import com.android.tradefed.testtype.IBuildReceiver;

import com.google.common.base.Charsets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;
import java.util.Map;

import javax.annotation.Nullable;

public class ActivityManagerFeatureTest extends DeviceTestCase implements IBuildReceiver {

    protected static final String AMS_FEATURE_TESTS_PKG = "com.android.cts.featuretests";
    protected static final String AMS_FEATURE_TESTS_CLASS = AMS_FEATURE_TESTS_PKG + ".ActivityManagerFeatureAppsTests";
    protected static final String AMS_FEATURE_TESTS_APK = "CtsAMSFeatureAppsTests.apk";

    protected static final String HELLO_WORLD_APK = "HelloWorld.apk";
    protected static final String HELLO_WORLD_PLUGIN_APK = "HelloWorldPlugin.apk";

    protected static final String TEST_SPRD_HELLOWORLD_PACKAGE = "test.sprd.helloworld";
    protected static final String PLUGIN_SPRD_HELLOWORLD_PACKAGE = "plugin.sprd.helloworld";

    private static final String RUNNER = "android.support.test.runner.AndroidJUnitRunner";

    /**
    * A helper to access resources in the build.
    */
    private CompatibilityBuildHelper mBuildHelper;

    /**
     * The defined timeout (in milliseconds) is used as a maximum waiting time when expecting the
     * command output from the device. At any time, if the shell command does not output anything
     * for a period longer than defined timeout the Tradefed run terminates.
     */
    private static final long DEFAULT_SHELL_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(20);

    /** instrumentation test runner argument key used for individual test timeout */
    protected static final String TEST_TIMEOUT_INST_ARGS_KEY = "timeout_msec";

    /**
     * Sets timeout (in milliseconds) that will be applied to each test. In the
     * event of a test timeout it will log the results and proceed with executing the next test.
     */
    private static final long DEFAULT_TEST_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10);

    protected int mPrimaryUserId;

    protected IBuildInfo mCtsBuild;

    protected ITestDevice mDevice;

    @Override
    public void setBuild(IBuildInfo buildInfo) {
        mCtsBuild = buildInfo;
        // Get the build, this is used to access the APK.
        mBuildHelper = new CompatibilityBuildHelper(buildInfo);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDevice = getDevice();
        mPrimaryUserId = getPrimaryUser();
        assertNotNull(mCtsBuild);  // ensure build has been set before test is run.
        installTestApps();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected int getPrimaryUser() throws DeviceNotAvailableException {
        return getDevice().getPrimaryUserId();
    }

    protected void installAppAsUser(String appFileName, int userId) throws FileNotFoundException,
            DeviceNotAvailableException {
        installAppAsUser(appFileName, true, userId);
    }

    protected void installAppAsUser(String appFileName, boolean grantPermissions, int userId)
            throws FileNotFoundException, DeviceNotAvailableException {
        CLog.d("Installing app " + appFileName + " for user " + userId);
        CompatibilityBuildHelper buildHelper = new CompatibilityBuildHelper(mCtsBuild);
        String result = mDevice.installPackageForUser(
                buildHelper.getTestFile(appFileName), true, grantPermissions, userId, "-t");
        assertNull("Failed to install " + appFileName + " for user " + userId + ": " + result,
                result);
    }

    protected void installTestApps() throws Exception {
        uninstallTestApps();
        installAppAsUser(AMS_FEATURE_TESTS_APK, mPrimaryUserId);
    }

    protected void uninstallTestApps() throws Exception {
        mDevice.uninstallPackage(AMS_FEATURE_TESTS_PKG);
    }

    protected void runDeviceTestsAsUser(
            String pkgName, @Nullable String testClassName, String testMethodName, int userId)
            throws DeviceNotAvailableException {
        Map<String, String> params = Collections.emptyMap();
        runDeviceTestsAsUser(pkgName, testClassName, testMethodName, userId, params);
    }

    protected void runDeviceTestsAsUser(
            String pkgName, @Nullable String testClassName,
            @Nullable String testMethodName, int userId,
            Map<String, String> params) throws DeviceNotAvailableException {
        if (testClassName != null && testClassName.startsWith(".")) {
            testClassName = pkgName + testClassName;
        }

        RemoteAndroidTestRunner testRunner = new RemoteAndroidTestRunner(
                pkgName, RUNNER, mDevice.getIDevice());
        testRunner.setMaxTimeToOutputResponse(DEFAULT_SHELL_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        testRunner.addInstrumentationArg(
                TEST_TIMEOUT_INST_ARGS_KEY, Long.toString(DEFAULT_TEST_TIMEOUT_MILLIS));
        if (testClassName != null && testMethodName != null) {
            testRunner.setMethodName(testClassName, testMethodName);
        } else if (testClassName != null) {
            testRunner.setClassName(testClassName);
        }

        for (Map.Entry<String, String> param : params.entrySet()) {
            testRunner.addInstrumentationArg(param.getKey(), param.getValue());
        }

        CollectingTestListener listener = new CollectingTestListener();
        assertTrue(mDevice.runInstrumentationTestsAsUser(testRunner, userId, listener));

        final TestRunResult result = listener.getCurrentRunResults();
        //testLongPressPowerReboot would reboot in device so ignore it,and the following is also
        if (result.isRunFailure() && !testMethodName.equals("testLongPressPowerReboot")) {
            throw new AssertionError("Failed to successfully run device tests for "
                    + result.getName() + ": " + result.getRunFailureMessage());
        }
        if (result.getNumTests() == 0 && !testMethodName.equals("testLongPressPowerReboot")) {
            throw new AssertionError("No tests were run on the device");
        }

        if (result.hasFailedTests()) {
            // build a meaningful error message
            StringBuilder errorBuilder = new StringBuilder("On-device tests failed:\n");
            for (Map.Entry<TestDescription, TestResult> resultEntry :
                    result.getTestResults().entrySet()) {
                if (!resultEntry.getValue().getStatus().equals(TestStatus.PASSED)) {
                    errorBuilder.append(resultEntry.getKey().toString());
                    errorBuilder.append(":\n");
                    errorBuilder.append(resultEntry.getValue().getStackTrace());
                }
            }
            if (!testMethodName.equals("testLongPressPowerReboot")
                    || !errorBuilder.toString().contains("Test run failed to complete"))
                throw new AssertionError(errorBuilder.toString());
        }
    }

    public void testFeatureLongPressPowerReboot() throws Exception {
        runDeviceTestsAsUser(AMS_FEATURE_TESTS_PKG,
                AMS_FEATURE_TESTS_CLASS,
                "testLongPressPowerReboot",
                mPrimaryUserId);
        //wait a while to avoid waitForBootComplete invalid
        Thread.sleep(5000);

        assertTrue("Device failed to boot", mDevice.waitForBootComplete(120000));
    }

    public void testFeaturePluginAddon() throws Exception {
        try {
            installAppAsUser(HELLO_WORLD_APK, mPrimaryUserId);
            installAppAsUser(HELLO_WORLD_PLUGIN_APK, mPrimaryUserId);
            //this test case can also handle in host, not runDeviceTestsAsUser
            //mDevice.executeShellCommand("am start -n test.sprd.helloworld/.HelloWorldActivity");
            //checkLogcatForText("AddonManager", "plugin.sprd.helloworld", 5000);
            runDeviceTestsAsUser(AMS_FEATURE_TESTS_PKG,
                    AMS_FEATURE_TESTS_CLASS,
                    "testPluginAddon",
                    mPrimaryUserId);
        } finally {
            mDevice.uninstallPackage(PLUGIN_SPRD_HELLOWORLD_PACKAGE);
            mDevice.uninstallPackage(TEST_SPRD_HELLOWORLD_PACKAGE);
        }
    }

    public void testActivityDebugConfigs() throws Exception {
        runDeviceTestsAsUser(AMS_FEATURE_TESTS_PKG,
                AMS_FEATURE_TESTS_CLASS,
                "testActivityDebugConfigsPackageBundle",
                mPrimaryUserId);
        //wait a while to avoid waitForBootComplete invalid
        Thread.sleep(5000);

        assertTrue("Device failed to boot", mDevice.waitForBootComplete(120000));

    }
}
