package com.garian.batterymonitor

import android.app.PendingIntent.getActivity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.*
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import java.text.SimpleDateFormat
import java.util.*

class GraphActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        val realm: Realm = Realm.getDefaultInstance()

        var graph: GraphView = findViewById(R.id.graph)
        //graph.viewport.setScalable(true)
        //graph.viewport.setScrollable(true)
        graph.gridLabelRenderer.verticalAxisTitle = resources.getString(R.string.vertical_axis)
        graph.gridLabelRenderer.horizontalAxisTitle = resources.getString(R.string.horizontal_axis)

        var intent = getIntent()
        Log.e("GraphActivity","report name is "+intent.getStringExtra(getResources().getString(R.string.REPORT_NAME)))
        var reports: RealmResults<Report> = realm.where(Report::class.java).equalTo("reportName",intent.getStringExtra(getResources().getString(R.string.REPORT_NAME))).findAll()
        var series: LineGraphSeries<DataPoint> = LineGraphSeries<DataPoint>()
        for (report in reports){
            //val checkTime: Date = if(report.checkTime!=null) report.checkTime else continue
            var reportData: RealmList<ReportData> = report.reportData
            for(reportDatum in reportData) {
                if (reportDatum.checkTime == null || reportDatum.level == null) continue
                series.appendData(DataPoint(reportDatum.checkTime, reportDatum.level!!.toDouble()), false, 41)
            }
            if(reportData.size>2){
                graph.getViewport().setMinX(reportData[0]!!.checkTime.getTime().toDouble())
                graph.getViewport().setMaxX(reportData[reportData.lastIndex]!!.checkTime.getTime().toDouble())
                graph.getViewport().setXAxisBoundsManual(true)
            }
            if(reportData.size<10){
                graph.viewport.setScalable(false)
            } else{
                graph.viewport.setScalable(true)
            }
        }

        /*var series: LineGraphSeries<DataPoint> = LineGraphSeries<DataPoint>(arrayOf(
            DataPoint(0.toDouble(),1.toDouble()),
            DataPoint(1.toDouble(),5.toDouble()),
            DataPoint(2.toDouble(),3.toDouble()),
            DataPoint(3.toDouble(),2.toDouble()),
            DataPoint(4.toDouble(),6.toDouble())
        ))*/
        series.setDrawDataPoints(true)
        series.setOnDataPointTapListener(ReportTapListener())
        graph.addSeries(series)
        graph.getGridLabelRenderer().setLabelFormatter(DateAsXAxisLabelFormatter(this, SimpleDateFormat("MM/dd HH:mm")))
        graph.getGridLabelRenderer().setNumHorizontalLabels(3)
        /*if(reports.size>2) {
            graph.getViewport().setMinX(reports[0]!!.checkTime.getTime().toDouble())
            graph.getViewport().setMaxX(reports[reports.lastIndex]!!.checkTime.getTime().toDouble())
        }*/
        graph.getGridLabelRenderer().setHumanRounding(false)
    }

    inner class ReportTapListener: OnDataPointTapListener{
        override fun onTap(series: Series<*>?, dataPoint: DataPointInterface?) {
            val intent: Intent = Intent(this@GraphActivity,UsageStatsActivity::class.java)
            startActivity(intent)
        }
    }
}
