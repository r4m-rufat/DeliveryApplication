package com.codingwithrufat.deliveryapplication.utils.conditions

import android.view.View
import kotlinx.android.synthetic.main.fragment_register.view.*

fun checkingRegistrationFields(
    view: View,
    phoneNumber: String,
    username: String,
    password: String
): Boolean {

    var boolNumber = false
    var boolUsername = false
    var boolPassword = false

    if (phoneNumber.length == 9) {
        boolNumber = true
    } else {
        view.edit_phone_number.setError("Phone number is not valid")
    }

    if (username.length >= 4) {
        boolUsername = true
    } else {
        view.edit_username.setError("Username must be greater than 4")
    }

    if (password.length >= 6) {
        boolPassword = true
    }

    return boolNumber && boolUsername && boolPassword

}
