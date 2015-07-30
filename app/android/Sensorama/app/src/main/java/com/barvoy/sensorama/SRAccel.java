package com.barvoy.sensorama;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SRAccel extends Activity implements SensorEventListener {
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    Activity activity;
    float []vals = { 0, 0, 0 };

    public SRAccel(Activity activity) {
        mSensorManager = (SensorManager)activity.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        vals[0] = event.values[0];
        vals[1] = event.values[1];
        vals[2] = event.values[2];
    }

    public void capture(Sensorama s)
    {
        System.out.printf("%f %f %f ", vals[0], vals[1], vals[2]);
    }
}