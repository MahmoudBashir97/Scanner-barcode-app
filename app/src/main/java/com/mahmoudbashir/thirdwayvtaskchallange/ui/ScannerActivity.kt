package com.mahmoudbashir.thirdwayvtaskchallange.ui

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.mahmoudbashir.thirdwayvtaskchallange.R
import com.mahmoudbashir.thirdwayvtaskchallange.application
import com.mahmoudbashir.thirdwayvtaskchallange.databinding.ActivityScannerBinding
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel
import com.mahmoudbashir.thirdwayvtaskchallange.notificationService.NotifcationServiceReceiver
import com.mahmoudbashir.thirdwayvtaskchallange.repository.ItemRepository
import com.mahmoudbashir.thirdwayvtaskchallange.room.ItemsDatabase
import com.mahmoudbashir.thirdwayvtaskchallange.viewModel.ItemViewModel
import com.mahmoudbashir.thirdwayvtaskchallange.viewModel.viewModelProviderFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.N)
class ScannerActivity : AppCompatActivity() {

    lateinit var scannerActBinding: ActivityScannerBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private val REQUEST_CAMERA_PERMISSION = 201

    //This class provides methods to play DTMF tones
    private var toneGen1: ToneGenerator? = null

    private var barcodeData: String? = null

    var selectedType:String?=null
    lateinit var itemExpireDateTime:String
    var itemExpiredTimeStamp:Long=0




    val viewModel: ItemViewModel by lazy {
        val repo  = ItemRepository(ItemsDatabase.invoke(applicationContext),
            application as application
        )
        ViewModelProviders.of(this,
            viewModelProviderFactory(application,repo)
        )[ItemViewModel::class.java]
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerActBinding = DataBindingUtil.setContentView(this,R.layout.activity_scanner)


        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        initialiseDetectorsAndSources()
        cancelDetectedCode()
        saveDetectedCode()
        spinnerListener()
        selectExpiredDate()

    }

