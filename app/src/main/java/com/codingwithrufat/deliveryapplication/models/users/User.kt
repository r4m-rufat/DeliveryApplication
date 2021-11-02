package com.codingwithrufat.deliveryapplication.models.users

data class User(
    var id: String? = null,
    var phone_number: String? = null,
    var username: String? = null,
    var password: String? = null,
    var type: String? = null
)
