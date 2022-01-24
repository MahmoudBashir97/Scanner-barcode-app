package com.mahmoudbashir.thirdwayvtaskchallange.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.mahmoudbashir.thirdwayvtaskchallange.R
import com.mahmoudbashir.thirdwayvtaskchallange.application
import com.mahmoudbashir.thirdwayvtaskchallange.databinding.ActivityMainBinding
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel
import com.mahmoudbashir.thirdwayvtaskchallange.repository.ItemRepository
import com.mahmoudbashir.thirdwayvtaskchallange.room.ItemsDatabase
import com.mahmoudbashir.thirdwayvtaskchallange.viewModel.ItemViewModel
import com.mahmoudbashir.thirdwayvtaskchallange.viewModel.viewModelProviderFactory
import java.util.*

class MainActivity : AppCompatActivity() {

    public interface InterfacetoStartService{
        public fun onClick(list:ArrayList<ItemModel>)
    }
    lateinit var mainBinding:ActivityMainBinding

    private val REQUEST_CAMERA_PERMISSION = 201
    private val PERMISSION_REQUEST_CODE = 200

    //TODO() initializing viewModel
    val viewModel: ItemViewModel by lazy {
        val repo  = ItemRepository(ItemsDatabase.invoke(applicationContext),
            application as application
        )
        ViewModelProviders.of(this,
        viewModelProviderFactory(application,repo))[ItemViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(mainBinding.toolbar)
        mainBinding.toolbar.title = "My App"

        checkPermission()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "MahmoudChannel"
            val description = "Channel for Mahmoud Reminder"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("notifyme", name, importance)
            channel.description = description
            val notificationManager: NotificationManager? = getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when(id){
            R.id.expiredScreen ->{  // TODO() here to go to ExpiredScreen
                navigateToExpiredScreen()
            }
        }
        return true
    }

    private fun navigateToExpiredScreen() {
        Intent(this,ExpiredItemScreenActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()

                // main logic
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        showMessageOKCancel(
                            "You need to allow access permissions"
                        ) { dialog, which ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermission()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}