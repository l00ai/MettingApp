package com.example.metting.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.metting.R
import com.example.metting.adapters.UsersAdapter
import com.example.metting.listeners.UsersListener
import com.example.metting.models.User
import com.example.metting.utilities.Constants
import com.example.metting.utilities.PreferenceManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), UsersListener {

    private val preferenceManager: PreferenceManager by lazy {
        PreferenceManager(this)
    }
    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }
    private lateinit var usersAdapter: UsersAdapter
    private val data = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        textTitle.text = String.format(
            "%s %s",
            preferenceManager.getString(Constants.KEY_FIRST_NAME),
            preferenceManager.getString(Constants.KEY_LAST_NAME)
        )

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                sendFCMTokenToDatabase(task.result!!.token)
            }
        }

        textSignOut.setOnClickListener {
            signOut()
        }


        usersAdapter = UsersAdapter(data, this)
        usersRecyclerView.adapter = usersAdapter

        swipeRefreshLayout.setOnRefreshListener(this::getUsers)
        getUsers()
    }

    private fun getUsers(){
        swipeRefreshLayout.isRefreshing = true
        db.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener { task->
                swipeRefreshLayout.isRefreshing = false
                val myUserId = preferenceManager.getString(Constants.KEY_USER_ID)
                if (task.isSuccessful && task.result != null){
                    data.clear()
                    for (documentSnapshot in task.result!!){
                        if (myUserId == documentSnapshot.id){
                            continue
                        }else{
                            val user = User()
                            user.firstName = documentSnapshot.getString(Constants.KEY_FIRST_NAME)
                            user.lastName = documentSnapshot.getString(Constants.KEY_LAST_NAME)
                            user.email = documentSnapshot.getString(Constants.KEY_EMAIL)
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN)
                            data.add(user)
                        }
                    }
                    if (data.isNotEmpty()){
                        usersAdapter.notifyDataSetChanged()
                    }else{
                        textErrorMessage.text = String.format("%s", "No users available")
                        textErrorMessage.visibility = View.VISIBLE
                    }

                }else{
                    textErrorMessage.text = String.format("%s", "No users available")
                    textErrorMessage.visibility = View.VISIBLE
                }
            }
    }

    private fun sendFCMTokenToDatabase(token: String) {
        val documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID)!!)

        documentReference.update(Constants.KEY_FCM_TOKEN, token)
            .addOnFailureListener {
                Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun signOut() {
        Toast.makeText(this, "Signing Out ...", Toast.LENGTH_SHORT).show()
        val documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID)!!)

        val updates = mapOf(
            Constants.KEY_FCM_TOKEN to FieldValue.delete()
        )
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager.clearPreference()
                startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Unable to sign Out ", Toast.LENGTH_SHORT).show()
            }
    }

    override fun initiateVideoMeeting(user: User) {
        if (user.token == null || user.token!!.trim().isEmpty()){
            Toast.makeText(this, "${user.firstName} ${user.lastName} is not available for meeting", Toast.LENGTH_SHORT).show()
        }else{
            val intent = Intent(this, OutgoingInvitationActivity::class.java)
            intent.putExtra("user", user)
            intent.putExtra("type", "video")
            startActivity(intent)

        }
    }

    override fun initiateAudioMeeting(user: User) {
        if (user.token == null || user.token!!.trim().isEmpty()){
            Toast.makeText(this, "${user.firstName} ${user.lastName} is not available for meeting", Toast.LENGTH_SHORT).show()
        }else{
            val intent = Intent(this, OutgoingInvitationActivity::class.java)
            intent.putExtra("user", user)
            intent.putExtra("type", "audio")
            startActivity(intent)


        }
    }

    override fun onMultipleUserAction(isMultipleUsersSelected: Boolean) {
        if(isMultipleUsersSelected){
            imageConference.visibility = View.VISIBLE
            imageConference.setOnClickListener {
                val intent = Intent(applicationContext, OutgoingInvitationActivity::class.java)
                intent.putExtra("selectedUsers", Gson().toJson(usersAdapter.getSelectedUsers()))
                intent.putExtra("type", "video")
                intent.putExtra("isMultiple", true)
                startActivity(intent)
            }
        }else{
            imageConference.visibility = View.GONE
        }
    }
}