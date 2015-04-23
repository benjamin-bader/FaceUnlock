package com.android.internal.policy;

import android.os.IBinder;

oneway interface IFaceLockCallback {
    void unlock();
    void cancel();
    void reportFailedAttempt();
    void pokeWakelock(int millis);
}
