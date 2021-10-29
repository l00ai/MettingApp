package com.example.metting.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.metting.R
import com.example.metting.utilities.Constants
import com.example.metting.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        db = Firebase.firestore
        preferenceManager = PreferenceManager(applicationContext)

        imageBack.setOnClickListener { onBackPressed() }
        textSignIn.setOnClickListener { onBackPressed() }

        btnSignUp.setOnClickListener {
            if (inputFirsName.text.toString().trim().isEmpty()
                || inputLastName.text.toString().trim().isEmpty()
                || inputPassword.text.toString().trim().isEmpty()
                || inputConfirmPassword.text.toString().trim().isEmpty()
            ) {
                Toast.makeText(this, "Fill fields", Toast.LENGTH_SHORT).show()
            } else if (!isEmailValid(inputEmail.text.toString())) {
                Toast.makeText(this, "Email not match", Toast.LENGTH_SHORT).show()
            } else if (inputPassword.text.toString() != inputConfirmPassword.text.toString()) {
                Toast.makeText(this, "Password and ConfirmPassword must be match", Toast.LENGTH_SHORT).show()
            } else {
                signUp()
            }
        }


    }

    private fun signUp() {
        btnSignUp.visibility = View.INVISIBLE
        signUpProgressBar.visibility = View.VISIBLE

        val user = hashMapOf(
            Constants.KEY_FIRST_NAME to inputFirsName.text.toString(),
            Constants.KEY_LAST_NAME to inputLastName.text.toString(),
            Constants.KEY_EMAIL to inputEmail.text.toString(),
            Constants.KEY_PASSWORD to inputPassword.text.toString()
        )
        db.collection(Constants.KEY_COLLECTION_USERS).add(user)
            .addOnSuccessListener {documentRef->
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, documentRef.id)
                preferenceManager.putString(Constants.KEY_FIRST_NAME, inputFirsName.text.toString())
                preferenceManager.putString(Constants.KEY_LAST_NAME, inputLastName.text.toString())
                preferenceManager.putString(Constants.KEY_EMAIL, inputEmail.text.toString())

                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                btnSignUp.visibility = View.VISIBLE
                signUpProgressBar.visibility = View.INVISIBLE
                Toast.makeText(this, "fail ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.toRegex().matches(email)
    }
}