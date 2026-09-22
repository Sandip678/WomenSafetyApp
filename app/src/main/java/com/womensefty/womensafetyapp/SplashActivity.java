package com.womensefty.womensafetyapp;

import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // ImageView
        ImageView logo = findViewById(R.id.logo);

        // Scale Animation
        Animation scaleUpAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up);

        //Animation on ImageView
        logo.startAnimation(scaleUpAnimation);

        // automatic redirect login page
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity .class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}