package com.codingwithrufat.deliveryapplication

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
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
import kotlin.random.Random

private const val CHANNEL_ID = "my_channel"
class FirebaseMessagingService : FirebaseMessagingService() {



    override fun onNewToken(token: String?) {
        super.onNewToken(token)
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

//        // playing audio and vibration
//        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val r = RingtoneManager.getRingtone(applicationContext, notification)
//        r.play()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            r.isLooping = false
//        }
//
//        // vibration
//        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        val vibrationEffect=VibrationEffect.createOneShot(300,VibrationEffect.PARCELABLE_WRITE_RETURN_VALUE)
//        vibrator.vibrate(vibrationEffect)

//        val builder = NotificationCompat.Builder(this, "CHANNEL_ID")
//        builder.setSmallIcon(R.mipmap.ic_launcher)
//
//
//        val resultIntent = Intent(applicationContext,CourierActivity::class.java)
//
//        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//
//        resultIntent.putExtra("user_id", remoteMessage.data["client_id"]) // this one is client_id
//        val pendingIntent =
//            PendingIntent.getActivity(this,
//                System.currentTimeMillis().toInt(), resultIntent, FLAG_ONE_SHOT)
//        builder.setContentTitle(remoteMessage.notification!!.title)
//        builder.setContentText(remoteMessage.data["client_id"])
//        builder.setContentIntent(pendingIntent)
//        builder.setStyle(
//            NotificationCompat.BigTextStyle().bigText(
//                remoteMessage.notification!!.body
//            )
//        )
//        builder.setAutoCancel(true)
//        builder.priority = NotificationManager.IMPORTANCE_HIGH
//        mNotificationManager =
//            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channelId = "Your_channel_id"
//            val channel = NotificationChannel(
//                channelId,
//                "Channel human readable title",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            mNotificationManager!!.createNotificationChannel(channel)
//        }
//
//        // notificationId is a unique int for each notification that you must define
//        mNotificationManager!!.notify(Random.nextInt(), builder.build())

        val intent = Intent(this, CourierActivity::class.java)
        intent.putExtra("client_id", remoteMessage.data["client_id"])
        intent.putExtra("food_id", remoteMessage.data["food_id"])
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){

        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "My channel description"
        }

        notificationManager.createNotificationChannel(channel)

    }

}
