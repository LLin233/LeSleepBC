package androidpath.ll.lesleepbc.Events;

/**
 * Created by Le on 2015/9/19.
 */
public class LightChangedEvent {
    private float msg;

    public LightChangedEvent(float msg) {
        this.msg = msg;
    }

    public float getMessage() {
        return this.msg;
    }
}