    //TODO as We named its for selecting Expired date form DatePickerDialog
    private fun selectExpiredDate() {
        scannerActBinding.txtExpireDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val hour = c.get(Calendar.HOUR)
            val minute = c.get(Calendar.MINUTE)
            val second = c.get(Calendar.SECOND)


            val dpd = DatePickerDialog(this, { view, year, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                val selectedDateTime = "$dayOfMonth-${monthOfYear+1}-$year $hour:$minute:$second"

                val formatter = SimpleDateFormat("dd-m-yyyy hh:mm:ss")
                val cTime = formatter.format(Date())
                val ft = formatter.parse(cTime) as Date

                val itemExDate = formatter.parse(selectedDateTime) as Date

                scannerActBinding.txtExpireDate.text =  selectedDateTime.split(" ")[0]

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                itemExpireDateTime = itemExDate.toString()
                itemExpiredTimeStamp = itemExDate.time

                validateDateComparer(itemExDate)
                spinnerListenerExtensionDate(selectedDateTime)

            }, year, month, day)
            dpd.show()
        }
    }

    //TODO here we get the difference between two date to Number of days
    private fun validateDateComparer(itemExDate : Date)
    {
        val formatter = SimpleDateFormat("dd-m-yyyy hh:mm:ss")
        val cTime = formatter.format(Date())
        val ft = formatter.parse(cTime) as Date

        val diff: Long = itemExDate.time - ft.time

        val time = TimeUnit.DAYS
        val diffDays: Long = time.convert(diff, TimeUnit.MILLISECONDS)
        println("The difference in days is : $diffDays")

        scannerActBinding.isExtensionDate = diffDays>0

    }

    //TODO() Listen on spinner  of selected previous Expired Time
    private fun spinnerListenerExtensionDate(itemExDate: String){
        scannerActBinding.spinExtensionTime.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                when(position){
                    1 -> {
                        AddExtensionHours(itemExDate,6)
                    }
                    2->{
                        AddExtensionHours(itemExDate,12)
                    }
                    3->{
                        AddExtensionHours(itemExDate,18)
                    }
                    4->{
                        AddExtensionHours(itemExDate,24)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }
        }
    }

    //TODO() Add Number of hours to selected previous Expired Time
    private fun AddExtensionHours(itemExDate: String,hours:Long){
        val hours: Long = hours *60*60*1000

        val time = TimeUnit.HOURS
        val formatter = SimpleDateFormat("dd-m-yyyy hh:mm:ss")
        val cTime = formatter.format(Date())
        val ft = formatter.parse(cTime) as Date
        val dTime  = formatter.parse(itemExDate) as Date
        dTime.time = dTime.time + hours
        val calculated = dTime.time + hours

       itemExpireDateTime = "$calculated"
       itemExpiredTimeStamp = calculated

    }

    //TODO() Listen on spinner to get Item Types
    private fun spinnerListener() {
        val types = resources.getStringArray(R.array.item_types)
        scannerActBinding.spinItemTypes.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position >0){
                    selectedType = types[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }
        }
    }

    //TODO() saving detectedCode and inserting data to local room db
    private fun saveDetectedCode() {
        scannerActBinding.saveBtn.setOnClickListener {

            if (checkEditTextValidation()){
                val item = ItemModel(0,
                    scannerActBinding.edtBarCode.text.toString(),
                    scannerActBinding.edtItemName.text.toString(),
                    selectedType.toString(),
                    itemExpireDateTime,
                    itemExpiredTimeStamp
                )

                val check = viewModel.insertNewItem(item).isActive
                if (check){
                    showMessageToast("Added successfully!!")
                    getDataToStartService()
                    runBlocking {
                        delay(300)
                        onBackPressed()
                    }
                }else{
                    showMessageToast("some error occurred!")
                }
            }else{
                showMessageToast("please check your input fields")
            }
        }

    }

    private fun getDataToStartService() {
        viewModel.getAllStoredItems().observe(this,{ list ->
            if (list != null){
                startService()
            }
        })
    }

    //TODO go to start service for detecting Expired item and notify the user
    private fun startService(){
        val i = Intent(this, NotifcationServiceReceiver::class.java)


        i.putExtra("status", "start")


        val pendingIntent = PendingIntent.getBroadcast(this, 0, i, 0)


        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val onMinuteInMills = (1000 * 10).toLong()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MINUTE, 1)
        calendar[Calendar.SECOND] = 0
        val triggerAt = calendar.timeInMillis

        alarmManager.cancel(pendingIntent)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            onMinuteInMills,
            pendingIntent
        )

    }
    private fun showMessageToast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }

    //TODO here to cancel adding new Product and go back stack
    private fun cancelDetectedCode() {
        scannerActBinding.cancelBn.setOnClickListener {
            scannerActBinding.isLoading = false
            scannerActBinding.isScanned= false
            onBackPressed()
        }
    }


    private fun checkEditTextValidation():Boolean{
        return (scannerActBinding.edtItemName.text.toString().isNotEmpty()
                &&
                scannerActBinding.edtBarCode.text.toString().isNotEmpty()
                &&
                scannerActBinding.txtExpireDate.text.toString().isNotEmpty()
                && selectedType!!.isNotEmpty()
                ||
            (scannerActBinding.isExtensionDate && itemExpireDateTime.toString().isNotEmpty())
                )
    }


    private fun initialiseDetectorsAndSources(){
        scannerActBinding.isLoading = false
        scannerActBinding.isScanned= false

          barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()


        scannerActBinding.surfaceView.holder.addCallback(object: SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (ActivityCompat.checkSelfPermission(
                        this@ScannerActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    cameraSource.start(holder)
                } else {
                    ActivityCompat.requestPermissions(
                        this@ScannerActivity,
                        arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION
                    )
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
               //
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })



        //TODO("here to detect scanned code")
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode>{

            override fun release() {
                ///
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes: SparseArray<Barcode> = detections.detectedItems

                if (barcodes.size() != 0) {
                    scannerActBinding.isLoading = true
                    if (barcodes.valueAt(0).rawValue != null) {
                        barcodeData = barcodes.valueAt(0).rawValue.toString()
                        toneGen1?.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                        runBlocking {
                            delay(200)
                            scannerActBinding.isLoading =false
                            scannerActBinding.isScanned = true
                        }
                        val code = barcodeData.toString()
                        scannerActBinding.edtBarCode.setText(code)

                    } else {
                        Toast.makeText(
                            this@ScannerActivity,
                            "Use another thing!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })

    }
}