package com.bendb.faceunlock;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A reflection-based proxy delegating to the internal LockPatternUtils class.
 */
class LockPatternUtils {
    private static final String TAG = "LockPatternUtils";
    private static final String LOCK_PATTERN_UTILS = "com.android.internal.widget.LockPatternUtils";
    private static final String GET_ACTIVE_PASSWORD_QUALITY = "getActivePasswordQuality";

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final Object sLock = new Object();
    private static boolean sInitialized;
    private static Constructor sLockPatternUtilsCtor;
    private static Method sGetActivePasswordQuality;

    private final Object mLockPatternUtils;

    public LockPatternUtils(Context context) {
        initialize();

        Object instance = null;
        if (sLockPatternUtilsCtor != null) {
            try {
                instance = sLockPatternUtilsCtor
                        .newInstance(context.getApplicationContext());
            } catch (InvocationTargetException
                    | InstantiationException
                    | IllegalAccessException e) {
                Log.e(TAG, "Error constructing a LockPatternUtils: " + e);
            }
        }

        mLockPatternUtils = instance;
    }

    private static void initialize() {
        synchronized (sLock) {
            if (!sInitialized) {
                try {
                    Class<?> sLockPatternUtils = Class.forName(LOCK_PATTERN_UTILS);
                    sGetActivePasswordQuality = sLockPatternUtils.getDeclaredMethod(GET_ACTIVE_PASSWORD_QUALITY);
                    sLockPatternUtilsCtor = sLockPatternUtils.getConstructor(Context.class);
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                    sGetActivePasswordQuality = null;
                    sLockPatternUtilsCtor = null;
                } finally {
                    sInitialized = true;
                }
            }
        }
    }

    /**
     * Obtains the active user's current password quality.
     *
     * <p>This differs from {@link DevicePolicyManager#getPasswordQuality(android.content.ComponentName)}
     * in that it will return {@link DevicePolicyManager#PASSWORD_QUALITY_BIOMETRIC_WEAK} if
     * Face Unlock is enabled, despite the fact that a stronger password is also set.
     */
    public int getActivePasswordQuality() {
        int result = DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
        if (mLockPatternUtils != null) {
            try {
                result = (Integer) sGetActivePasswordQuality.invoke(mLockPatternUtils);
            } catch (InvocationTargetException | IllegalAccessException e) {
                if (DEBUG) Log.e(TAG,
                        "Error invoking LockPatternUtils.getActivePasswordQuality: " + e);
            }
        }
        return result;
    }
}
