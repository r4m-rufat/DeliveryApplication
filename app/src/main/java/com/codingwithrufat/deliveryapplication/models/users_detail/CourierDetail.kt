package com.codingwithrufat.deliveryapplication.models.users_detail

data class CourierDetail(
    var courier_id: String? = null,
    var courier_latitude: Double? = null,
    var courier_longitude: Double? = null,
    var busy: Boolean? = null,
    var food_id: String? = null,
    var destination_latitude: Double? = null,
    var destination_longitude: Double? = null
)
