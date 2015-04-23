package com.bendb.faceunlock;

/**
 * Callbacks invoked in response to Face Unlock events.
 */
public interface FaceUnlockCallback {
    /**
     * Indicates that the user's face has been recognized and passed
     * verification.
     */
    void unlock();

    /**
     * Indicates that Face Unlock has timed out or has been manually canceled.
     */
    void cancel();

    /**
     * Indicates that a face has been recognized but is not verified as the
     * owner's face.
     */
    void reportFailedAttempt();
}
