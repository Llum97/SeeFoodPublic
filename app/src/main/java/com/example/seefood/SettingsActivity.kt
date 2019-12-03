package com.example.seefood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.auth.EmailAuthProvider
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.layout_add_intake.view.*
import kotlinx.android.synthetic.main.layout_change_username.view.*
import kotlinx.android.synthetic.main.layout_change_password.view.*
import kotlinx.android.synthetic.main.layout_change_username.view.dialogCancelBtn
import kotlinx.android.synthetic.main.layout_change_username.view.dialogConfirmButton

class SettingsActivity : AppCompatActivity() {

    private var changePassword : Button? = null
    private var changeUsername : Button? = null
    private var clearData : Button? = null
    private var addIntake : Button? = null
    private var returnToDashboard : Button? = null
    private var mAuth: FirebaseAuth? = null
    private var mlogout: Button? = null
    private var passwordDialog : View? = null
    private var mDialogView : View? = null
    private var passwordDialogBuilder : AlertDialog.Builder? = null
    private var mBuilder : AlertDialog.Builder? = null
    private var clearDataDialog : View? = null
    private var clearDataDialogBuilder : AlertDialog.Builder? = null
    private var addIntakeDialog : View? = null
    private var addIntakeDialogBuilder : AlertDialog.Builder? = null
    private var selectedNutrient : String = ""

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mAuth = FirebaseAuth.getInstance()
        initializeUI()
        intializeButton()
    }

    private fun intializeButton() {
        returnToDashboard!!.setOnClickListener {
            val intent = Intent(this@SettingsActivity, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        changePassword!!.setOnClickListener {
            passwordDialog = LayoutInflater.from(this).inflate(R.layout.layout_change_password, null)
            passwordDialogBuilder = AlertDialog.Builder(this)
                .setView(passwordDialog)
                .setTitle("Change Password")
            val  mAlertDialog = passwordDialogBuilder!!.show()
            passwordDialog!!.dialogConfirmButton.setOnClickListener {
                mAlertDialog.dismiss()
                val oldPassword = passwordDialog!!.oldPassword.text.toString()
                val newPassword = passwordDialog!!.newPassword.text.toString()
                val confirmPassword = passwordDialog!!.confirmNewPassword.text.toString()

                if (TextUtils.isEmpty(oldPassword)) {
                    Toast.makeText(applicationContext, "Enter Old Password", Toast.LENGTH_LONG).show()
                }
                if (TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(applicationContext, "Enter New Password", Toast.LENGTH_LONG).show()
                }
                if (TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(applicationContext, "Enter Confirm Password", Toast.LENGTH_LONG).show()
                }
                if (newPassword != confirmPassword)
                {
                    Toast.makeText(
                        applicationContext,
                        "New password and confirm password does not match.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else
                {
                    val currUser = mAuth!!.currentUser
                    val credential = EmailAuthProvider
                        .getCredential(currUser!!.email.toString(), oldPassword)

                    currUser!!.reauthenticate(credential).addOnCompleteListener{
                            task ->
                        if (task.isSuccessful) {
                            currUser!!.updatePassword(newPassword).addOnCompleteListener{
                                    update_task ->
                                if (update_task.isSuccessful)
                                {
                                    Toast.makeText(
                                        applicationContext,
                                        "Password updated.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                else
                                {
                                    Toast.makeText(
                                        applicationContext,
                                        "Password update failed. Please try again.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        else {
                            Toast.makeText(
                                applicationContext,
                                "Password update failed. Please try again.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                }
            }
            passwordDialog!!.dialogCancelBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }



        }

        changeUsername!!.setOnClickListener {
            mDialogView = LayoutInflater.from(this).inflate(R.layout.layout_change_username, null)
            mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Change Username")
            val  mAlertDialog = mBuilder!!.show()
            mDialogView!!.dialogConfirmButton.setOnClickListener {
                mAlertDialog.dismiss()
                val newName = mDialogView!!.newUsername.text.toString()
                val confirmName = mDialogView!!.confirmNewUsername.text.toString()

                if (TextUtils.isEmpty(newName)) {
                    Toast.makeText(applicationContext, "Enter email", Toast.LENGTH_LONG).show()
                }
                if (TextUtils.isEmpty(confirmName)) {
                    Toast.makeText(applicationContext, "Enter password", Toast.LENGTH_LONG).show()
                }
                if (newName != confirmName)
                {
                    Toast.makeText(
                        applicationContext,
                        "New name and confirm name does not match.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else
                {
                    val docRef = db.collection("users").document(mAuth!!.currentUser!!.uid)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val doc = document.data
                                doc!!.set("name", newName)
                                docRef.update(doc)
                                Toast.makeText(applicationContext,"Username changed.",Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(applicationContext, "Information does not exist!", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(applicationContext, "Information failed to load!", Toast.LENGTH_LONG).show()
                        }
                }
            }
            mDialogView!!.dialogCancelBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }

        clearData!!.setOnClickListener {
            clearDataDialog = LayoutInflater.from(this).inflate(R.layout.layout_clear_data, null)
            clearDataDialogBuilder = AlertDialog.Builder(this)
                .setView(clearDataDialog)
                .setTitle("Are you sure to clear all data?")
            val  mAlertDialog = clearDataDialogBuilder!!.show()
            clearDataDialog!!.dialogConfirmButton.setOnClickListener {
                mAlertDialog.dismiss()
                emptyData(nutrition)
            }
            clearDataDialog!!.dialogCancelBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }

        addIntake!!.setOnClickListener {
            addIntakeDialog = LayoutInflater.from(this).inflate(R.layout.layout_add_intake, null)
            addIntakeDialogBuilder = AlertDialog.Builder(this)
                .setView(addIntakeDialog)
                .setTitle("Add number of mg to values of nutrition.")
            val  mAlertDialog = addIntakeDialogBuilder!!.show()

            addIntakeDialog!!.dialogConfirmButton.setOnClickListener {

                if (TextUtils.isEmpty(selectedNutrient))
                {
                    Toast.makeText(applicationContext, "Choose nutrients.", Toast.LENGTH_LONG).show()
                }
                else
                {
                    var amt = addIntakeDialog!!.amount!!.text.toString()
                    addManualIntake(amt, selectedNutrient)
                }



                mAlertDialog.dismiss()
            }
            addIntakeDialog!!.dialogCancelBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }

        mlogout!!.setOnClickListener {
            val docRef = db.collection("users")
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()

        }

    }

    fun set_chol(view: View) {
        selectedNutrient = "Cholesterol"
    }

    fun set_sat_fat(view: View) {
        selectedNutrient = "Saturated Fat"
    }

    fun set_tot_fat(view: View) {
        selectedNutrient = "Total Fat"
    }

    fun set_sodium(view: View) {
        selectedNutrient = "Sodium"
    }

    fun set_diet_fib(view: View) {
        selectedNutrient = "Dietary Fiber"
    }

    fun set_tot_carb(view: View) {
        selectedNutrient = "Total Carbohydrate"
    }

    private fun addManualIntake(nutrition: String, name: String)
    {
        val docRef = db.collection("users").document(mAuth!!.currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val doc = document.data
                    if (!TextUtils.isEmpty(nutrition)) {
                        var currNutrition = document!!.getDouble(name)!!.toFloat()
                        doc!!.set(name, currNutrition + nutrition.toFloat())
                        docRef.update(doc)
                        Toast.makeText(applicationContext,"Data added.",Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Information does not exist!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "Information failed to load!", Toast.LENGTH_LONG).show()
            }

    }

    private fun emptyData(names:ArrayList<String>) {

        val data: HashMap<String,Float> = hashMapOf()

        for (i in 0 until names.size) {
            data.put(names[i], 0f)
        }


        db.collection("users").document((mAuth!!.currentUser!!.uid))
            .set(data, SetOptions.merge()).addOnCompleteListener {
                    task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Data cleared!", Toast.LENGTH_LONG).show()

                }
                else {
                    Toast.makeText(applicationContext, "Failed to clear data. Please try again.", Toast.LENGTH_LONG).show()

                }

            }
    }

    private fun initializeUI() {
        changeUsername = findViewById(R.id.changeUsername)
        changePassword = findViewById(R.id.changePassword)
        clearData = findViewById(R.id.clearData)
        addIntake = findViewById(R.id.addIntake)
        returnToDashboard = findViewById(R.id.returnToDashboard)
        mlogout = findViewById(R.id.logout)
    }



    companion object {
        val nutrition : ArrayList<String> = arrayListOf(
            "Cholesterol",
            "Dietary Fiber",
            "Saturated Fat",
            "Sodium",
            "Total Carbohydrate",
            "Total Fat")
    }
}
