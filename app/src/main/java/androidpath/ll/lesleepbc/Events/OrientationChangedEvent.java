package androidpath.ll.lesleepbc.Events;

/**
 * Created by Le on 2015/9/17.
 */
public class OrientationChangedEvent {
    private int msg;

    public OrientationChangedEvent(int msg) {
        this.msg = msg;
    }

    public int getMessage() {
        return this.msg;
    }
}
