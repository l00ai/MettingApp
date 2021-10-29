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
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        db = Firebase.firestore
        preferenceManager = PreferenceManager(applicationContext)

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        textSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btnSignIn.setOnClickListener {
            if (!isEmailValid(inputEmail.text.toString())){
                Toast.makeText(this, "Email not match", Toast.LENGTH_SHORT).show()
            }else if (inputPassword.text.toString().trim().isEmpty()){
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
            }else {
                signIn()
            }
        }


    }

    private fun signIn() {
        signInProgressBar.visibility = View.VISIBLE
        btnSignIn.visibility = View.INVISIBLE

        db.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, inputEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, inputPassword.text.toString())
            .get()
            .addOnCompleteListener {task->
                if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0 ){
                    val documentSnapshot = task.result!!.documents[0]
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                    preferenceManager.putString(Constants.KEY_FIRST_NAME, documentSnapshot.getString(Constants.KEY_FIRST_NAME)!!)
                    preferenceManager.putString(Constants.KEY_LAST_NAME, documentSnapshot.getString(Constants.KEY_LAST_NAME)!!)
                    preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL)!!)

                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }else{
                    signInProgressBar.visibility = View.INVISIBLE
                    btnSignIn.visibility = View.VISIBLE
                    Toast.makeText(this, "Unable to sign in", Toast.LENGTH_SHORT).show()
                }
            }


    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.toRegex().matches(email)
    }
}