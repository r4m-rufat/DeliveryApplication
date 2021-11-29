package com.codingwithrufat.deliveryapplication.models

data class ClientDetail(
    var client_id:String?,
    var client_latitude:Double?,
    var client_longitude:Double?,
    var food_id:List<String>?
)
