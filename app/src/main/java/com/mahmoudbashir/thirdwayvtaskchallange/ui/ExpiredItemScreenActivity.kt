package com.mahmoudbashir.thirdwayvtaskchallange.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.mahmoudbashir.thirdwayvtaskchallange.R
import com.mahmoudbashir.thirdwayvtaskchallange.adapters.ItemAdapter
import com.mahmoudbashir.thirdwayvtaskchallange.application
import com.mahmoudbashir.thirdwayvtaskchallange.databinding.ActivityExpiredItemScreenBinding
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel
import com.mahmoudbashir.thirdwayvtaskchallange.repository.ItemRepository
import com.mahmoudbashir.thirdwayvtaskchallange.room.ItemsDatabase
import com.mahmoudbashir.thirdwayvtaskchallange.viewModel.ItemViewModel
import com.mahmoudbashir.thirdwayvtaskchallange.viewModel.viewModelProviderFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ExpiredItemScreenActivity : AppCompatActivity() {
    lateinit var expiredBinding:ActivityExpiredItemScreenBinding

    //initializing viewModel
    val viewModel: ItemViewModel by lazy {
        val repo  = ItemRepository(ItemsDatabase.invoke(applicationContext),
            application as application
        )
        ViewModelProviders.of(this,
            viewModelProviderFactory(application,repo)
        )[ItemViewModel::class.java]
    }
    lateinit var mlist:ArrayList<ItemModel>
    lateinit var itemsAdpt:ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        expiredBinding = DataBindingUtil.setContentView(this,R.layout.activity_expired_item_screen)

        setUpRecyclerView()
        getExpiredData()

    }

    // get Expired data and display it on view with recycler view
    private fun getExpiredData() {
        expiredBinding.isItemAdded = false
        viewModel.getAllStoredItems().observe(this,{
            if (it != null){
                checkExpiredItems(it)
            }
        })
    }

    private fun showMessageToast(message:String){
        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
        Log.d("messageDeb : ",message)
    }

    private fun checkExpiredItems(it:List<ItemModel>){
        val formatter = SimpleDateFormat("dd-m-yyyy hh:mm:ss")
        val cTime = formatter.format(Date())
        val ft = "${formatter.parse(cTime) as Date}"

        Log.d("ftDate: ", ft)
        val cDate = formatter.parse(cTime) as Date
        val cuTimeStamp = cDate.time
        Log.d("ftDate: ", "${cDate.time}")


        for (item in it){
            val dateTimeStamp = item.itemExpiryDate
            if (dateTimeStamp.equals(cuTimeStamp) || item.isExpired){
                mlist.add(item)
            }
        }

        itemsAdpt.differ.submitList(mlist)
    }

    // do initialize recyclerView
    private fun setUpRecyclerView() {
        mlist = ArrayList()
        itemsAdpt = ItemAdapter()
        expiredBinding.recItems.apply {
            setHasFixedSize(true)
            adapter = itemsAdpt
        }
    }
}