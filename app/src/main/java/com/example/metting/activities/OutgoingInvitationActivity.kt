package com.example.metting.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.solver.widgets.ConstraintAnchor
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.metting.R
import com.example.metting.models.User
import com.example.metting.network.ApiClient
import com.example.metting.network.ApiService
import com.example.metting.utilities.Constants
import com.example.metting.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_outgoing_invitation.*
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class OutgoingInvitationActivity : AppCompatActivity() {

    private val preferenceManager: PreferenceManager by lazy {
        PreferenceManager(this)
    }
    private var inviterToken: String? = null
    private var meetingRoom: String? = null
    private var meetingType: String? = null
    private var user: User? = null
    private var rejectionCount = 0
    private var totalReceiver = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing_invitation)




        meetingType = intent.getStringExtra("type")

        if(meetingType != null){
            if (meetingType == "video"){
                imageMeetingType.setImageResource(R.drawable.ic_video)
            }else{
                imageMeetingType.setImageResource(R.drawable.ic_audio)
            }
        }

        if(intent.getSerializableExtra("user") != null){
             user = intent.getSerializableExtra("user") as User
            if (user != null){
                textFirstChar.text = user!!.firstName!!.substring(0, 1)
                textUsername.text = String.format("%s %s", user!!.firstName, user!!.lastName)
                textEmail.text = user!!.email
            }
        }


        imageStopInvitation.setOnClickListener {
            if (intent.getBooleanExtra("isMultiple", false)) {
                val type = object : TypeToken<ArrayList<User>>() {}.type
                val receivers =
                    Gson().fromJson<ArrayList<User>>(intent.getStringExtra("selectedUsers"), type)
                cancelInvitation(null, receivers)
            }else{
                if (user != null ){
                    cancelInvitation(user!!.token!!,null)
                }
            }

        }

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {task->
            if (task.isSuccessful && task.result != null){
                inviterToken = task.result!!.token

                if (meetingType != null){
                    if (intent.getBooleanExtra("isMultiple", false)){
                        val type =object :TypeToken<ArrayList<User>>(){}.type
                        val receivers = Gson().fromJson<ArrayList<User>>(intent.getStringExtra("selectedUsers"), type)
                        if (receivers != null){
                            totalReceiver = receivers.size
                        }
                        initiateMeeting(meetingType!!, null, receivers)
                    }else{
                        if (user != null){
                            totalReceiver = 1
                            initiateMeeting(meetingType!!, user!!.token!!, null)
                        }
                    }
                }

            }
        }


    }

    private fun initiateMeeting(meetingType: String, receiverToken: String?, receivers: ArrayList<User>?){
        try {
            val tokens = JSONArray()

            if (receiverToken != null){
                tokens.put(receiverToken)
            }

            if (!receivers.isNullOrEmpty()){
                val userNames = StringBuilder()
                for (i in receivers){
                    tokens.put(i.token)
                    userNames.append(i.firstName).append(" ").append(i.lastName).append("\n")
                }
                textFirstChar.visibility = View.GONE
                textUsername.text = userNames.toString()
                textEmail.visibility = View.GONE
            }

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION)
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType)
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME))
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME))
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL))
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken)

            meetingRoom = preferenceManager.getString(Constants.KEY_USER_ID) + "_" + UUID.randomUUID().toString().substring(0, 5)
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom)

            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION)

        }catch (exception: Exception){
            Toast.makeText(this@OutgoingInvitationActivity, exception.message , Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun sendRemoteMessage(remoteMessageBody: String, type: String){
        ApiClient().getClient().create(ApiService::class.java).sendRemoteMessage(
            Constants.getRemoteMessageHeader(), remoteMessageBody
        ).enqueue(object :Callback<String>{

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@OutgoingInvitationActivity, t.message, Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful){
                    if (type == Constants.REMOTE_MSG_INVITATION){
                        Toast.makeText(this@OutgoingInvitationActivity, "invitation send successfully", Toast.LENGTH_SHORT).show()
                    }else if (type == Constants.REMOTE_MSG_INVITATION_RESPONSE){
                        Toast.makeText(this@OutgoingInvitationActivity, "invitation canceled", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }else{
                    Toast.makeText(this@OutgoingInvitationActivity, response.message(), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

        })
    }

    private fun cancelInvitation(receiverToken: String?, receiver: ArrayList<User>?) {
        try {
            val tokens = JSONArray()

            if(receiverToken != null){
                tokens.put(receiverToken)
            }

            if (!receiver.isNullOrEmpty()){
                for(user in receiver){
                    tokens.put(user.token)
                }
            }

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELED)

            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE)

        } catch (exception: Exception) {
            Toast.makeText(this@OutgoingInvitationActivity, exception.message, Toast.LENGTH_SHORT)
                .show()
            finish()
        }
    }

    private val invitationResponseReceiver = object :BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val type = intent!!.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)
            if (type != null){
                if (type == Constants.REMOTE_MSG_INVITATION_ACCEPTED){
                    try {
                        val serverURL = URL("https://meet.jit.si")

                        val builder = JitsiMeetConferenceOptions.Builder()
                        builder.setServerURL(serverURL)
                            .setWelcomePageEnabled(false)
                            .setRoom(meetingRoom)
                            if (meetingType == "audio"){
                            //    builder.setVideoMuted(true)
                                builder.setAudioOnly(true)
                            }

                        JitsiMeetActivity.launch(this@OutgoingInvitationActivity, builder.build())
                        finish()
                    }catch (exception: Exception){
                        Toast.makeText(this@OutgoingInvitationActivity, exception.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }else if(type == Constants.REMOTE_MSG_INVITATION_REJECT){
                    rejectionCount += 1
                    if(rejectionCount == totalReceiver){
                        Toast.makeText(this@OutgoingInvitationActivity, "Invitation Rejected", Toast.LENGTH_SHORT).show()
                        finish()
                    }

                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseReceiver, IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(invitationResponseReceiver)
    }

}