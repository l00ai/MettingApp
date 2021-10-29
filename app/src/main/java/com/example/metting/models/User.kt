package com.example.metting.models

import java.io.Serializable

data class User(var firstName: String?, var lastName: String?, var email: String?, var token: String?): Serializable {

    constructor(): this("", "","","")
}