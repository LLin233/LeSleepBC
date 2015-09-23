package androidpath.ll.lesleepbc;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import androidpath.ll.lesleepbc.Events.SleepDataUpdateEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by Le on 2015/9/21.
 */
public class LineChartFragment extends Fragment {
    private final String TAG = getClass().getName();
    DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private String filePath = Environment.getExternalStorageDirectory().getPath() + "/test.json";

    private LineChart mLineChart;


    public LineChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(SleepDataUpdateEvent event) {
        //xValues.add(event.getPoint().getTimeStamp());
        // yValues.add(new Entry(event.getPoint().getMovementStatus(), xValues.size() - 1));
        Log.d(TAG, "got Event");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.content_main_chart, container, false);
        mLineChart = (LineChart) view.findViewById(R.id.chart);

        LineData mLineData = getLineData(36, 1);
        showChart(mLineChart, mLineData, Color.rgb(114, 188, 223));

        //TODO read data from sleepdata file, load them into chart
        return view;
    }


    //translating timestamp
    private String formatMinutes(float timemillis) {
        return formatter.format(timemillis);
    }

    private void showChart(LineChart lineChart, LineData lineData, int color) {
        lineChart.setDrawBorders(false);  //是否在折线图上添加边框
        // no description text
        lineChart.setDescription("");// 数据描述
        // 如果没有数据的时候，会显示这个，类似listview的emtpyview
        lineChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable / disable grid background
        lineChart.setDrawGridBackground(false); // 是否显示表格颜色
        lineChart.setGridBackgroundColor(Color.WHITE & 0x70FFFFFF); // 表格的的颜色，在这里是是给颜色设置一个透明度

        // enable touch gestures
        lineChart.setTouchEnabled(true); // 设置是否可以触摸

        // enable scaling and dragging
        lineChart.setDragEnabled(true);// 是否可以拖拽
        lineChart.setScaleEnabled(true);// 是否可以缩放

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(false);//

        lineChart.setBackgroundColor(color);// 设置背景

        // add data
        lineChart.setData(lineData); // 设置数据

        // get the legend (only possible after setting data)
        Legend mLegend = lineChart.getLegend(); // 设置比例图标示，就是那个一组y的value的

        // modify the legend ...
        // mLegend.setPosition(LegendPosition.LEFT_OF_CHART);
        mLegend.setForm(Legend.LegendForm.CIRCLE);// 样式
        mLegend.setTextColor(Color.WHITE);// 颜色
        lineChart.animateX(2500); // 立即执行的动画,x轴
    }

    /**
     * 生成一个数据
     *
     * @param count 表示图表中有多少个坐标点
     * @param range 用来生成range以内的随机数
     * @return
     */
    private LineData getLineData(int count, float range) {
        ArrayList<String> xValues = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            // x轴显示的数据，这里默认使用数字下标显示
            xValues.add("" + i);
        }

        // y轴的数据
        ArrayList<Entry> yValues = new ArrayList<Entry>();
        for (int i = 0; i < count; i++) {
            float value = (float) (Math.random() * range);
            yValues.add(new Entry(value, i));
        }

        // create a dataset and give it a type
        // y轴的数据集合
        LineDataSet lineDataSet = new LineDataSet(yValues, "测试折线图" /*显示在比例图上*/);
        // mLineDataSet.setFillAlpha(110);
        // mLineDataSet.setFillColor(Color.RED);

        //用y轴的集合来设置参数

        lineDataSet.setDrawCubic(true); //曲线平滑
        lineDataSet.setDrawFilled(true); //填充
        lineDataSet.setLineWidth(1.75f); // 线宽
        lineDataSet.setCircleSize(3f);// 显示的圆形大小
        lineDataSet.setColor(Color.WHITE);// 显示颜色
        lineDataSet.setCircleColor(Color.WHITE);// 圆形的颜色
        lineDataSet.setHighLightColor(Color.WHITE); // 高亮的线的颜色

        ArrayList<LineDataSet> lineDataSets = new ArrayList<LineDataSet>();
        lineDataSets.add(lineDataSet); // add the datasets

        // create a data object with the datasets
        LineData lineData = new LineData(xValues, lineDataSets);

        return lineData;
    }

    private void showLineDataFromFile() {

        new AsyncTask<Void, SleepPoint, String>() {
            @Override
            protected String doInBackground(Void... params) {
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

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                SleepData mSleepdata = new Gson().fromJson(s.toString(), SleepData.class);
                ArrayList<String> xValues = new ArrayList<String>();
                ArrayList<Entry> yValues = new ArrayList<Entry>();

                for (SleepPoint sp : mSleepdata.getSleepData()) {
                    xValues.add(sp.getTimeStamp());
                    yValues.add(new Entry(sp.getMovementStatus(), xValues.size() - 1));
                    LineDataSet lineDataSet = new LineDataSet(yValues, "Time" /*显示在比例图上*/);
                    lineDataSet.setDrawCircles(false);
                    lineDataSet.setDrawValues(false);
                    lineDataSet.setDrawCubic(true); //曲线平滑
                    lineDataSet.setDrawFilled(true); //填充
                    lineDataSet.setLineWidth(1.75f); // 线宽
                    lineDataSet.setColor(Color.WHITE);// 显示颜色
                    lineDataSet.setHighLightColor(Color.WHITE); // 高亮的线的颜色
                    ArrayList<LineDataSet> lineDataSets = new ArrayList<LineDataSet>();
                    lineDataSets.add(lineDataSet); // add the datasets
                    // create a data object with the datasets
                    LineData lineData = new LineData(xValues, lineDataSets);
                    showChart(mLineChart, lineData, Color.rgb(114, 188, 223));
                }

            }
        }.execute();
    }


    public void display() {
        showLineDataFromFile();
    }
}



