package com.mahmoudbashir.thirdwayvtaskchallange.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.mahmoudbashir.thirdwayvtaskchallange.R
import com.mahmoudbashir.thirdwayvtaskchallange.databinding.CustomDialogFragmentLayoutBinding

class CustomDialogFragment(val interfaceOnClick : IonClickButtons): DialogFragment(){

    lateinit var dialogBinding:CustomDialogFragmentLayoutBinding
    private  val TAG = "CustomDialogFragment"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialogBinding = DataBindingUtil.inflate(inflater,R.layout.custom_dialog_fragment_layout,container,false)


        dialogBinding.cancelBtn.setOnClickListener {
            interfaceOnClick.Cancel()
        }

        dialogBinding.saveBtn.setOnClickListener {
            interfaceOnClick.Save()
        }


        return dialogBinding.root
    }

    interface IonClickButtons{
        public fun Save()
        public fun Cancel()
    }
}