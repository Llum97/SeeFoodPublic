package com.example.seefood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationActivity : AppCompatActivity() {

    private var emailTV: EditText? = null
    private var nameTV: EditText? = null
    private var passwordTV: EditText? = null
    private var regBtn: Button? = null
    private var progressBar: ProgressBar? = null

    private var mAuth: FirebaseAuth? = null
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        mAuth = FirebaseAuth.getInstance()


        initializeUI()

        regBtn!!.setOnClickListener { registerNewUser() }
    }


    private fun registerNewUser() {
        progressBar!!.visibility = View.VISIBLE

        val email: String
        val password: String
        val name: String
        email = emailTV!!.text.toString()
        password = passwordTV!!.text.toString()
        name = nameTV!!.text.toString()

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(applicationContext, "Please enter name...", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(applicationContext, "Please enter email...", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(applicationContext, "Please enter password!", Toast.LENGTH_LONG).show()
            return
        }


        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val map = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "Cholesterol" to 0f,
                        "Dietary Fiber" to 0f,
                        "Saturated Fat" to 0f,
                        "Sodium" to 0f,
                        "Total Carbohydrate" to 0f,
                        "Total Fat" to 0f
//
                    )
                    val userID = mAuth?.currentUser?.uid!!
//
                    db.collection("users").document(userID)
                        .set(map)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()
                            progressBar!!.visibility = View.GONE



                            val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, "Adding database failed! Please try again later", Toast.LENGTH_LONG).show()
                            progressBar!!.visibility = View.GONE
                        }


                } else {
                    Toast.makeText(applicationContext, "Registration failed! Please try again later", Toast.LENGTH_LONG).show()
                    progressBar!!.visibility = View.GONE
                }
            }
    }

    private fun initializeUI() {
        emailTV = findViewById(R.id.email)
        passwordTV = findViewById(R.id.password)
        nameTV = findViewById(R.id.name)
        regBtn = findViewById(R.id.register)
        progressBar = findViewById(R.id.progressBar)
    }
}
