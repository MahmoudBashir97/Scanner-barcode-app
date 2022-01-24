package com.mahmoudbashir.thirdwayvtaskchallange.notificationService

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mahmoudbashir.thirdwayvtaskchallange.R
import com.mahmoudbashir.thirdwayvtaskchallange.application
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel
import com.mahmoudbashir.thirdwayvtaskchallange.repository.ItemRepository
import com.mahmoudbashir.thirdwayvtaskchallange.room.ItemsDatabase
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@DelicateCoroutinesApi
class NotifcationServiceReceiver:BroadcastReceiver() {


    lateinit var repo:ItemRepository
    lateinit var notificationManagerCompat : NotificationManagerCompat

    @SuppressLint("CheckResult")
    override fun onReceive(context: Context?, intent: Intent?) {

        repo = ItemRepository(ItemsDatabase.invoke(context!!), application())


        notificationManagerCompat = NotificationManagerCompat.from(context!!)

        val status  = intent?.getStringExtra("status")
        if (status == "start") {
            repo.getAllStoredItems().subscribeOn(Schedulers.computation())
                .subscribe {
                    if (it.isNotEmpty()){
                        Log.d("serviceD ", "service started")
                        //doNotification(context,it[0],"")
                        check(context,it)
                    }
                }
        }

}




fun check(context: Context?, mlist:List<ItemModel>){

    val formatter = SimpleDateFormat("dd-m-yyyy hh:mm:ss")
    val cTime = formatter.format(Date())
    val ft = "${formatter.parse(cTime) as Date}"
    val cDate = formatter.parse(cTime) as Date
    val cuTimeStamp = cDate.time
    Log.d("ftDate: ", "${cDate.time}")

    for (item in mlist){
        val dateTimeStamp = item.itemExpiryDate
        if (dateTimeStamp.equals(cuTimeStamp)){
            doNotification(context!!,item,"")
            val updatedItem = ItemModel(item.id,item.itemCode,item.itemName,item.itemType,item.itemExpiryDate,item.itemExpiredTimeStamp,true)
            GlobalScope.launch {
                repo.updateItem(updatedItem)
            }
        }
    }
}


fun doNotification(context: Context?,item:ItemModel){
var imgId = R.drawable.food
when(item.itemType){
   "Food" -> {
       imgId = R.drawable.food
   }
   "Drink" ->{
       imgId = R.drawable.pepsi_png
   }
   "Chipse" ->{
       imgId = R.drawable.chipsy
   }
   "Electronic" ->{
       imgId = R.drawable.electronics
   }
   else -> {
       imgId = R.drawable.food
   }
}



val action = Intent(context,NotifcationServiceReceiver::class.java).apply {
   action = AlarmClock.ACTION_DISMISS_ALARM
   putExtra("stopAction","stop")
}

val pendingIntent: PendingIntent =
   PendingIntent.getBroadcast(context, 0, action, 0)

val builder = NotificationCompat.Builder(context!!, "notifyme")
   .setContentIntent(pendingIntent)
   .setContentTitle("title")
   .setContentText("subTitle")
   .setSmallIcon(imgId)
   .setPriority(NotificationCompat.PRIORITY_DEFAULT)
   .addAction(1,"Stop",pendingIntent)


notificationManagerCompat.notify(200, builder.build())
}


fun doNotification(context: Context?,item:ItemModel,n:String){

    var imgId = R.drawable.food
    when(item.itemType){
        "Food" -> {
            imgId = R.drawable.food
        }
        "Drink" ->{
            imgId = R.drawable.pepsi_png
        }
        "Chipse" ->{
            imgId = R.drawable.chipsy
        }
        "Electronic" ->{
            imgId = R.drawable.electronics
        }
        else -> {
            imgId = R.drawable.food
        }
    }


val action = Intent(context,NotifcationServiceReceiver::class.java).apply {
   action = AlarmClock.ACTION_DISMISS_ALARM
   putExtra("stopAction","stop")
}

val pendingIntent: PendingIntent =
   PendingIntent.getBroadcast(context, 0, action, 0)

val builder = NotificationCompat.Builder(context!!, "notifyme")
   .setContentIntent(pendingIntent)
    .setContentTitle("There is some items Expired ${item.itemName}")
    .setContentText("please check it in Expired Screen Items")
   .setSmallIcon(imgId)
   .setPriority(NotificationCompat.PRIORITY_HIGH)
   .addAction(1,"View",pendingIntent)


notificationManagerCompat.notify(200, builder.build())
}
}