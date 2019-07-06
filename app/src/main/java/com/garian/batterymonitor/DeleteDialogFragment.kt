package com.garian.batterymonitor

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import java.lang.ClassCastException

class DeleteDialogFragment: DialogFragment() {

    internal lateinit var listener: DeleteDialogListener

    interface DeleteDialogListener{
        fun onDeletePositiveClick(dialog: DialogFragment)
        fun onDeleteNegativeClick(dialog: DialogFragment)
    }

    override public fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            builder.setMessage("Are you sure you want to delete the report starting at "+getArguments()!!.get("interval")+"?")
                .setPositiveButton(R.string.delete, DialogInterface.OnClickListener(){ dialog, id ->
                    listener.onDeletePositiveClick(this)
                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener(){ dialog, id ->

                })
            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")

    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        try{
            listener = context as DeleteDialogListener
        } catch(e: ClassCastException){
            throw ClassCastException(context.toString() + " must implement DeleteDialogListner")
        }
    }
}