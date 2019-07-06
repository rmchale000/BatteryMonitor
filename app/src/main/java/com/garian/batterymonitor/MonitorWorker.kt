package com.garian.batterymonitor

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import java.text.SimpleDateFormat
import java.util.Date

class MonitorWorker (appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams){

    init{
        Realm.init(appContext)
    }

    override fun doWork(): Result {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            applicationContext.registerReceiver(null, ifilter)
        }

        val batteryPct: Float? = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level / scale.toFloat()
        }
        val workTime = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Date())
        Log.e("dowork","battery is " + batteryPct + " checked at time " + workTime)

        val realm: Realm = Realm.getDefaultInstance()
        try {
            val report: Report? = realm.where(Report::class.java).equalTo("reportName",getInputData().getString("REPORT_NAME")).findFirst()
            if(report==null)return Result.failure()

            realm.beginTransaction()
            var reportData: ReportData = realm.createObject(ReportData::class.java)
            reportData.level = batteryPct
            reportData.checkTime = Date()
            report.reportData.add(reportData)
            realm.commitTransaction()

        } finally{
            realm.close()
        }
        return Result.success()
    }

    override fun onStopped(){

    }
}

