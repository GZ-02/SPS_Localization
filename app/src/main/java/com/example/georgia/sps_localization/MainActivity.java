package com.example.georgia.sps_localization;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    /********************************************Declaring Variables***************************************************/
    DatabaseHandler myDbHandler;
    private String TAG="com.example.georgia.sps_localization";
    public int i,j;

    /*************************************Function that creates the Main activity************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Line to keep screen on permanently
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Create database handler
        myDbHandler=new DatabaseHandler(this,null,null,1);
    }


    /******************************Method that gets called when we click Bayes button*********************/
    public void GoToBayes(View view){
        Intent intent =new Intent(this,BayesFilter.class);
        startActivity(intent);
    }


    /******************************Method that gets called when we click Particle button*********************/
    public void GoToParticle(View view){
        Intent intent =new Intent(this,ParticleInfo.class);
        startActivity(intent);
    }
}
