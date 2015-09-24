package androidpath.ll.lesleepbc.Views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidpath.ll.lesleepbc.AccelerometerDataService;
import androidpath.ll.lesleepbc.Events.LightChangedEvent;
import androidpath.ll.lesleepbc.Events.OrientationChangedEvent;
import androidpath.ll.lesleepbc.Events.ShakeEvent;
import androidpath.ll.lesleepbc.Events.StopServiceEvent;
import androidpath.ll.lesleepbc.R;
import androidpath.ll.lesleepbc.Utils.ServiceTools;
import androidpath.ll.lesleepbc.Utils.ShakeDetector;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by Le on 2015/9/19.
 */

public class SleepTrackingActivity extends AppCompatActivity {

    private final String TAG = getClass().getName();
    private boolean SWITCH_ON = false;
    boolean isLightOn = false;
    private SensorManager sensorManager;
    private Sensor accelerometer, lightSensor;
    private ShakeDetector mShakeDetector;
    private AlertDialog mAlertDialogServiceOn, mAlertDialogServiceOff, mAlertDialogHoldingPhone, mAlertDialogLightOn;
    private android.os.Handler mHandler;

    @Bind(R.id.status_recording)
    TextView mStatusRecording;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;

    private SensorEventListener mAccelerometerlistener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            mShakeDetector.detectShake(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_tracking);

        //init framework
        ButterKnife.bind(this);
        mShakeDetector = new ShakeDetector(this);
        mHandler = new android.os.Handler();
        setUpSensor();
        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ServiceTools.isServiceRunning("androidpath.ll.lesleepbc.AccelerometerDataService", getBaseContext())) {
                    SWITCH_ON = true;
                }

                if (!SWITCH_ON) {
                    SWITCH_ON = true;
                    Toast.makeText(getApplicationContext(), "Starting accelerometer recording", Toast.LENGTH_SHORT).show();
                    startServiceClick(view);
                    mHandler.postDelayed(delayLightSensor, 5000);
                } else {
                    SWITCH_ON = false;
                    Toast.makeText(getApplicationContext(), "Stopping accelerometer recording", Toast.LENGTH_SHORT).show();
                    stopServiceClick(view);
                }
            }
        });
    }

    private void setUpSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(mAccelerometerlistener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onEvent(StopServiceEvent event) {
        stopRecordingService();
        if (mAlertDialogServiceOn == null) {
            mAlertDialogServiceOn = build(this.getResources().getString(R.string.title_dialog_shake_monitor_on),
                    this.getResources().getString(R.string.msg_dialog_shake_monitor_on));
            mAlertDialogServiceOn.show();
        } else {
            mAlertDialogServiceOn.show();
        }
    }

    public void onEvent(ShakeEvent event) {
        if (mAlertDialogServiceOff == null) {
            mAlertDialogServiceOff = build(this.getResources().getString(R.string.title_dialog_shake_monitor_off),
                    this.getResources().getString(R.string.msg_dialog_shake_monitor_off));
            mAlertDialogServiceOff.show();
        } else {
            mAlertDialogServiceOff.show();
        }

    }

    public void onEvent(OrientationChangedEvent event) {
        stopRecordingService();
        if (mAlertDialogHoldingPhone == null) {
            mAlertDialogHoldingPhone = build("Awake", "You are holding your phone");
            mAlertDialogHoldingPhone.show();
        } else {
            mAlertDialogHoldingPhone.show();
        }
    }

    public void onEvent(LightChangedEvent event) {
        stopRecordingService();
        if (mAlertDialogLightOn == null) {
            mAlertDialogLightOn = build("The light is on", "please turn off your light and place your device");
            mAlertDialogLightOn.show();
        } else {
            mAlertDialogLightOn.show();
        }
        isLightOn = false;
    }


    public void startServiceClick(View view) {
        Log.i(TAG, "Starting accelerometer recording");
        startService(new Intent(this, AccelerometerDataService.class));
        mStatusRecording.setText("Recording : ON");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void stopServiceClick(View view) {
        Log.i(TAG, "Stopping accelerometer recording");
        stopRecordingService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        sensorManager.registerListener(mAccelerometerlistener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onDestroy() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(mAccelerometerlistener);
        }
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }


    public AlertDialog build(String title, String msg) {
        AlertDialog.Builder mAlertDialogBuilder = null;
        AlertDialog mAlertDialog = null;

        mAlertDialogBuilder = new AlertDialog.Builder(
                this);
        // set title
        mAlertDialogBuilder.setTitle(title);
        // set dialog message
        mAlertDialogBuilder
                .setMessage(msg)
                .setCancelable(true)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        mAlertDialog = mAlertDialogBuilder.create();

        return mAlertDialog;
    }

    private void stopRecordingService() {
        SWITCH_ON = false;
        mHandler.removeCallbacks(delayLightSensor);
        sensorManager.unregisterListener(mLightListener);
        stopService(new Intent(this, AccelerometerDataService.class));
        mStatusRecording.setText("Recording : OFF");
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private final Runnable delayLightSensor = new Runnable() {
        @Override
        public void run() {
            sensorManager.registerListener(mLightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    };


    private SensorEventListener mLightListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            if (x > 20) {
                isLightOn = true;
            }
            if (isLightOn) {
                EventBus.getDefault().post(new LightChangedEvent());
            }
        }
    };

}
