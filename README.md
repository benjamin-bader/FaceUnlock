Face Unlock API
===============

Enable Face Unlock in your own apps.

```java
public class MainActivity extends Activity {
  private FaceUnlock faceUnlock;
  private SurfaceView unlockSurface;
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
        if (faceUnlock.isEnabled()) {
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

```
compile 'com.bendb.faceunlock:faceunlock:0.1.0-SNAPSHOT@aar'
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
