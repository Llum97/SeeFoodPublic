package com.example.seefood

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.gson.Gson


class DashboardActivity : AppCompatActivity() {

    private val PERMISSION_CODE = 1000;
    private val IMAGE_CAPTURE_CODE = 1001
    var image_uri: Uri? = null
    private var mAuth: FirebaseAuth? = null
    private var nameTV: TextView? = null
    private var progressBar: ProgressBar? = null
    private var settingsButton: Button? = null
    private var btn_camera: Button? = null
    val db = FirebaseFirestore.getInstance()


    //pieCharts
    private var totalFat: PieChart? = null
    private var saturatedFat: PieChart? = null
    private var cholesterol: PieChart? = null
    private var sodium: PieChart? = null
    private var totalCarbohydrate: PieChart? = null
    private var dietaryFiber: PieChart? = null

    //boolean values that will tell if the user has surpassed the maximum recommended intake
    var totalFatMaxCap = false
    var saturatedfatMaxCap = false
    var cholesterolMaxCap = false
    var sodiumMaxCap = false
    var carbMaxCap = false
    var dietMaxCap = false


    //boolean values that will tell if the values are 0 or not
    var fat0 = false
    var satFat0 = false
    var cholesterol0 = false
    var sodium0 = false
    var carb0 = false
    var diet0 = false

    val value = arrayListOf<Float>()
    val name = ArrayList<String>()//Creating an empty arraylist


    //arraylists for labels
    var totalfatArr: ArrayList<PieEntry> = ArrayList()
    var saturatedfatArr: ArrayList<PieEntry> = ArrayList()
    var cholesterolArr: ArrayList<PieEntry> = ArrayList()
    var sodiumArr: ArrayList<PieEntry> = ArrayList()
    var carbArr: ArrayList<PieEntry> = ArrayList()
    var dietArr: ArrayList<PieEntry> = ArrayList()

    //the colors
    //get the colors from colors.xml resources
    var lightBlue = 0
    var lightGreen = 0
    var lightOrange = 0
    var lightPink = 0
    var pink = 0
    var purple = 0
    var lightPurple = 0
    var green = 0
    var darkblue = 0
    var orange = 0
    var lightYellow = 0
    var yellow = 0
    var errorRed = 0
    var textColor = 0
    var grey =0


