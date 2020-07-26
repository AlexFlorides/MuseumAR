package com.example.museumar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class NoConnection extends AppCompatActivity {

    Timer mytimer;

    // keep checking if network connection recovers after 1 second
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noconnection);

        mytimer = new Timer();
        mytimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isNetworkAvailable()){
                    mytimer.cancel();
                    startActivity(new Intent(NoConnection.this, Login.class));
                }
            }
        }, 0, 1000);//put here time 1000 milliseconds=1 second
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // method checking if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
