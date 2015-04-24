/*
 *  * Copyright (C) 2015 Benjamin Bader
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
