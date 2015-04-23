package com.bendb.faceunlock;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.android.internal.policy.IFaceLockCallback;
import com.android.internal.policy.IFaceLockInterface;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author bendb
 */
public class FaceUnlock implements Handler.Callback {
    private static final String TAG = "FaceUnlock";

    private static final String FACE_UNLOCK_PACKAGE = "com.android.facelock";

    private final Context mContext;
    private final FaceUnlockCallback mCallback;

    private Handler mHandler;
    private final int MSG_SERVICE_CONNECTED = 0;
    private final int MSG_SERVICE_DISCONNECTED = 1;
    private final int MSG_UNLOCK = 2;
    private final int MSG_CANCEL = 3;
    private final int MSG_FAILED_UNLOCK_ATTEMPT = 4;
    private final int MSG_POKE_WAKELOCK = 5;

    private final AtomicBoolean mIsRunning = new AtomicBoolean(false);
    private final AtomicBoolean mIsUiActive = new AtomicBoolean(false);
    private IFaceLockInterface mService;
    private View mFaceLockView;
    private PowerManager.WakeLock mWakeLock;

    private LockPatternUtils mUtils;

    public FaceUnlock(Context context, FaceUnlockCallback callback) {
        mContext = context;
        mCallback = callback;
        mHandler = new Handler(this);
    }

    public boolean isEnabled() {
        if (mHandler.getLooper() != Looper.myLooper()) {
            throw new IllegalStateException("isEnabled() called off of the main thread");
        }

        PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo(FACE_UNLOCK_PACKAGE, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return false;
        }

        DevicePolicyManager dpm = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm.getCameraDisabled(null)) {
            return false;
        }

        if (mUtils == null) {
            mUtils = new LockPatternUtils(mContext);
        }

        int mode = mUtils.getActivePasswordQuality();
        if (mode != DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK) {
            return false;
        }

        return true;
    }

    public boolean start(View faceUnlockView) {
        if (mHandler.getLooper() != Looper.myLooper()) {
            throw new IllegalStateException("start() called off of the main thread");
        }

        boolean wasRunning = mIsRunning.getAndSet(true);
        if (wasRunning) {
            Log.w(TAG, "start() called when already running");
            return true;
        }

        mFaceLockView = faceUnlockView;

        mHandler.removeMessages(MSG_SERVICE_CONNECTED);

        // TODO: In the official impl, they use a private overload of bindService
        //       that accepts a UserHandle param.  We don't have access to it.
        //       Do we need this?  Can we reflect in?
        mContext.bindService(
                new Intent(IFaceLockInterface.class.getName()).setPackage(FACE_UNLOCK_PACKAGE),
                mConnection,
                Context.BIND_AUTO_CREATE);

        return true;
    }

    public boolean stop() {
        if (mHandler.getLooper() != Looper.myLooper()) {
            throw new IllegalStateException("stop() called off of the main thread");
        }

        mHandler.removeMessages(MSG_SERVICE_CONNECTED);

        boolean wasRunning = mIsRunning.getAndSet(false);
        if (wasRunning) {
            stopUi();

            if (mService != null) {
                try {
                    mService.unregisterCallback(mFaceLockCallback);
                } catch (RemoteException e) {
                    // welp
                }
                mService = null;
            }
            mContext.unbindService(mConnection);
            mHandler.removeCallbacksAndMessages(null);
            mFaceLockView = null;
        }
        return wasRunning;
    }

    private void startUi(IBinder windowToken, int x, int y, int width, int height) {
        if (mIsUiActive.compareAndSet(false, true)) {
            try {
                mService.startUi(windowToken, x, y, width, height, false);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception starting Face Unlock: " + e);
            }
        }
    }

    private void stopUi() {
        if (mIsUiActive.compareAndSet(true, false)) {
            try {
                mService.stopUi();
            } catch (RemoteException e) {
                Log.e(TAG, "Exception stopping Face Unlock: " + e);
            }
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_SERVICE_CONNECTED:
                handleServiceConnected();
                break;
            case MSG_SERVICE_DISCONNECTED:
                handleServiceDisconnected();
                break;
            case MSG_UNLOCK:
                handleUnlock();
                break;
            case MSG_CANCEL:
                handleCancel();
                break;
            case MSG_FAILED_UNLOCK_ATTEMPT:
                handleFailedAttempt();
                break;
            case MSG_POKE_WAKELOCK:
                // wat do
                break;
            default:
                return false;
        }
        return true;
    }

    private void handleServiceConnected() {
        try {
            mService.registerCallback(mFaceLockCallback);
        } catch (RemoteException e) {
            mService = null;
            mIsRunning.set(false);
            return;
        }

        if (mFaceLockView != null) {
            IBinder windowToken = mFaceLockView.getWindowToken();
            if (windowToken != null) {
                int[] position = new int[2];
                mFaceLockView.getLocationInWindow(position);
                startUi(windowToken, position[0], position[1], mFaceLockView.getWidth(), mFaceLockView.getHeight());
            } else {
                Log.e(TAG, "windowToken is null in handleServiceConnected()");
            }
        }
    }

    private void handleServiceDisconnected() {
        if (mIsRunning.compareAndSet(true, false)) {
            mService = null;
        }
    }

    private void handleUnlock() {
        stop();
        mCallback.unlock();
    }

    private void handleCancel() {
        stop();
        mCallback.cancel();
    }

    private void handleFailedAttempt() {
        mCallback.reportFailedAttempt();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        /**
         * Called when the Face Unlock service connects after calling bind().
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = IFaceLockInterface.Stub.asInterface(iBinder);
            mHandler.sendEmptyMessage(MSG_SERVICE_CONNECTED);
        }

        /**
         * Called when the Face Unlock service unexpectedly disconnects.
         * Indicates an error.
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mHandler.sendEmptyMessage(MSG_SERVICE_DISCONNECTED);
        }
    };

    private final IFaceLockCallback mFaceLockCallback = new IFaceLockCallback.Stub() {
        @Override
        public void unlock() throws RemoteException {
            mHandler.sendEmptyMessage(MSG_UNLOCK);
        }

        @Override
        public void cancel() throws RemoteException {
            mHandler.sendEmptyMessage(MSG_CANCEL);
        }

        @Override
        public void reportFailedAttempt() throws RemoteException {
            mHandler.sendEmptyMessage(MSG_FAILED_UNLOCK_ATTEMPT);
        }

        @Override
        public void pokeWakelock(int millis) throws RemoteException {
            mHandler.sendEmptyMessage(MSG_POKE_WAKELOCK);
        }
    };
}
