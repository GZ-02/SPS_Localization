package com.example.georgia.sps_localization;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class BayesFilter extends AppCompatActivity {

    DatabaseHandler myDb;
    TablePrior prior;
    int index,index2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bayes_filter);

        //Line to keep screen on permanently
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        prior.setProbability(Float.toString(1/19));
        myDb.addRow(prior);

        for (index=0;index<19;index++) {
            for (index2 = 0; index2 < 43; index2++) {

            }
        }

    }

    public void LocateMe(View view){

    }

}
