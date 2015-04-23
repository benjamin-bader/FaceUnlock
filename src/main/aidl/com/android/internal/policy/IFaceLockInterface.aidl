package com.android.internal.policy;

import android.os.IBinder;
import com.android.internal.policy.IFaceLockCallback;

interface IFaceLockInterface {
    void startUi(IBinder containsWindowToken, int x, int y, int width, int height, boolean useLiveness);
    void stopUi();
    void registerCallback(IFaceLockCallback cb);
    void unregisterCallback(IFaceLockCallback cb);
}
