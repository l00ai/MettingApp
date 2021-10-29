package com.example.metting.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.metting.R
import com.example.metting.listeners.UsersListener
import com.example.metting.models.User
import kotlinx.android.synthetic.main.item_container_user.view.*

class UsersAdapter(var data: ArrayList<User>, var usersListener: UsersListener): RecyclerView.Adapter<UsersAdapter.MyViewHolder>() {

    private val selectedUsers = ArrayList<User>()

    fun getSelectedUsers(): ArrayList<User>{
        return selectedUsers
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val textFirstChar = itemView.textFirstChar
        private val textUsername = itemView.textUsername
        private val textEmail = itemView.textEmail
        private val imageAudioMeeting = itemView.imageAudioMeeting
        private val imageVideoMeeting = itemView.imageVideoMeeting
        private val userContainer = itemView.userContainer
        private val imageSelected = itemView.imageSelected

        fun setUserData(user: User){
            textFirstChar.text = user.firstName!!.substring(0, 1)
            textUsername.text = String.format("%s %s", user.firstName, user.lastName)
            textEmail.text = user.email

            imageAudioMeeting.setOnClickListener {
                usersListener.initiateAudioMeeting(user)
            }

            imageVideoMeeting.setOnClickListener {
                usersListener.initiateVideoMeeting(user)
            }

            userContainer.setOnLongClickListener {
                if (imageSelected.visibility == View.VISIBLE){
                    selectedUsers.add(user)
                    imageSelected.visibility = View.VISIBLE
                    imageVideoMeeting.visibility = View.GONE
                    imageAudioMeeting.visibility = View.GONE
                    usersListener.onMultipleUserAction(true)
                }
                true
            }

            userContainer.setOnClickListener {
                if (imageSelected.visibility == View.VISIBLE){
                    selectedUsers.remove(user)
                    imageSelected.visibility = View.GONE
                    imageVideoMeeting.visibility = View.VISIBLE
                    imageAudioMeeting.visibility = View.VISIBLE
                    if (selectedUsers.size == 0 ){
                        usersListener.onMultipleUserAction(false)
                    }
                }else{
                    if(selectedUsers.size > 0){
                        selectedUsers.add(user)
                        imageSelected.visibility = View.VISIBLE
                        imageVideoMeeting.visibility = View.GONE
                        imageAudioMeeting.visibility = View.GONE
                    }
                }
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_container_user, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
       return data.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setUserData(data[position])
    }


}