package androidpath.ll.lesleepbc;

/**
 * Created by Le on 2015/9/22.
 */
public class SleepPoint {

    private String timeStamp;
    private float movementStatus;

    public SleepPoint(String timeStamp, float movementStatus) {
        this.movementStatus = movementStatus;
        this.timeStamp = timeStamp;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public float getMovementStatus() {
        return movementStatus;
    }

    @Override
    public String toString() {
        return timeStamp + " : " + movementStatus;
    }
}
