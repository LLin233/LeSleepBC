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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import androidpath.ll.lesleepbc.Events.DataEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by Le on 2015/9/16.
 */
public class AccelerometerDataService extends Service {
    private final String TAG = getClass().getName();
    public static final String SLEEP_DATA = "sleepData";

    private boolean alreadyDeletedResidualFile = false;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private int waitForSensorsToWarmUp = 0;
    public static float DEFAULT_MIN_SENSITIVITY = 0.0F;
    private final float alpha = 0.8f;
    private final float[] gravity = {0, 0, 0};

    private SensorEventListener mAccelerometerlistener = new SensorEventListener() {
        private static final int BUFFER_SIZE = 50;
        private static final String CSV_SEPARATOR = ",";
        private String FILENAME = Environment.getExternalStorageDirectory().getPath() + "/data.csv";
        private Number[][] buf = new Number[BUFFER_SIZE][3];
        private int pos = 0;

        @Override
        public void onSensorChanged(final SensorEvent event) {
//            buf[pos][0] = System.currentTimeMillis(); //time
//            buf[pos][1] = event.values[0]; // x
//            buf[pos][2] = event.values[1]; // y
//            buf[pos][3] = event.values[2]; // z
//            pos++;
//
//            if (pos >= BUFFER_SIZE) {
//                try {
//                    File file = new File(FILENAME);
//                    file.createNewFile();
//                    BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
//
//                    for (int i = 0; i < pos; i++) {
//                        StringBuilder line = new StringBuilder();
//
//                        line.append(buf[i][0]);
//                        line.append(CSV_SEPARATOR);
//                        line.append(buf[i][1]);
//                        line.append(CSV_SEPARATOR);
//                        line.append(buf[i][2]);
//                        line.append(CSV_SEPARATOR);
//                        line.append(buf[i][3]);
//                        line.append("\n");
//                        EventBus.getDefault().postSticky(new DataEvent(line.toString()));
//                        Log.d(TAG, line.toString());
//                        writer.append(line.toString());
//                    }
//
//                    writer.flush();
//                    writer.close();
//                } catch (Exception e) {
//                    Log.e(TAG, "Error appending data to file!", e);
//                } finally {
//                    pos = 0;
//                }
//            }

            if (waitForSensorsToWarmUp < 5) {
                if (waitForSensorsToWarmUp == 4) {
                    waitForSensorsToWarmUp++;
                    try {

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

            final double curX = event.values[0] - gravity[0];
            final double curY = event.values[1] - gravity[1];
            final double curZ = event.values[2] - gravity[2];

            final double mAccelCurrent = Math.sqrt(curX * curX + curY * curY + curZ * curZ);
            final double absAccel = Math.abs(mAccelCurrent);

            buf[pos][0] = System.currentTimeMillis(); //time
            buf[pos][1] = mAccelCurrent;
            buf[pos][2] = absAccel;
            pos++;

            if (pos >= BUFFER_SIZE) {
                try {
                    File file = new File(FILENAME);
                    file.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

                    for (int i = 0; i < pos; i++) {
                        StringBuilder line = new StringBuilder();

                        line.append(buf[i][0]);
                        line.append(CSV_SEPARATOR);
                        line.append(buf[i][1]);
                        line.append(CSV_SEPARATOR);
                        line.append(buf[i][2]);
                        line.append("\n");
                        EventBus.getDefault().post(new DataEvent(line.toString()));
                        Log.d(TAG, line.toString());
                        writer.append(line.toString());
                    }
                    writer.flush();
                    writer.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error appending data to file!", e);
                } finally {
                    pos = 0;
                }
            }
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (!alreadyDeletedResidualFile) {
                    deleteFile(SLEEP_DATA);
                    alreadyDeletedResidualFile = true;
                }
//                final SharedPreferences.Editor ed = getSharedPreferences(SERVICE_IS_RUNNING,
//                        Context.MODE_PRIVATE).edit();
//                ed.putBoolean(SERVICE_IS_RUNNING, true);
//                ed.commit();
                return null;
            }
        }.execute();


        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(mAccelerometerlistener);
        }

    }

}
