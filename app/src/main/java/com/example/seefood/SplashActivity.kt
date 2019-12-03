package com.example.seefood

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView


class SplashActivity : Activity() {

    private var SPLASH_TIME_OUT = 4000L;
    private var TIME = 0L;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashpage)


        val a = RotateAnimation(
            0.0f, 1440.0f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )

        val splash: ImageView = findViewById(R.id.splash_icon) as ImageView

        for (i in 1..4)
        {
            Handler().postDelayed(object :Runnable {
                public override fun run() {
                    a.duration = SPLASH_TIME_OUT/4
                    splash.startAnimation(a)
                    Handler().postDelayed(object:Runnable {
                        public override fun run() {
                            splash.setAnimation(null)
                        }
                    }, 500)
                }
            }, SPLASH_TIME_OUT/4 * i)
        }

//





        Handler().postDelayed(object:Runnable {
            public override fun run() {

                val homeIntent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(homeIntent)
                finish()
            }
        }, SPLASH_TIME_OUT)
    }
}