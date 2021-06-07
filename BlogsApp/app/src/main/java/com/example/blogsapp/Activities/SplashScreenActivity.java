package com.example.blogsapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.blogsapp.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        getSupportActionBar().hide();

        Thread thread = new Thread(){

            public void run()
            {
                try {

                    sleep(3000);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally {

                    Intent splashScreen = new Intent(SplashScreenActivity.this,RegisterActivity.class);
                    startActivity(splashScreen);
                }
            }
        };thread.start();

    }
}