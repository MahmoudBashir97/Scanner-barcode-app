package com.mahmoudbashir.thirdwayvtaskchallange.fragments

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.util.SparseArray
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.mahmoudbashir.thirdwayvtaskchallange.ui.MainActivity
import com.mahmoudbashir.thirdwayvtaskchallange.R
import com.mahmoudbashir.thirdwayvtaskchallange.databinding.FragmentScannerBinding
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel
import com.mahmoudbashir.thirdwayvtaskchallange.notificationService.NotifcationServiceReceiver
import com.mahmoudbashir.thirdwayvtaskchallange.viewModel.ItemViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class Scanner_Fragment : Fragment() {

    lateinit var scannerBinding: FragmentScannerBinding

    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private val REQUEST_CAMERA_PERMISSION = 201

    //This class provides methods to play DTMF tones
    private var toneGen1: ToneGenerator? = null

    private var barcodeData: String? = null



    var selectedType:String?=null
    lateinit var itemExpireDateTime:String
    lateinit var viewModel:ItemViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        scannerBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_scanner, container, false)
        viewModel = (activity as MainActivity).viewModel

        return scannerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        initialiseDetectorsAndSources()
        cancelDetectedCode()
        saveDetectedCode()
        spinnerListener()

        scannerBinding.txtExpireDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val hour = c.get(Calendar.HOUR)
            val minute = c.get(Calendar.MINUTE)
            val second = c.get(Calendar.SECOND)


            val dpd = DatePickerDialog(requireContext(), { view, year, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                val selectedDateTime = "$dayOfMonth-${monthOfYear+1}-$year $hour:$minute:$second"

                val formatter = SimpleDateFormat("dd-m-yyyy hh:mm:ss")
                val cTime = formatter.format(Date())
                val ft = formatter.parse(cTime) as Date

                val itemExDate = formatter.parse(selectedDateTime) as Date

                scannerBinding.txtExpireDate.text =  selectedDateTime.split(" ")[0]

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                itemExpireDateTime = itemExDate.toString()

                validateDateComparer(itemExDate)
                spinnerListenerExtensionDate(selectedDateTime)

            }, year, month, day)
            dpd.show()

        }
    }


    private fun validateDateComparer(itemExDate : Date)
    {

        val formatter = SimpleDateFormat("dd-m-yyyy hh:mm:ss")
        val cTime = formatter.format(Date())
        val ft = formatter.parse(cTime) as Date

        val diff: Long = itemExDate.time - ft.time

        val time = TimeUnit.DAYS
        val diffDays: Long = time.convert(diff, TimeUnit.MILLISECONDS)
        println("The difference in days is : $diffDays")

        scannerBinding.isExtensionDate = diffDays>0
    }
    private fun spinnerListenerExtensionDate(itemExDate: String){
        scannerBinding.spinExtensionTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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

    private fun AddExtensionHours(itemExDate: String,hours:Long){
        val hours: Long = hours *60*60*1000

        val time = TimeUnit.HOURS
        // var ExDateConvertedTime: Long = time.convert(itemExDate.time, TimeUnit.MILLISECONDS) + hours
        val formatter = SimpleDateFormat("dd-m-yyyy hh:mm:ss")
        val cTime = formatter.format(Date())
        val ft = formatter.parse(cTime) as Date
        val dTime  = formatter.parse(itemExDate) as Date
        dTime.time = dTime.time + hours

        //showMessageToast("time : $dTime")

        itemExpireDateTime = "$dTime"

    }

    private fun spinnerListener() {
        val types = resources.getStringArray(R.array.item_types)
        scannerBinding.spinItemTypes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position >0){
                    selectedType = types[position]
                    // Toast.makeText(this@ScannerActivity,"type: ${types[position]} ",Toast.LENGTH_LONG).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }
        }
    }

    private fun saveDetectedCode() {
        scannerBinding.saveBtn.setOnClickListener {

            if (checkEditTextValidation()){
                val item = ItemModel(0,
                    scannerBinding.edtBarCode.text.toString(),
                    scannerBinding.edtItemName.text.toString(),
                    selectedType.toString(),
                    itemExpireDateTime,
                    0
                )

                val check = viewModel.insertNewItem(item).isActive
                if (check){
                    showMessageToast("Added successfully!!")
                    getDataToStartService()
                    runBlocking {
                        delay(300)
                        findNavController().navigateUp()
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
        viewModel.getAllStoredItems().observe(viewLifecycleOwner,{ list ->
            if (list != null){
                showMessageToast("zzzzzzzzzzzzzzzz")
                startServic(list as ArrayList<ItemModel>)
            }
        })
    }

    private fun startServic(item:ArrayList<ItemModel>){
        val i = Intent((activity as MainActivity), NotifcationServiceReceiver::class.java)

        i.putExtra("status", "start")
       // i.putExtra("mlist",item)

        val pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0)


        val alarmManager = (activity as MainActivity).getSystemService(Context.ALARM_SERVICE) as AlarmManager

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
        Toast.makeText(context,message,Toast.LENGTH_LONG).show()
    }

    private fun cancelDetectedCode() {
        scannerBinding.cancelBn.setOnClickListener {
            scannerBinding.isLoading = false
            scannerBinding.isScanned= false
            findNavController().navigateUp()
        }
    }


    private fun checkEditTextValidation():Boolean{
        return (scannerBinding.edtItemName.text.toString().isNotEmpty()
                &&
                scannerBinding.edtBarCode.text.toString().isNotEmpty()
                &&
                scannerBinding.txtExpireDate.text.toString().isNotEmpty()
                && selectedType!!.isNotEmpty()
                ||
                (scannerBinding.isExtensionDate && itemExpireDateTime.toString().isNotEmpty())
                )
    }


    private fun initialiseDetectorsAndSources(){
        scannerBinding.isLoading = false
        scannerBinding.isScanned= false

        barcodeDetector = BarcodeDetector.Builder(context)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(context, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()


        scannerBinding.surfaceView.holder.addCallback(object: SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (ActivityCompat.checkSelfPermission(
                        (activity as MainActivity),
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    cameraSource.start(holder)
                } else {
                    ActivityCompat.requestPermissions(
                        (activity as MainActivity),
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
                    scannerBinding.isLoading = true
                    if (barcodes.valueAt(0).rawValue != null) {
                        barcodeData = barcodes.valueAt(0).rawValue.toString()
                        toneGen1?.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                        runBlocking {
                            delay(200)
                            scannerBinding.isLoading =false
                            scannerBinding.isScanned = true
                        }
                        val code = barcodeData.toString()
                        scannerBinding.edtBarCode.setText(code)

                    } else {
                        showMessageToast("Use another thing!!")

                    }
                }
            }
        })

    }


    override fun onPause() {
        super.onPause()
        cameraSource.release()
    }

    override fun onResume() {
        super.onResume()
        initialiseDetectorsAndSources()
    }

}