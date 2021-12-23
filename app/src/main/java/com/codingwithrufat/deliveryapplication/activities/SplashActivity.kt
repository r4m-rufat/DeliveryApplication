package com.codingwithrufat.deliveryapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.codingwithrufat.deliveryapplication.R
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val txt_anim = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.interpolato_anim);
        val logo_anim = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.scale_up_anim)

        app_name.startAnimation(txt_anim)
        ic_logo.startAnimation(logo_anim)

        CoroutineScope(Main).launch {
            delay(2000L)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }

    }
}