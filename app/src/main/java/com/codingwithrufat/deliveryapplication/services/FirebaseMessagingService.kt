package com.codingwithrufat.deliveryapplication

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.codingwithrufat.deliveryapplication.activities.CourierActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {


    var mNotificationManager: NotificationManager? = null
    override fun onNewToken(token: String?) {
        super.onNewToken(token)
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // playing audio and vibration
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(applicationContext, notification)
        r.play()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.isLooping = false
        }

        // vibration
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect=VibrationEffect.createOneShot(300,VibrationEffect.PARCELABLE_WRITE_RETURN_VALUE)
        vibrator.vibrate(vibrationEffect)

        val builder = NotificationCompat.Builder(this, "CHANNEL_ID")
        builder.setSmallIcon(R.mipmap.ic_launcher)


        val resultIntent = Intent(this,CourierActivity ::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 1, resultIntent, FLAG_UPDATE_CURRENT)
        builder.setContentTitle(remoteMessage.notification!!.title)
        var notificationBody=remoteMessage.notification!!.body
        builder.setContentText(notificationBody?.substring(0,notificationBody.indexOf(":")))
        builder.setContentIntent(pendingIntent)
        builder.setStyle(
            NotificationCompat.BigTextStyle().bigText(
                remoteMessage.notification!!.body
            )
        )
        builder.setAutoCancel(true)
        builder.priority = NotificationManager.IMPORTANCE_HIGH
        mNotificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "Your_channel_id"
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            mNotificationManager!!.createNotificationChannel(channel)
            builder.setChannelId(channelId)
        }

        // notificationId is a unique int for each notification that you must define
        mNotificationManager!!.notify(100, builder.build())
    }

}
