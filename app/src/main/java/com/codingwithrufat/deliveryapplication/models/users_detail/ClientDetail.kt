package com.codingwithrufat.deliveryapplication.models.users_detail

data class ClientDetail(
    var client_id: String? = null,
    var client_latitude: Double? = null,
    var client_longitude: Double? = null,
    var food_id: String? = null
)
