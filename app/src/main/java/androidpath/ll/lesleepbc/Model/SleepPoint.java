package androidpath.ll.lesleepbc.Model;

/**
 * Created by Le on 2015/9/22.
 */
public class SleepPoint {

    private float timeStamp;
    private float movementStatus;

    public SleepPoint(float timeStamp, float movementStatus) {
        this.movementStatus = movementStatus;
        this.timeStamp = timeStamp;
    }

    public float getTimeStamp() {
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
