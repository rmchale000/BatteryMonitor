package com.garian.batterymonitor

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class SimpleListAdapter (val reportListIn: Array<String>): RecyclerView.Adapter<SimpleListAdapter.ReportListViewHolder>(){

    var reportList: Array<String> = arrayOf("Report 1","Report 2","report 3")
    var intervalList: Array<String> = arrayOf("")

    class ReportListViewHolder(val v: View): RecyclerView.ViewHolder(v) {
        var reportnameView: TextView = v.findViewById(R.id.starttimeLI)
        var endtimeView: TextView = v.findViewById(R.id.endtimeLI)
        var statusView: TextView = v.findViewById(R.id.statusLI)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportListViewHolder {
        val tv: View = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_list_item, parent, false)
        return ReportListViewHolder(tv)
    }

    override fun onBindViewHolder(holder: ReportListViewHolder, position: Int) {
        holder.reportnameView.setText(reportList[position])
    }

    override fun getItemCount(): Int {
        return reportList.size
    }
}