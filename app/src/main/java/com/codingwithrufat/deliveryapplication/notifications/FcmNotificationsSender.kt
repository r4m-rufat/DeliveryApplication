package com.codingwithrufat.deliveryapplication.notifications

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import com.codingwithrufat.deliveryapplication.utils.constants.TAG
import com.codingwithrufat.deliveryapplication.utils.constants.postUrl
import com.codingwithrufat.deliveryapplication.utils.constants.fcmServerKey

class FcmNotificationsSender(
    var userFcmToken: String,
    var title: String,
    var body: String,
    var client_id: String,
    var food_id: String,
    mContext: Context,
    mActivity: Activity
) {

    var mContext: Context
    var mActivity: Activity
    lateinit  var requestQueue: RequestQueue

    fun SendNotifications() {
        requestQueue = Volley.newRequestQueue(mActivity)
        val mainObj = JSONObject()
        try {
            mainObj.put("to", userFcmToken)
            val notiObject = JSONObject()
            val dataObject = JSONObject()
            dataObject.put("client_id", client_id)
            dataObject.put("food_id", food_id)
            notiObject.put("title", title)
            notiObject.put("body", body)
            mainObj.put("notification", notiObject)
            mainObj.put("data", dataObject)
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                postUrl,
                mainObj,
                Response.Listener<JSONObject?> {
                    // code run is got response
                },
                Response.ErrorListener { error -> Log.d(TAG,"Error"+error?.message.toString()) }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String>? {
                    val header: MutableMap<String, String> = HashMap()
                    header["content-type"] = "application/json"
                    header["authorization"] = "key=$fcmServerKey"
                    return header
                }
            }
            requestQueue.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    init {
        this.mContext = mContext
        this.mActivity = mActivity
    }
}

