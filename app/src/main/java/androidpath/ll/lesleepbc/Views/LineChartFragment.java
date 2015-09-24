package androidpath.ll.lesleepbc.Views;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import androidpath.ll.lesleepbc.Model.SleepData;
import androidpath.ll.lesleepbc.Model.SleepPoint;
import androidpath.ll.lesleepbc.R;
import androidpath.ll.lesleepbc.Utils.SleepApplication;

/**
 * Created by Le on 2015/9/21.
 */
public class LineChartFragment extends Fragment {
    private final String TAG = getClass().getName();
    DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private String filePath;
    private LineChart mLineChart;


    public LineChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.content_main_chart, container, false);
        mLineChart = (LineChart) view.findViewById(R.id.chart);
        SharedPreferences preferences = getActivity().getSharedPreferences("userInfo",
                Activity.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        filePath = SleepApplication.getFilePath(username);

        LineData mLineData = new LineData();
        showChart(mLineChart, mLineData, Color.rgb(114, 188, 223));
        return view;
    }


    //translating timestamp
    private String formatTimestamp(float timemillis) {
        return formatter.format(timemillis);
    }

    private void showChart(LineChart lineChart, LineData lineData, int color) {
        lineChart.setDrawBorders(false);
        lineChart.setDescription(""); // no description text
        lineChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable / disable grid background
        lineChart.setDrawGridBackground(false);
        lineChart.setGridBackgroundColor(Color.WHITE & 0x70FFFFFF);

        // enable touch gestures
        lineChart.setTouchEnabled(true);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(false);//

        lineChart.setBackgroundColor(color);

        // add data
        lineChart.setData(lineData);

        // get the legend (only possible after setting data)
        Legend mLegend = lineChart.getLegend();

        // modify the legend ...
        // mLegend.setPosition(LegendPosition.LEFT_OF_CHART);
        mLegend.setForm(Legend.LegendForm.CIRCLE);
        mLegend.setTextColor(Color.WHITE);
        lineChart.animateX(2500);
    }

    void showLineDataFromFile() {

        new AsyncTask<Void, SleepPoint, String>() {
            @Override
            protected String doInBackground(Void... params) {

                StringBuilder stringBuilder = new StringBuilder();
                String line = null;

                //User verification
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

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                SleepData mSleepdata = new Gson().fromJson(s.toString(), SleepData.class);
                ArrayList<String> xValues = new ArrayList<String>();
                ArrayList<Entry> yValues = new ArrayList<Entry>();
                if (mSleepdata != null) {
                    for (SleepPoint sp : mSleepdata.getSleepData()) {
                        xValues.add(formatTimestamp(sp.getTimeStamp()));
                        yValues.add(new Entry(sp.getMovementStatus(), xValues.size() - 1));
                        LineDataSet lineDataSet = new LineDataSet(yValues, "Time");
                        lineDataSet.setDrawCircles(false);
                        lineDataSet.setDrawValues(false);
                        lineDataSet.setDrawCubic(true);
                        lineDataSet.setDrawFilled(true);
                        lineDataSet.setLineWidth(1.75f);
                        lineDataSet.setColor(Color.WHITE);
                        lineDataSet.setHighLightColor(Color.WHITE);
                        ArrayList<LineDataSet> lineDataSets = new ArrayList<LineDataSet>();
                        lineDataSets.add(lineDataSet); // add the datasets
                        // create a data object with the datasets
                        LineData lineData = new LineData(xValues, lineDataSets);
                        showChart(mLineChart, lineData, Color.rgb(114, 188, 223));
                    }
                }

            }
        }.execute();
    }
}



