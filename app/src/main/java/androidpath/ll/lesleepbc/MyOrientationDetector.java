package androidpath.ll.lesleepbc;

/**
 * Created by Le on 2015/9/16.
 */

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;

import androidpath.ll.lesleepbc.Events.OrientationChangedEvent;
import de.greenrobot.event.EventBus;

public class MyOrientationDetector extends OrientationEventListener {
    public MyOrientationDetector(Context context) {
        super(context, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onOrientationChanged(int orientation) {
/***
 *    A result of ORIENTATION_UNKNOWN (-1) means the device is flat (perhaps on a table) and the orientation is unknown.
 A result of 0 means the device is in its “normal” orientation, with the top of the device facing in the up direction. (“Normal” is defined by the device manufacturer. You need to test on each device to find out for sure what “normal” means.)
 A result of 90 means the device is tilted 90 degrees, with the left side of the device facing in the up direction.
 A result of 180 means the device is tilted 180 degrees, with the bottom side of the device facing in the up direction (upside down).
 A result of 270 means the device is tilted 270 degrees, with the right side of the device facing in the up direction.
 */
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }
//        //只检测是否有四个角度的改变
//        if (orientation > 350 || orientation < 10) {
//            orientation = 0;
//        } else if (orientation > 80 && orientation < 100) {
//            orientation = 90;
//        } else if (orientation > 170 && orientation < 190) {
//            orientation = 180;
//        } else if (orientation > 260 && orientation < 280) {
//            orientation = 270;
//        } else {
//            return;
//        }
        EventBus.getDefault().post(new OrientationChangedEvent(orientation));
        Log.i("MyOrientationDetector ", "onOrientationChanged:" + orientation);
    }
}