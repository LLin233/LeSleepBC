package androidpath.ll.lesleepbc;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.util.Timer;
import java.util.TimerTask;

import androidpath.ll.lesleepbc.Events.SleepDataUpdateEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by Le on 2015/9/16.
 */
public class AccelerometerDataService extends Service {
    private final String TAG = getClass().getName();
    private String filePath = Environment.getExternalStorageDirectory().getPath() + "/test.json";
    private TimerTask updateTask;
    Timer updateTimer;

    int time = 14;

    private final class UpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            //final long currentTime = System.currentTimeMillis();
            //final float x = currentTime;
            final float x = time;
            time += 3;
            float y = java.lang.Math
                    .min(DEFAULT_MIN_SENSITIVITY, maxNetForce);
            Log.d(TAG, time + ":  " + y);

            final SleepPoint sleepPoint = new SleepPoint(x + "", y);
            EventBus.getDefault().post(new SleepDataUpdateEvent(sleepPoint));
//
            mSleepData.add(sleepPoint);
            jsonProcess(mSleepData);
            // append the two doubles in sleepPoint to file
            //TODO: send data to chart, stickyEvent
            maxNetForce = 0;
        }
    }


    public static final String SLEEP_DATA = "sleepData";

    private boolean alreadyDeletedResidualFile = false;
    // Object for intrinsic lock
    public static final Object DATA_LOCK = new Object();

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private int waitForSensorsToWarmUp = 0;
    public static float DEFAULT_MIN_SENSITIVITY = 1.0F;
    private final float alpha = 0.8f;
    private float maxNetForce = 0.0f;
    public static int MAX_POINTS_IN_A_GRAPH = 200;
    private float[] gravity = {0, 0, 0};
    private SleepData mSleepData;


    private SensorEventListener mAccelerometerlistener = new SensorEventListener() {
        private static final int BUFFER_SIZE = 50;
        private static final String CSV_SEPARATOR = ",";
        private String FILENAME = Environment.getExternalStorageDirectory().getPath() + "/data.csv";


        @Override
        public void onSensorChanged(final SensorEvent event) {
            if (waitForSensorsToWarmUp < 5) {
                if (waitForSensorsToWarmUp == 4) {
                    waitForSensorsToWarmUp++;
                    try {
                        updateTask = new UpdateTimerTask();
                        updateTimer.scheduleAtFixedRate(updateTask,
                                6000, 6000);

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


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(mAccelerometerlistener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

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
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                SleepData temp = mSleepData;
                jsonProcess(temp);
                return null;
            }
        };
        updateTimer.cancel();
        if (sensorManager != null) {
            sensorManager.unregisterListener(mAccelerometerlistener);
        }
        super.onDestroy();

    }


    private void jsonProcess(SleepData sleepData) {
        Gson gson = new Gson();
        System.out.println("filePath:" + filePath);//查看实际路径
        try {
            String json = gson.toJson(sleepData);
            FileWriter writer = new FileWriter(filePath);
            writer.write(json);
            writer.close();
            System.out.println("JSON写入完毕");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
