package androidpath.ll.lesleepbc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Le on 2015/9/22.
 */
public class SleepData {
    private List<SleepPoint> sleepData;

    public SleepData() {
        sleepData = new ArrayList<SleepPoint>();
    }

    public List<SleepPoint> getSleepData() {
        return sleepData;
    }

    public void add(SleepPoint sp) {
        if (sleepData.size() >= 300) {
            sleepData.remove(0);
        }
        sleepData.add(sp);
    }
}
