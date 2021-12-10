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
            notiObject.put("title", title)
            notiObject.put("body", body)
            mainObj.put("notification", notiObject)
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Request.Method.POST,
                postUrl,
                mainObj,
                object : Response.Listener<JSONObject?>{
                    override fun onResponse(response: JSONObject?) {

                        // code run is got response
                    }
                },
                object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError?) {
                        Log.d(TAG,"Error"+error?.message.toString())
                    }
                }) {
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

