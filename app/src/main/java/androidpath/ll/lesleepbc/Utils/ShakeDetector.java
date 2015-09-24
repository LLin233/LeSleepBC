package androidpath.ll.lesleepbc.Utils;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

import androidpath.ll.lesleepbc.Events.ShakeEvent;
import androidpath.ll.lesleepbc.Events.StopServiceEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by Le on 2015/9/23.
 */
public class ShakeDetector {

    private Context mContext;
    private final int SHAKE_THRESHOLD = 800;
    private long lastUpdate;
    private float[] lastAccelerometerData = {0, 0, 0};

    public ShakeDetector(Context context) {
        mContext = context;
    }


    public void detectShake(SensorEvent event) {
        long curTime = System.currentTimeMillis();
        // only allow one update every 100ms.
        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;
            float speed = Math.abs(event.values[0] + event.values[1] + event.values[2] - lastAccelerometerData[0] - lastAccelerometerData[1] - lastAccelerometerData[2]) / diffTime * 10000;

            if (speed > SHAKE_THRESHOLD) {
                Log.d("sensor", "shake detected w/ speed: " + speed);
                if (ServiceTools.isServiceRunning("androidpath.ll.lesleepbc.AccelerometerDataService", mContext)) {
                    EventBus.getDefault().post(new StopServiceEvent());
                } else {
                    EventBus.getDefault().post(new ShakeEvent());
                }
            }
            lastAccelerometerData[0] = event.values[0];
            lastAccelerometerData[1] = event.values[1];
            lastAccelerometerData[2] = event.values[2];
        }
    }

}
