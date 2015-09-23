package androidpath.ll.lesleepbc.Events;

import androidpath.ll.lesleepbc.SleepPoint;

/**
 * Created by Le on 2015/9/21.
 */
public class SleepDataUpdateEvent {
    private SleepPoint sleepPoint;

    public SleepDataUpdateEvent(SleepPoint sleepPoint) {
        this.sleepPoint = sleepPoint;
    }

    public SleepPoint getPoint() {
        return this.sleepPoint;
    }
}
