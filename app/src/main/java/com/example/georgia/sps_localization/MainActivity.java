package com.example.georgia.sps_localization;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    //Declare variables
    DatabaseHandler myDbHandler;

    //Columns for the tables AccessPoints
    private static final String TABLE_NAME="AccessPoint";
    private static final String COLUMN_ID="_id";
    private static final String COLUMN_TIMESTAMP="timestamp";
    private static final String COLUMN_RSSI="rssi";

    private String TAG="com.example.georgia.sps_localization";
    public int i,j;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create database handler
        myDbHandler=new DatabaseHandler(this,null,null,1);

        String query ="CREATE TABLE "+ TABLE_NAME + "(" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," /*+ COLUMN_TIMESTAMP + " TEXT NULL,"*/;

        for(i=0;i<=255;i++){
            String query1;
            if (i==255){
                query1=query + COLUMN_RSSI + String.valueOf(i) + " TEXT NULL";
            }
            else{
                query1=query + COLUMN_RSSI + String.valueOf(i) + " TEXT NULL,";
            }
            Log.i(TAG,query1);
        }
        query=query + ");";
        Log.i(TAG,query);
    }
}
