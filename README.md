Face Unlock API
===============

Enable Face Unlock in your own apps
-----------------------------------

Unlocking your phone by looking at it is cool.  Why not unlock your apps by looking at them, too?

A few phones currently have Face Unlock enabled, and increasingly more are shipping with it.  While the underlying technology is closed and proprietary, the interface to it is merely private.  This library exposes an public API into the private unlock service, allowing you to make use of it it your own apps.

Limitations
-----------

This library will only work if:
* The phone has Face Unlock
* The owner has configured and enabled Face Unlock
* The owner *also* has another form of password enabled

Example
-------

```java
public class MainActivity extends Activity {
  // This is your entry point into Face Unlock
  private FaceUnlock faceUnlock;
  
  // FaceUnlock needs a surface on which to render the
  // camera UI
  private SurfaceView unlockSurface;
  
  // Just something to trigger the unlock sequence
  private Button unlockButton;

  @Override
  public void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);
    setContentView(R.layout.activity_main);

    unlockSurface = (SurfaceView) findViewById(R.id.surface_view);
    unlockButton = (Button) findViewById(R.id.unlock_button);

    faceUnlock = new FaceUnlock(this, new FaceUnlockCallback() {
      @Override
      public void cancel() {
        Log.i("MainActivity", "Face unlock timed out or was canceled");
      }

      @Override
      public void unlock() {
        Log.i("MainActivity", "Face unlock succeeded");
      }

      @Override
      public void reportFailedAtempt() {
        Log.i("MainActivity", "Face unlock recognized a face but it wasn't the correct face");
      }
    });

    unlockButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Note that not all devices support face unlock, and not
        // everyone on such a device has it turned on.  Be sure to
        // check this!
        if (faceUnlock.isEnabled()) {
          // This will trigger the unlock UI.
          faceUnlock.start(unlockSurface);
        }
      }
    });
  }
}
```

Download
========

Face Unlock API is currently available as snapshot in the Sonatype repo, pending
further testing:

```gradle
// in build.gradle

repositories {
  maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
}

dependencies {
  compile 'com.bendb.faceunlock:faceunlock:0.1.0-SNAPSHOT@aar'
}
```

License
=======

```
Copyright 2015 Benjamin Bader

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
