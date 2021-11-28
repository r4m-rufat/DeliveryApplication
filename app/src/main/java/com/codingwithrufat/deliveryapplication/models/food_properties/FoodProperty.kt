package com.codingwithrufat.deliveryapplication.models.food_properties

data class FoodProperty(
    var food_id: String? = null,
    var food_name: String? = null,
    var food_weight: String? = null,
    var food_height: String? = null,
    var food_width: String? = null,
    var order_time: String? = null,
    var source_latitude: Double? = null,
    var source_longitude: Double? = null,
    var destination_longitude: Double? = null,
    var destination_latitude: Double? = null
)