package com.example.metting.utilities

class Constants {

    companion object {
        const val KEY_COLLECTION_USERS = "users"
        const val KEY_FIRST_NAME = "first_name"
        const val KEY_LAST_NAME = "last_name"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_USER_ID = "user_id"
        const val KEY_FCM_TOKEN = "fcm_token"

        const val KEY_PREFERENCE_NAME = "videoMeetingPreference"
        const val KEY_IS_SIGNED_IN = "isSignedIn"

        const val REMOTE_MSG_AUTHORIZATION = "Authorization"
        const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"

        const val REMOTE_MSG_TYPE = "type"
        const val REMOTE_MSG_INVITATION = "invitation"
        const val REMOTE_MSG_MEETING_TYPE = "meetingType"
        const val REMOTE_MSG_INVITER_TOKEN = "invitationToken"
        const val REMOTE_MSG_DATA = "data"
        const val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"

        const val REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse"

        const val REMOTE_MSG_INVITATION_ACCEPTED = "accepted"
        const val REMOTE_MSG_INVITATION_REJECT = "reject"
        const val REMOTE_MSG_INVITATION_CANCELED = "canceled"

        const val REMOTE_MSG_MEETING_ROOM = "meetingRoom"

        fun getRemoteMessageHeader(): HashMap<String, String> {
            return hashMapOf(
                REMOTE_MSG_CONTENT_TYPE to "application/json",
                REMOTE_MSG_AUTHORIZATION to "key=AAAAmdZRetQ:APA91bFGGJwSbM4piN6A04aFve6c9anx1U9vCkYGm9reS0T3o-ur_ZmS9qXkzay2PCGWCpckPYoCVvGuyFmzTNiIOHhEKc-9gQOGZD6ks_nKDAu5RqpB5Mbc-xb31la1sm7ZycA90FgY"
            )
        }
    }
}