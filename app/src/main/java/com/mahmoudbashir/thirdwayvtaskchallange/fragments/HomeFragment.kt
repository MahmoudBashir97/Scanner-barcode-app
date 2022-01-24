package com.mahmoudbashir.thirdwayvtaskchallange.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.mahmoudbashir.thirdwayvtaskchallange.ui.MainActivity
import com.mahmoudbashir.thirdwayvtaskchallange.R
import com.mahmoudbashir.thirdwayvtaskchallange.adapters.ItemAdapter
import com.mahmoudbashir.thirdwayvtaskchallange.databinding.FragmentHomeBinding
import com.mahmoudbashir.thirdwayvtaskchallange.ui.ScannerActivity
import com.mahmoudbashir.thirdwayvtaskchallange.viewModel.ItemViewModel


class HomeFragment : Fragment() {

    lateinit var homeBinding:FragmentHomeBinding
    lateinit var viewModel: ItemViewModel
    lateinit var itemAdpt : ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeBinding =  DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        viewModel = (activity as MainActivity).viewModel // initializing ViewModel


        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeBinding.relBtnToScan.setOnClickListener { navigateToScannerActivity() }

        setUpRecyclerView()
        getItemsFromLocal()
    }

    private fun navigateToScannerActivity() {
        val intent = Intent((activity as MainActivity).applicationContext,ScannerActivity::class.java)
        startActivity(intent)
    }

    //TODO Get ItemFrom Local db and Display it inside recyclerview
    private fun getItemsFromLocal() {
        homeBinding.isItemAdded = false
        viewModel.getAllStoredItems().observe(viewLifecycleOwner,{
            if (it != null && it.isNotEmpty()){
                homeBinding.isItemAdded = true
                Log.d("itemsReceived : ","name: ${it[0].itemName}")
                itemAdpt.differ.submitList(it)
            }
        })
    }

    private fun setUpRecyclerView() {
        itemAdpt = ItemAdapter()
        homeBinding.recItems.apply {
            setHasFixedSize(true)
            adapter = itemAdpt
        }
    }

    override fun onResume() {
        super.onResume()
        itemAdpt.updateList()
    }

}