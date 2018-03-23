package com.example.georgia.sps_localization;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class ParticleFilter extends AppCompatActivity {

    /********************************************Declaring Variables***************************************************/
    public String TAG="com.example.georgia.sps_localization";


    /*****************************************Function that creates the Particle activity*********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particle_filter);
        Log.i(TAG,"Entered Particle filters");

    }
}