    var hashMap: HashMap<String, Float> = HashMap<String, Float>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)


        mAuth = FirebaseAuth.getInstance()

        initializeUI()
        initializeButton()
        displayInformation()

    }


    private fun initializeButton() {
        settingsButton!!.setOnClickListener {
            val intent = Intent(this@DashboardActivity, SettingsActivity::class.java)
            startActivity(intent)
        }

        btn_camera!!.setOnClickListener {

            //if system os is Marshmallow or Above, we need to request runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    //permission was not enabled
                    val permission = arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    //show popup to request permission
                    requestPermissions(permission, PERMISSION_CODE)
                } else {
                    //permission already granted
                    // openCamera()
                    clearCharts()
                    pickImageFromGallery()

                }
            } else {
                //system os is < marshmallow
                //openCamera()
                clearCharts()
                pickImageFromGallery()
            }

        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }



    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //called when image was captured from camera intent
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            val imageUri = data?.data
            // image_view.setImageURI(data?.data)
            analyzeImage(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))
        }

    }

    private fun analyzeImage(bitmap: Bitmap?) {
        if (bitmap == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

// imageView.setImageBitmap(null)
// textRecognitionModels.clear()
// bottomSheetRecyclerView.adapter?.notifyDataSetChanged()
// bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
// showProgress()

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {
                val mutableImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)

                recognizeText(it, mutableImage)
                Log.e("asdasdas", mutableImage.toString())

// imageView.setImageBitmap(mutableImage)
// hideProgress()
// bottomSheetRecyclerView.adapter?.notifyDataSetChanged()
// bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            .addOnFailureListener {
                Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
// hideProgress()
            }
    }

    private fun recognizeText(result: FirebaseVisionText?, image: Bitmap?) {
        if (result == null || image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        val canvas = Canvas(image)
        val rectPaint = Paint()
        rectPaint.color = Color.RED
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 4F
        val textPaint = Paint()
        textPaint.color = Color.RED
        textPaint.textSize = 40F
        var index = 0

        //these variables store values to be put in the hashmap
        var strVal = ""
        var labelNum = 0f


        result.textBlocks.forEach { block ->
            block.lines.forEach { line ->

                Log.e("text",Gson().toJson(line.text))
                if (line.text.contains("Total Fat") ||
                    line.text.contains("Saturated Fat") ||
                    line.text.contains("Cholesterol") ||
                    line.text.contains("Sodium") ||
                    line.text.contains("Total Carbohydrate") ||
                    line.text.contains("Dietary Fiber")){

                    if (line.text.matches(".*\\d.*".toRegex())) {
                        val splited = line.text.split(" ").toTypedArray()
                        val num = splited[splited.size-1]

                        if (splited[0] == "Sodium" || splited[0] == "Cholesterol") {
                            strVal= splited[0]
                            name.add(splited[0])
                        } else {
                            strVal = splited[0] + " " + splited[1]
                            name.add(splited[0] + " " + splited[1])
                        }

                        labelNum = num.replace("[^0-9.]".toRegex(),"").toFloat()
                        value.add(num.replace("[^0-9.]".toRegex(),"").toFloat())
                        Log.e("asddad", labelNum.toString())
                        Log.e("asddad", strVal)

                        hashMap.put(strVal,labelNum)
                    }
                }

            }


        }

        storeData(name,value)


    }

    private fun storeData(names:ArrayList<String>, pieValues: ArrayList<Float>) {

        val docRef = db.collection("users").document(mAuth!!.currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val data: HashMap<String,Float> = hashMapOf()

                    val min = minOf(names.size, pieValues.size)
                    for (i in 0 until min) {
                        data.put(names[i], pieValues[i] + document.getDouble(names[i])!!.toFloat())
                    }

                    db.collection("users").document((mAuth!!.currentUser!!.uid))
                        .set(data, SetOptions.merge())
                    displayInformation()
                }

            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "Information failed to load!", Toast.LENGTH_LONG)
                    .show()
                progressBar!!.visibility = View.GONE
            }


    }

    private fun displayInformation() {

        val docRef = db.collection("users").document(mAuth!!.currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val doc = document.data
                    val namePrint = document.getString("name")
                    nameTV!!.text = "Welcome " + namePrint


                    //get colors to be used in textColor and the piechart
                    getColors()

                    //sets the piechart textcolor
                    piechartTextColorAndRadiusDefinition(textColor)

                    //for individual piecharts
                    /*
                    if hashmap contains one of the nutrition labels, it gets it's number from the hashmap.
                    if that number is greater than maximum recommended value(MRV), it will return a red pie chart
                    if theat number is equal to 0 (sometimes the nutrition label have 0 values), it will display
                    the chart with the MRV. else it will display the piechart that'll have value from the label and MRV
                     */

                    val numFatFromLabel = document.getDouble("Total Fat")!!.toFloat()
                    val defaultDietNum = defaultDiet.get("Total Fat")

                    //if fat number from database is greater than the recommended value it sets totalFatMaxCap to
                    //true. This boolean value will help setting color of pie chart to be red
                    //else if the fat number from database is not equal to 0, then it adds value to the
                    //piechart arraylist else
                    //sets fat0 value to be true. this value will help set the color of piechart to be grey
                    if(numFatFromLabel!!.compareTo(defaultDietNum!!) > 0){
                        totalFatMaxCap = true
                        totalfatArr.add(PieEntry(numFatFromLabel!!,"Exceeded max total fat intake"))
                    } else if (numFatFromLabel!!.compareTo(defaultDietNum!!) <= 0){
                        if(numFatFromLabel!!.compareTo(0) != 0){
                            fat0=false
                            totalfatArr.add(PieEntry(numFatFromLabel, "TotalFatFromInput"))
                            totalfatArr.add(PieEntry(defaultDietNum!!.minus(numFatFromLabel), "Remaining Fat intake"))
                        } else if(numFatFromLabel!!.compareTo(0.0) == 0){
                            fat0=true
                            totalfatArr.add(PieEntry(.00001f, "Fat value: 0"))
                        }

                    }


                    val numSatFromLabel = document.getDouble("Saturated Fat")!!.toFloat()
                    val defaultDietSatNum = defaultDiet.get("Sat Fat")

                    if(numSatFromLabel!!.compareTo(defaultDietSatNum!!) > 0){
                        saturatedfatMaxCap = true
                        saturatedfatArr.add(PieEntry(numSatFromLabel!!,"Exceeded max saturated fat intake"))
                    } else if(numSatFromLabel!!.compareTo(defaultDietSatNum!!) <= 0){
                        if(numSatFromLabel!!.compareTo(0) != 0) {
                            satFat0 = false
                            saturatedfatArr.add(PieEntry(numSatFromLabel, "TotalSaturatedFatFromInput"))
                            saturatedfatArr.add(PieEntry(defaultDietSatNum!!.minus(numSatFromLabel), "Remaining Saturated Fat"))
                        } else if(numSatFromLabel!!.compareTo(0) == 0){
                            satFat0 = true
                            saturatedfatArr.add(PieEntry(.00001f, "SaturatedFat value: 0"))
                        }
                    }

                    val numColFromLabel = document.getDouble("Cholesterol")!!.toFloat()
                    val defaultDietColNum = defaultDiet.get("Cholesterol")

                    if(numColFromLabel!!.compareTo(defaultDietColNum!!) > 0){
                        cholesterolMaxCap = true
                        cholesterolArr.add(PieEntry(numColFromLabel!!,"Exceeded max total fat intake"))
                    } else if(numColFromLabel!!.compareTo(defaultDietColNum!!) <= 0){
                        if(numColFromLabel!!.compareTo(0) != 0) {
                            cholesterol0 = false
                            cholesterolArr.add(PieEntry(numColFromLabel, "CholesterolFromInput"))
                            cholesterolArr.add(PieEntry(defaultDietColNum!!.minus(numColFromLabel), "Remaining Cholesterol"))
                        } else if(numColFromLabel!!.compareTo(0) == 0){
                            cholesterol0 = true
                            cholesterolArr.add(PieEntry(.00001f, "Cholesterol value: 0"))
                        }
                    }

                    val numSodFromLabel = document.getDouble("Sodium")!!.toFloat()
                    val defaultDietSodNum = defaultDiet.get("Sodium")

                    if(numSodFromLabel!!.compareTo(defaultDietSodNum!!) > 0){
                        sodiumMaxCap = true
                        sodiumArr.add(PieEntry(numSodFromLabel!!,"Exceeded max total fat intake"))
                    } else if(numSodFromLabel!!.compareTo(defaultDietSodNum!!) <= 0){
                        if(numSodFromLabel!!.compareTo(0) != 0) {
                            sodium0 = false
                            sodiumArr.add(PieEntry(numSodFromLabel, "SodiumFromInput"))
                            sodiumArr.add(PieEntry(defaultDietSodNum!!.minus(numSodFromLabel), "Remaining Sodium"))
                        } else if(numSodFromLabel!!.compareTo(0) == 0){
                            sodium0 = true
                            sodiumArr.add(PieEntry(.00001f, "Sodium value:0"))
                        }
                    }


                    val numCarbFromLabel = document.getDouble("Total Carbohydrate")!!.toFloat()
                    val defaultDietCarbNum = defaultDiet.get("Total Carbohydrate")

                    if(numCarbFromLabel!!.compareTo(defaultDietCarbNum!!) > 0){
                        carbMaxCap = true
                        carbArr.add(PieEntry(numCarbFromLabel!!,"Exceeded max total fat intake"))
                    } else if(numCarbFromLabel!!.compareTo(defaultDietCarbNum!!) <= 0){
                        if(numCarbFromLabel!!.compareTo(0) != 0) {
                            carb0=false
                            carbArr.add(PieEntry(numCarbFromLabel, "TotalCarbohydrateFromInput"))
                            carbArr.add(PieEntry(defaultDietCarbNum!!.minus(numCarbFromLabel), "Remaining Carbohydrate"))

                        }else if(numCarbFromLabel!!.compareTo(0) == 0){
                            carb0 = true
                            carbArr.add(PieEntry(.00001f, "Carbohydrate value:0"))
                        }
                    }

                    val numFibFromLabel = document.getDouble("Dietary Fiber")!!.toFloat()
                    val defaultDietFibNum = defaultDiet.get("Dietary Fiber")
                    if(numFibFromLabel!!.compareTo(defaultDietFibNum!!) > 0){
                        dietMaxCap= true
                        dietArr.add(PieEntry(numFibFromLabel!!,"Exceeded max total fat intake"))
                    } else if(numFibFromLabel!!.compareTo(defaultDietFibNum!!) <= 0){
                        if(numFibFromLabel!!.compareTo(0) != 0) {
                            diet0 = false
                            dietArr.add(PieEntry(numFibFromLabel, "Dietary FiberFromInput"))
                            dietArr.add(PieEntry(defaultDietFibNum!!.minus(numFibFromLabel), "Remaining Dietary Fiber"))
                        }else if(numFibFromLabel!!.compareTo(0) == 0){
                            diet0 = true
                            dietArr.add(PieEntry(.00001f, "DietaryFiber value:0"))
                        }
                    }



                    //piedata set for different labels

                    val fatPiedataset: PieDataSet = PieDataSet(totalfatArr, " Total Fat Values")
                    fatPiedataset.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    fatPiedataset.valueLinePart1Length = 0.8f
                    fatPiedataset.valueLinePart2Length = 0.9f
                    fatPiedataset.valueLinePart1OffsetPercentage = 60.0f
                    val fatPieData: PieData = PieData(fatPiedataset)
                    fatPieData.notifyDataChanged()
                    totalFat!!.data = fatPieData
                    totalFat!!.setUsePercentValues(true)
                    totalFat!!.description.isEnabled = false
                    totalFat!!.legend.isEnabled = false




                    //for saturated fat
                    val saturatedPiedataset: PieDataSet = PieDataSet(saturatedfatArr, "Saturated Fat Values")
                    saturatedPiedataset.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    saturatedPiedataset.valueLinePart1Length = 0.8f
                    saturatedPiedataset.valueLinePart2Length = 0.9f
                    saturatedPiedataset.valueLinePart1OffsetPercentage = 60.0f
                    val saturatedPieData: PieData = PieData(saturatedPiedataset)
                    saturatedPieData.notifyDataChanged()
                    saturatedFat!!.data = saturatedPieData
                    saturatedFat!!.setUsePercentValues(true)
                    saturatedFat!!.description.isEnabled = false
                    saturatedFat!!.legend.isEnabled = false


                    //for cholesterol
                    val cholesterolPiedataset: PieDataSet = PieDataSet(cholesterolArr, "cholesterol Values")
                    cholesterolPiedataset.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    cholesterolPiedataset.valueLinePart1Length = 0.8f
                    cholesterolPiedataset.valueLinePart2Length = 0.9f
                    cholesterolPiedataset.valueLinePart1OffsetPercentage = 60.0f
                    val cholesterolPieData: PieData = PieData(cholesterolPiedataset)
                    cholesterolPieData.notifyDataChanged()
                    cholesterol!!.data = cholesterolPieData
                    cholesterol!!.setUsePercentValues(true)
                    cholesterol!!.description.isEnabled = false
                    cholesterol!!.legend.isEnabled = false

                    //for sodium
                    val sodiumPiedataset: PieDataSet = PieDataSet(sodiumArr, "Sodium Values")
                    sodiumPiedataset.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    sodiumPiedataset.valueLinePart1Length = 0.8f
                    sodiumPiedataset.valueLinePart2Length = 0.9f
                    sodiumPiedataset.valueLinePart1OffsetPercentage = 60.0f
                    val sodiumPieData: PieData = PieData(sodiumPiedataset)
                    sodiumPieData.notifyDataChanged()
                    sodium!!.data = sodiumPieData
                    sodium!!.setUsePercentValues(true)
                    sodium!!.description.isEnabled = false
                    sodium!!.legend.isEnabled = false



                    //for total carbohydrate
                    val carbPiedataset: PieDataSet = PieDataSet(carbArr, " Carbohydrate Values")
                    carbPiedataset.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    carbPiedataset.valueLinePart1Length = 0.8f
                    carbPiedataset.valueLinePart2Length = 0.9f
                    carbPiedataset.valueLinePart1OffsetPercentage = 60.0f
                    val carbPieData: PieData = PieData(carbPiedataset)
                    carbPieData.notifyDataChanged()
                    totalCarbohydrate!!.data = carbPieData
                    totalCarbohydrate!!.setUsePercentValues(true)
                    totalCarbohydrate!!.description.isEnabled = false
                    totalCarbohydrate!!.legend.isEnabled = false


                    //for dietary fibers
                    val dietFiberPiedataset: PieDataSet = PieDataSet(dietArr, "Dietary fibers Values")
                    dietFiberPiedataset.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    dietFiberPiedataset.valueLinePart1Length = 0.8f
                    dietFiberPiedataset.valueLinePart2Length = 0.9f
                    dietFiberPiedataset.valueLinePart1OffsetPercentage = 60.0f
                    val dietPieData: PieData = PieData(dietFiberPiedataset)
                    dietPieData.notifyDataChanged()
                    dietaryFiber!!.data = dietPieData
                    dietaryFiber!!.setUsePercentValues(true)
                    dietaryFiber!!.description.isEnabled = false
                    dietaryFiber!!.legend.isEnabled = false


                    //set colors for all piecharts
                    if(totalFatMaxCap){
                        fatPiedataset.colors = arrayListOf(errorRed)
                    } else if(fat0) {
                        fatPiedataset.colors = arrayListOf(grey)
                    }else {
                        //fatPiedataset.colors =arrayListOf(lightPurple,purple)
                        fatPiedataset.colors = arrayListOf(lightBlue,darkblue)
                    }

                    if(saturatedfatMaxCap){
                        saturatedPiedataset.colors = arrayListOf(errorRed)
                    } else if (satFat0) {
                        saturatedPiedataset.colors = arrayListOf(grey)
                    }else {
                        //saturatedPiedataset.colors = arrayListOf(lightPink,pink)
                        saturatedPiedataset.colors = arrayListOf(lightGreen,green)
                    }


                    if(cholesterolMaxCap){
                        cholesterolPiedataset.colors = arrayListOf(errorRed)
                    } else if(cholesterol0) {
                        cholesterolPiedataset.colors = arrayListOf(grey)
                    }else {
                        cholesterolPiedataset.colors =  arrayListOf(lightBlue,darkblue)
                    }


                    if(sodiumMaxCap){
                        sodiumPiedataset.colors = arrayListOf(errorRed)
                    } else if(sodium0) {
                        sodiumPiedataset.colors = arrayListOf(grey)
                    }else{
                        sodiumPiedataset.colors = arrayListOf(lightGreen,green)

                    }

                    if(carbMaxCap){
                        carbPiedataset.colors = arrayListOf(errorRed)
                    } else if(carb0) {
                        carbPiedataset.colors = arrayListOf(grey)
                    }else{
                        //carbPiedataset.colors = arrayListOf(lightYellow,yellow)
                        carbPiedataset.colors =  arrayListOf(lightBlue,darkblue)
                    }

                    if(dietMaxCap){
                        dietFiberPiedataset.colors = arrayListOf(errorRed)
                    } else if(diet0) {
                        dietFiberPiedataset.colors = arrayListOf(grey)
                    }else{
                        //dietFiberPiedataset.colors =arrayListOf(lightOrange,orange)
                        dietFiberPiedataset.colors = arrayListOf(lightGreen,green)
                    }

                    //animates the chart
                    animateCharts()

                    //displays the clicked value as a toast
                    displaySelectedValues()

                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "Information failed to load!", Toast.LENGTH_LONG)
                    .show()
                progressBar!!.visibility = View.GONE
            }


    }


    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun getColors(){
        //get the colors from colors.xml resources
        lightBlue = resources.getColor(R.color.lightBlue, null)
        lightGreen = resources.getColor(R.color.lightGreen, null)
        lightOrange = resources.getColor(R.color.lightOrange, null)
        lightPink = resources.getColor(R.color.lightPink, null)
        pink = resources.getColor(R.color.pink, null)
        purple = resources.getColor(R.color.colorAccent, null)
        lightPurple = resources.getColor(R.color.lightPurple, null)
        green = resources.getColor(R.color.colorPrimary, null)
        darkblue = resources.getColor(R.color.darkblue, null)
        orange = resources.getColor(R.color.orange, null)
        lightYellow = resources.getColor(R.color.lightYellow,null)
        yellow = resources.getColor(R.color.yellow,null)
        errorRed = resources.getColor(R.color.deepRed,null)
        textColor = Color.WHITE
        grey = resources.getColor(R.color.grey,null)
    }

    private fun animateCharts(){
        //animate each piechart
        totalFat!!.animateXY(1400, 1400)
        saturatedFat!!.animateXY(1400, 1400)
        cholesterol!!.animateXY(1400, 1400)
        sodium!!.animateXY(1400, 1400)
        totalCarbohydrate!!.animateXY(1400, 1400)
        dietaryFiber!!.animateXY(1400, 1400)
    }

    private fun displaySelectedValues(){
        totalFat!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null) {
                    return
                }
                Toast.makeText(this@DashboardActivity, "Selected total fat Value: " + e.y
                    , Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected() {
            }
        })

        saturatedFat!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null) {
                    return
                }
                Toast.makeText(this@DashboardActivity, "Selected saturated fat Value: " + e.y
                    , Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected() {
            }
        })

        cholesterol!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null) {
                    return
                }
                Toast.makeText(this@DashboardActivity, "Selected cholesterol Value: " + e.y
                    , Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected() {
            }
        })

        sodium!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null) {
                    return
                }
                Toast.makeText(this@DashboardActivity, "Selected sodium Value: " + e.y
                    , Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected() {
            }
        })

        totalCarbohydrate!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null) {
                    return
                }
                Toast.makeText(this@DashboardActivity, "Selected total carbohydrated Value: " + e.y
                    , Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected() {
            }
        })

        dietaryFiber!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null) {
                    return
                }
                Toast.makeText(this@DashboardActivity, "Selected dietary fiber Value: " + e.y
                    , Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected() {
            }
        })
    }

    private fun piechartTextColorAndRadiusDefinition(textColor:Int){
        //for each label set the text color
        totalFat!!.holeRadius = 1f
        totalFat!!.transparentCircleRadius = 2f
        totalFat!!.setEntryLabelColor(textColor)
        totalFat!!.notifyDataSetChanged()

        //for saturated fat
        saturatedFat!!.holeRadius = 1f
        saturatedFat!!.transparentCircleRadius = 2f
        saturatedFat!!.setEntryLabelColor(textColor)
        saturatedFat!!.notifyDataSetChanged()

        //for cholesterol
        cholesterol!!.holeRadius = 1f
        cholesterol!!.transparentCircleRadius = 2f
        cholesterol!!.setEntryLabelColor(textColor)
        cholesterol!!.notifyDataSetChanged()

        //for sodium
        sodium!!.holeRadius = 1f
        sodium!!.transparentCircleRadius = 2f
        sodium!!.setEntryLabelColor(textColor)
        sodium!!.notifyDataSetChanged()

        //for total carbohydrate
        totalCarbohydrate!!.holeRadius = 1f
        totalCarbohydrate!!.transparentCircleRadius = 2f
        totalCarbohydrate!!.setEntryLabelColor(textColor)
        totalCarbohydrate!!.notifyDataSetChanged()

        //for dietary fibers
        dietaryFiber!!.holeRadius = 1f
        dietaryFiber!!.transparentCircleRadius = 2f
        dietaryFiber!!.setEntryLabelColor(textColor)
        dietaryFiber!!.notifyDataSetChanged()


    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
        val defaultDiet: HashMap<String,Float> = hashMapOf(
            "Total Fat" to 65f,
            "Sat Fat" to 20f,
            "Cholesterol" to 300f,
            "Sodium" to 2400f,
            "Total Carbohydrate" to 300f,
            "Dietary Fiber" to 25f)
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun initializeUI() {
        nameTV = findViewById(R.id.nameView)
        progressBar = findViewById(R.id.progressBar)

        //get views for the labels
        totalFat = findViewById(R.id.totalFatChart)
        saturatedFat =  findViewById(R.id.saturatedFatChart)
        cholesterol =  findViewById(R.id.cholesterolChart)
        sodium =  findViewById(R.id.sodiumChart)
        totalCarbohydrate =  findViewById(R.id.totalCarbohydrateChart)
        dietaryFiber = findViewById(R.id.dietaryFiberChart)


        settingsButton = findViewById(R.id.settings_button)
        btn_camera = findViewById(R.id.button_camera)
    }

    //to clear the previous cached data
    private fun clearCharts(){

        hashMap.clear()
        totalfatArr.clear()
        saturatedfatArr.clear()
        cholesterolArr.clear()
        sodiumArr.clear()
        carbArr.clear()
        dietArr.clear()

        totalFat!!.invalidate()
        saturatedFat!!.invalidate()
        cholesterol!!.invalidate()
        sodium!!.invalidate()
        totalCarbohydrate!!.invalidate()
        dietaryFiber!!.invalidate()

        totalFat!!.clear()
        saturatedFat!!.clear()
        cholesterol!!.clear()
        sodium!!.clear()
        totalCarbohydrate!!.clear()
        dietaryFiber!!.clear()

    }

}