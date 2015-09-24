package androidpath.ll.lesleepbc;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import androidpath.ll.lesleepbc.Model.SleepData;
import androidpath.ll.lesleepbc.Model.SleepPoint;
import androidpath.ll.lesleepbc.Utils.MyOrientationDetector;
import androidpath.ll.lesleepbc.Utils.ShakeDetector;
import androidpath.ll.lesleepbc.Utils.SleepApplication;
import androidpath.ll.lesleepbc.Views.SleepTrackingActivity;

/**
 * Created by Le on 2015/9/16.
 */

public class AccelerometerDataService extends Service {
    private final String TAG = getClass().getName();
    private String filePath;



    //sensor
    private MyOrientationDetector mMyOrientationDetector;
    private SensorManager sensorManager;
    private Sensor lightSensor, accelerometer;
    private ShakeDetector mShakeDetector;


    private TimerTask updateTask;
    private Timer updateTimer;

    private int waitForSensorsToWarmUp = 0;
    private float DEFAULT_MIN_SENSITIVITY = 1.0F;
    private float alpha = 0.8f;
    private float maxNetForce = 0.0f;
    private float[] gravity = {0, 0, 0};
    private SleepData mSleepData;


    private final class UpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            final float x = System.currentTimeMillis();
            float y = java.lang.Math
                    .min(DEFAULT_MIN_SENSITIVITY, maxNetForce);
            final SleepPoint sleepPoint = new SleepPoint(x, y);
            mSleepData.add(sleepPoint);
            jsonProcess(mSleepData); //write data to json file
            maxNetForce = 0;
        }
    }




    private SensorEventListener mAccelerometerlistener = new SensorEventListener() {

        @Override
        public void onSensorChanged(final SensorEvent event) {

            mShakeDetector.detectShake(event);

            if (waitForSensorsToWarmUp < 5) {
                if (waitForSensorsToWarmUp == 4) {
                    waitForSensorsToWarmUp++;
                    try {
                        updateTask = new UpdateTimerTask();
                        updateTimer.scheduleAtFixedRate(updateTask,
                                10000, 10000); //delay 10sec, execute each 10sec

                    } catch (IllegalStateException ise) {
                        // user stopped monitoring really quickly after
                        // starting.
                        Log.d(TAG, "User stopped monitoring quickly after starting.");
                    }
                    gravity[0] = event.values[0];
                    gravity[1] = event.values[1];
                    gravity[2] = event.values[2];
                }
                waitForSensorsToWarmUp++;
                return;
            }
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            double curX = event.values[0] - gravity[0];
            double curY = event.values[1] - gravity[1];
            double curZ = event.values[2] - gravity[2];

            double mAccelCurrent = Math.sqrt(curX * curX + curY * curY + curZ * curZ);
            double absAccel = Math.abs(mAccelCurrent);
            maxNetForce = (float) (absAccel > maxNetForce ? absAccel : maxNetForce);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        updateTimer = new Timer();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSleepData = new SleepData();
        mShakeDetector = new ShakeDetector(getBaseContext());
        mMyOrientationDetector = new MyOrientationDetector(this);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("userInfo",
                Activity.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        filePath = SleepApplication.getFilePath(username);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);



        sensorManager.registerListener(mAccelerometerlistener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mMyOrientationDetector.enable();


        //push Notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name)).setContentText(getString(R.string.service_description))
                .setAutoCancel(false).setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));

        Intent resultIntent = new Intent(this, SleepTrackingActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
        mBuilder.setContentIntent(resultPendingIntent);
        startForeground(1337, mBuilder.build());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        updateTimer.cancel();
        if (sensorManager != null) {
            sensorManager.unregisterListener(mAccelerometerlistener);
            mMyOrientationDetector.disable();
        }
        super.onDestroy();

    }

    private void jsonProcess(SleepData sleepData) {
        Gson gson = new Gson();
        Log.d(TAG, "filePath:" + filePath);
        try {
            String json = gson.toJson(sleepData);
            FileWriter writer = new FileWriter(filePath);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
