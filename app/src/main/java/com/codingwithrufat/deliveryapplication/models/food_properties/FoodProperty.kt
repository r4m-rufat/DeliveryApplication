package com.codingwithrufat.deliveryapplication.models.food_properties

data class FoodProperty(
    var food_id:String?,
    var food_name:String?,
    var food_weight:String?,
    var food_heigth:String?,
    var food_width:String?,
    var food_length:String?,
    var order_time:String?,
    var source_latitude:Double?,
    var source_longitude:Double?,
    var destination_longitude:Double?,
    var destination_latitude:Double?
)
