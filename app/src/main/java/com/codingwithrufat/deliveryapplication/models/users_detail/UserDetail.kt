package com.codingwithrufat.deliveryapplication.models.users_detail

data class UserDetail(
    var id: String? = null,
    var phone_number: String? = null,
    var username: String? = null,
    var password: String? = null,
    var type: String? = null,
    var locationLat: String? = null,
    var locationLong: String? = null
)
