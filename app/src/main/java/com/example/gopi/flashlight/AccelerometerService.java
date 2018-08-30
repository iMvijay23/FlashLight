package com.example.gopi.flashlight;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class AccelerometerService extends Service implements SensorEventListener {
    CameraManager mCameraManager;
    SensorManager sensorManager;
    Sensor accelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 1000;
    private boolean isTorchEnabled = true;
    private int count = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager.registerListener
                (AccelerometerService.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
                if (/*speed > SHAKE_THRESHOLD &&*/ x > 7) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (isTorchEnabled) {
                                count++;
                                if (count == 4) {
                                    mCameraManager.setTorchMode("0", isTorchEnabled);
                                    isTorchEnabled = false;
                                    count = 0;
                                }
                            } else {
                                count++;
                                if (count == 4) {
                                    mCameraManager.setTorchMode("0", isTorchEnabled);
                                    isTorchEnabled = true;
                                    count = 0;
                                }
                            }
                        }

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
