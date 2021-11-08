package com.codingwithrufat.deliveryapplication.utils.conditions

import android.view.View
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.android.synthetic.main.fragment_register.view.edit_phone_number
import kotlinx.android.synthetic.main.fragment_reset_password.view.*

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

fun checkChangePasswordFields(
    view: View,
    new_password: String,
    confirm_password: String
): Boolean{

    var bool_new_password = false
    var bool_confirm_password = false
    var bool_matches = false

    if (new_password.length >= 6){
        bool_new_password = true
    }else{
        view.edit_new_password.setError("Password must be 6 and more characters")
    }

    if (confirm_password.length >= 6){
        bool_confirm_password = true
    }else{
        view.edit_confirm_password.setError("Password must be 6 and more characters")
    }

    if (new_password == confirm_password){
        bool_matches = true
    }else{
        view.edit_confirm_password.setError("Password doesn't match with another password")
    }

    return bool_confirm_password && bool_new_password && bool_matches

}

fun checkLoginFields(
    view: View,
    phoneNumber: String,
    password: String
): Boolean{

    var bool_phone_number = false
    var bool_password = false

    if (phoneNumber.length == 9){
        bool_phone_number = true
    }else{
        view.edit_phone_number_login.setError("Phone number is invalid")
    }

    if (password.length >= 6){
        bool_password = true
    }else{
        view.txt_input_password_login.setError("Password must be 6 and more characters")
    }

    return bool_password && bool_phone_number

}
