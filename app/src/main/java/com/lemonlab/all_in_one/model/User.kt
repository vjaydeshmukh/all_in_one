package com.lemonlab.all_in_one.model

data class User(var name: String, var email: String, var online: String){
    constructor(): this(
        "","","false"
    )
}