package com.codingwithrufat.deliveryapplication.models



data class Couriers(
    var courier_id:String?,
    var courier_latitude: Double?,
    var courier_longitude:Double?,
    var busy:Boolean?,
    var food_id:String?,
    var destination_latitude:Double?,
    var destination_longitude: Double?
)
