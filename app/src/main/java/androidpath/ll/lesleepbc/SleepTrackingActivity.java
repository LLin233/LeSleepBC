package androidpath.ll.lesleepbc;

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

import androidpath.ll.lesleepbc.Events.DataEvent;
import androidpath.ll.lesleepbc.Events.LightChangedEvent;
import androidpath.ll.lesleepbc.Events.OrientationChangedEvent;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by Le on 2015/9/19.
 */

public class SleepTrackingActivity extends AppCompatActivity {

    private final String TAG = getClass().getName();
    private boolean SWITCH_ON = false;
    private MyOrientationDetector mMyOrientationDetector;
    private SensorManager sensorManager;
    private Sensor lightSensor;

    @Bind(R.id.data)
    TextView myData;
    @Bind(R.id.status_orientation)
    TextView mStatusOrientation;
    @Bind(R.id.status_light)
    TextView mStatusLight;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;


    private SensorEventListener mLightListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            //EventBus.getDefault().post(new LightChangedEvent(x));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_tracking);
        //init framework
        ButterKnife.bind(this);
        setUpSensor();
        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ServiceTools.isServiceRunning("androidpath.ll.lesleepbc.AccelerometerDataService", getApplicationContext())) {
                    SWITCH_ON = true;
                }

                if (!SWITCH_ON) {
                    SWITCH_ON = true;
                    Toast.makeText(getApplicationContext(), "Starting accelerometer recording", Toast.LENGTH_SHORT).show();
                    startServiceClick(view);
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
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(mLightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mMyOrientationDetector = new MyOrientationDetector(this);
    }

    public void onEvent(DataEvent event) {
        myData.setText(event.getData());
    }

    public void onEvent(OrientationChangedEvent event) {
        mStatusOrientation.setText(event.getMessage() + "");
    }

    public void onEvent(LightChangedEvent event) {
        mStatusLight.setText(event.getMessage() + "lux");
    }


    public void startServiceClick(View view) {
        Log.i(TAG, "Starting accelerometer recording");
        startService(new Intent(this, AccelerometerDataService.class));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void stopServiceClick(View view) {
        Log.i(TAG, "Stopping accelerometer recording");
        stopService(new Intent(this, AccelerometerDataService.class));
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMyOrientationDetector.enable();
        EventBus.getDefault().registerSticky(this);
        sensorManager.registerListener(mLightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMyOrientationDetector.disable();

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(mLightListener);
        }
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }
}
