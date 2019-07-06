package com.garian.batterymonitor

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ReportListAdapter (val reportListIn: Array<ReportListData>): RecyclerView.Adapter<ReportListAdapter.ReportListViewHolder>(){

    var reportList: Array<ReportListData> = reportListIn
    var intervalList: Array<String> = arrayOf("")

    class ReportListViewHolder(val v: View): RecyclerView.ViewHolder(v) {
        var reportnameView: TextView = v.findViewById(R.id.starttimeLI)
        var endtimeView: TextView = v.findViewById(R.id.endtimeLI)
        var statusView: TextView = v.findViewById(R.id.statusLI)

    }

    fun setNewReportList(reportListIn: Array<ReportListData>){
        reportList = reportListIn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportListViewHolder {
        val tv: View = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_list_item, parent, false)
        return ReportListViewHolder(tv)
    }

    override fun onBindViewHolder(holder: ReportListViewHolder, position: Int) {
        val reportListData: ReportListData = reportList[position]
        holder.reportnameView.setText(reportListData.startTime)
        if(reportListData.endTime!=null)
            holder.endtimeView.setText(reportListData.endTime)
        if(reportListData.status.equals("Running")) {
            holder.statusView.setText(reportListData.status)
            holder.statusView.setVisibility(View.VISIBLE)
        } else{
            holder.statusView.setVisibility(View.GONE)
        }
        val itemView = holder.statusView.getParent()
        if(itemView is View) itemView.setTag(R.id.tag_transition_group,reportListData.reportName)
    }

    override fun getItemCount(): Int {
        return reportList.size
    }
}

open class ReportListData(){

    var reportName: String? = null
    var startTime: String? = null
    var endTime: String? = null
    var status: String? = null

    constructor(reportNameIn: String?, startTimeIn: String?, endTimeIn: String?, statusIn: String?): this(){
        reportName = reportNameIn
        startTime = startTimeIn
        endTime = endTimeIn
        status = statusIn
    }


}