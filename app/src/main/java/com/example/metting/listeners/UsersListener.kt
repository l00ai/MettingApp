package com.example.metting.listeners

import com.example.metting.models.User

interface UsersListener {

    fun initiateVideoMeeting(user: User)

    fun initiateAudioMeeting(user: User)

    fun onMultipleUserAction(isMultipleUsersSelected: Boolean)

}