package com.codingwithrufat.deliveryapplication.utils.prefence

import android.content.Context
import com.codingwithrufat.deliveryapplication.utils.constants.SHARD_PREFENCE

class MyPrefence(context: Context?) {


    val prefence=context?.getSharedPreferences(SHARD_PREFENCE,Context.MODE_PRIVATE)

    fun getBoolen(key:String):Boolean{
        return prefence!!.getBoolean(key,true)
    }
    fun setBoolen(key:String,bl:Boolean){
        val editor=prefence!!.edit()
        editor.putBoolean(key,bl)
        editor.apply()
    }
    fun getString(key:String): String? {
        return prefence!!.getString(key,null)
    }
    fun setString(key:String,st:String){
        val editor=prefence!!.edit()
        editor.putString(key,st)
        editor.apply()
    }

}