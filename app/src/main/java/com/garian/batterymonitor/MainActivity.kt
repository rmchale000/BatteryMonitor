package com.garian.batterymonitor

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Button
import android.support.design.widget.Snackbar
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import io.realm.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), DeleteDialogFragment.DeleteDialogListener {
    private val rvAdapter:RecyclerView.Adapter<*> = ReportListAdapter(arrayOf(ReportListData()))
    private val simpleAdapter:RecyclerView.Adapter<*> = SimpleListAdapter(arrayOf(""))
    private var reportBeingDeleted = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Realm.init(applicationContext)

        var reportRecyclerView: RecyclerView = findViewById(R.id.reportRecyclerView)
        reportRecyclerView.layoutManager = LinearLayoutManager(this)

        val realm: Realm = Realm.getDefaultInstance()
        //var reportNameList: Array<String>
        var reportList: Array<ReportListData> = arrayOf<ReportListData>()
        try{
            var reports: RealmResults<Report> = realm.where(Report::class.java).sort("startTime",Sort.DESCENDING).findAll()
            Log.e("onCreate","reports is "+reports.size)
            //reportNameList = Array(reports.size) {i -> SimpleDateFormat("MM/dd HH:mm").format(reports[i]!!.startTime)}
            /*reportList = Array(reports.size) {i -> Triple(SimpleDateFormat("MM/dd HH:mm").format(reports[i]!!.startTime),
                SimpleDateFormat("MM/dd HH:mm").format(reports[i]!!.reportData.sort("checkTime",Sort.DESCENDING).get(0)!!.checkTime),
                reports[i]!!.status
            )}*/
            var isRunningReport = false
            for(report in reports){
                var startTime = SimpleDateFormat("MM/dd HH:mm").format(report.startTime)
                var endTimeStr: String
                if(report.reportData.size>0){
                    val endTimeDate:Date = report.reportData.sort("checkTime",Sort.DESCENDING).get(0)!!.checkTime
                    val cal1:Calendar = Calendar.getInstance()
                    val cal2:Calendar = Calendar.getInstance()
                    cal1.setTime(report.startTime)
                    cal2.setTime(endTimeDate)
                    if(cal1.get(Calendar.DAY_OF_MONTH)==cal2.get(Calendar.DAY_OF_MONTH)){
                        endTimeStr = SimpleDateFormat("HH:mm").format(endTimeDate)
                    } else {
                        endTimeStr = SimpleDateFormat("MM/dd HH:mm").format(endTimeDate)
                    }
                } else{
                    endTimeStr = ""
                }
                var status = ""
                if(report.status.equals("Running")){
                    isRunningReport = true
                    status = "Running"
                    val startButton: Button = findViewById(R.id.startbutton)
                    val stopButton: Button = findViewById(R.id.stopbutton)
                    startButton.setVisibility(View.GONE)
                    stopButton.setVisibility(View.VISIBLE)
                } else{
                    status = ""
                }
                reportList+=ReportListData(report.reportName,startTime,endTimeStr,status)
            }
            if(!isRunningReport){
                val startButton: Button = findViewById(R.id.startbutton)
                val stopButton: Button = findViewById(R.id.stopbutton)
                startButton.setVisibility(View.VISIBLE)
                stopButton.setVisibility(View.GONE)
            }
        } finally{
            realm.close()
        }
        Log.e("onCreate","reportList is "+reportList.size)
        if(rvAdapter is ReportListAdapter) {
            rvAdapter.setNewReportList(reportList)
        }
        reportRecyclerView.setAdapter(rvAdapter)
    }

    public fun startReport(view: View?){
        //val reportName: String = Date().toString()
        val reportName: String = SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS").format(Date())
        val realm: Realm = Realm.getDefaultInstance()
        try{
            realm.beginTransaction()
            var report: Report = realm.createObject(Report::class.java)
            report.reportName = reportName
            report.status = "Running"
            report.startTime = Date()
            realm.commitTransaction()
        } finally{
            realm.close()
        }
        val monitorData = workDataOf("REPORT_NAME" to reportName)
        val monitorWorkRequest = PeriodicWorkRequestBuilder<MonitorWorker>(15, TimeUnit.MINUTES)
            .setInputData(monitorData)
            .addTag("batterycheck")
            .build()
        //WorkManager.getInstance().enqueue(monitorWorkRequest)
        WorkManager.getInstance().enqueueUniquePeriodicWork("batterycheck", ExistingPeriodicWorkPolicy.REPLACE, monitorWorkRequest)
        refreshAdapter()
    }

    public fun stopReport(view: View?){
        val realm: Realm = Realm.getDefaultInstance()
        try{
            var report: Report? = realm.where(Report::class.java).equalTo("status","Running").findFirst()
            if(report==null) return;
            realm.beginTransaction()
            report.status = "ended"
            realm.commitTransaction()

            /*var reportData: ReportData? = realm.where(ReportData::class.java).equalTo("reportName",report.reportName)
                .sort("checkTime", Sort.DESCENDING).findFirst()
            if(reportData!=null){
                realm.beginTransaction()
                report.endTime = reportData.checkTime
                realm.commitTransaction()
            }*/

        } finally {
            realm.close()
        }
        WorkManager.getInstance().cancelAllWorkByTag("batterycheck")
        refreshAdapter()
        //WorkManager.getInstance().cancelAllWork()
    }

    public fun viewReport(view: View?){
        val realm: Realm = Realm.getDefaultInstance()
        try {
            var report: RealmResults<Report> = realm.where(Report::class.java).findAll()
            Log.e("db report output", report.toString())
            var reportData: RealmResults<ReportData> = realm.where(ReportData::class.java).findAll()
            Log.e("db reportdata output",reportData.toString())
            var reportCount: Long? = realm.where(ReportData::class.java).count()
            Log.e("db output", "count is " + reportCount)
        } finally {
            realm.close()
        }

        val itemView = view!!.getParent()
        val reportName: String
        if(itemView is View) {
            reportName = itemView.getTag(R.id.tag_transition_group).toString()
        } else return
        val intent: Intent = Intent(this,GraphActivity::class.java)
        intent.putExtra(getResources().getString(R.string.REPORT_NAME),reportName)
        startActivity(intent)
    }

    public fun deleteReport(view: View?){
        //Log.e("deleteReport","Delete report called 1")
        val itemView = view!!.getParent()
        val reportName: String
        if(itemView is View) {
            reportName = itemView.getTag(R.id.tag_transition_group).toString()
        } else return
        reportBeingDeleted = reportName
        val fragment = DeleteDialogFragment()
        fragment.show(supportFragmentManager,"File Name")
        val bundl = Bundle()
        bundl.putString("interval",reportName.substring(0,5) + " " + reportName.substring(11,16))
        fragment.setArguments(bundl)
    }

    public fun clearReports(view: View?){
        /*val realm: Realm = Realm.getDefaultInstance()
        try{
            var reports: RealmResults<Report> = realm.where(Report::class.java).findAll()
            realm.beginTransaction()
            reports.deleteAllFromRealm()
            realm.commitTransaction()

            var reportsData: RealmResults<ReportData> = realm.where(ReportData::class.java).findAll()
            realm.beginTransaction()
            reportsData.deleteAllFromRealm()
            realm.commitTransaction()

        } finally {
            realm.close()
        }*/
    }

    private fun refreshAdapter(){
        val realm: Realm = Realm.getDefaultInstance()

        var reportList: Array<ReportListData> = arrayOf<ReportListData>()
        try{
            var reports: RealmResults<Report> = realm.where(Report::class.java).sort("startTime",Sort.DESCENDING).findAll()
            //reportNameList = Array(reports.size) {i -> SimpleDateFormat("MM/dd HH:mm").format(reports[i]!!.startTime)}
            /*reportList = Array(reports.size) {i -> Triple(SimpleDateFormat("MM/dd HH:mm").format(reports[i]!!.startTime),
                SimpleDateFormat("MM/dd HH:mm").format(reports[i]!!.reportData.sort("checkTime",Sort.DESCENDING).get(0)!!.checkTime),
                reports[i]!!.status
            )}*/
            var isRunningReport = false
            for(report in reports){
                var startTime = SimpleDateFormat("MM/dd HH:mm").format(report.startTime)
                var endTimeStr: String
                if(report.reportData.size>0){
                    val endTimeDate:Date = report.reportData.sort("checkTime",Sort.DESCENDING).get(0)!!.checkTime
                    val cal1:Calendar = Calendar.getInstance()
                    val cal2:Calendar = Calendar.getInstance()
                    cal1.setTime(report.startTime)
                    cal2.setTime(endTimeDate)
                    if(cal1.get(Calendar.DAY_OF_MONTH)==cal2.get(Calendar.DAY_OF_MONTH)){
                        endTimeStr = SimpleDateFormat("HH:mm").format(endTimeDate)
                    } else {
                        endTimeStr = SimpleDateFormat("MM/dd HH:mm").format(endTimeDate)
                    }
                } else{
                    endTimeStr = ""
                }
                var status = ""
                if(report.status.equals("Running")){
                    isRunningReport = true
                    status = "Running"
                    val startButton: Button = findViewById(R.id.startbutton)
                    val stopButton: Button = findViewById(R.id.stopbutton)
                    startButton.setVisibility(View.GONE)
                    stopButton.setVisibility(View.VISIBLE)
                } else{
                    status = ""
                }
                reportList+=ReportListData(report.reportName,startTime,endTimeStr,status)
            }
            if(!isRunningReport){
                val startButton: Button = findViewById(R.id.startbutton)
                val stopButton: Button = findViewById(R.id.stopbutton)
                startButton.setVisibility(View.VISIBLE)
                stopButton.setVisibility(View.GONE)
            }
        } finally{
            realm.close()
        }
        /*var reportNameList: Array<String>
        try{
            var reports: RealmResults<Report> = realm.where(Report::class.java).findAll()
            reportNameList = Array(reports.size) {i -> SimpleDateFormat("MM/dd HH:mm").format(reports[i]!!.startTime)}
        } finally{
            realm.close()
        }*/
        if(rvAdapter is ReportListAdapter) {
            rvAdapter.setNewReportList(reportList)
        }
        rvAdapter.notifyDataSetChanged()
    }

    override fun onDeletePositiveClick(dialogFragment: DialogFragment) {
        Log.e("deleteReport","Delete report called")
        dialogFragment.dismiss()
        val realm: Realm = Realm.getDefaultInstance()
        try{
            var reports: RealmResults<Report> = realm.where(Report::class.java).equalTo("reportName",reportBeingDeleted).findAll()
            realm.beginTransaction()
            for(report in reports){
                report.reportData.deleteAllFromRealm()
            }
            reports.deleteAllFromRealm()
            realm.commitTransaction()
        } finally{
            realm.close()
        }
        refreshAdapter()
        Snackbar.make(findViewById(R.id.reportRecyclerView), reportBeingDeleted + " was deleted", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDeleteNegativeClick(dialog: DialogFragment) {

    }
}

open class Report : RealmObject(){
    var reportName: String = ""
    var startTime: Date = Date()
    var endTime: Date? = null
    var status: String? = "not running"
    var reportData: RealmList<ReportData> = RealmList()

}

open class ReportData : RealmObject(){
    var reportName: String = ""
    var level: Float? = 0.toFloat()
        get() = field
        set(value){
            field = value
        }
    var checkTime: Date = Date()
        get() = field
        set(value) {
            field = value
        }

}
