package androidpath.ll.lesleepbc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import butterknife.ButterKnife;

/**
 * Created by Le on 2015/9/16.
 */

public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().getName();
    private String filePath = Environment.getExternalStorageDirectory().getPath() + "/test.json";
    private LineChartFragment mCurrentFragment;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //init framework
        ButterKnife.bind(this);
        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SleepTrackingActivity.class);
                startActivity(intent);
            }
        });


        //chart
        mCurrentFragment = new LineChartFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mCurrentFragment)
                .commit();
    }


    private String readfile() {
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        try {
            FileReader fileReader =
                    new FileReader(filePath);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            // Always close files.
            bufferedReader.close();

        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            filePath + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }


    private void GsonTest01() {
        SleepData mSleepdata = new Gson().fromJson(readfile(), SleepData.class);
        System.out.println(mSleepdata.getSleepData());
    }


    public void flashData(View view) {
        mCurrentFragment.display();
    }

}
