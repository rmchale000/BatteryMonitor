package com.garian.batterymonitor

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import kotlin.random.Random

class UsageStatsActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usage_stats_table)
        val reportTable: TableLayout = findViewById(R.id.reporttable)
        val names = arrayOf("Notepad","Pulsar","Calculator","Photos","Drive","WhatsApp","Slack","Maps","Chrome","Phone")
        for(name in names){
            val tableRow: TableRow = TableRow(getApplicationContext())
            val appName: TextView = TextView(getApplicationContext())
            appName.setText(name)
            appName.setPadding(8,8,8,8)
            val appCpu: TextView = TextView(getApplicationContext())
            appCpu.setText(String.format("%.2f", Random.nextDouble(0.0,1.0))+"%")
            appCpu.setPadding(8,8,8,8)
            val appMem: TextView = TextView(getApplicationContext())
            appMem.setText(Random.nextInt(0,1000).toString()+"k")
            appMem.setPadding(8,8,8,8)

            tableRow.addView(appName)
            tableRow.addView(appCpu)
            tableRow.addView(appMem)

            reportTable.addView(tableRow)
        }
    }
}