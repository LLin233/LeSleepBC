package androidpath.ll.lesleepbc.Utils;

import android.app.Application;
import android.os.Environment;

/**
 * Created by Le on 2015/9/23.
 */
public class SleepApplication extends Application{
    private static String prefixFilePath = Environment.getExternalStorageDirectory().getPath();

    public static String getFilePath(String username) {
        return prefixFilePath  + "/" + username +".json";
    }
}
