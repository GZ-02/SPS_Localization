package com.example.georgia.sps_localization;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import static android.content.Context.SENSOR_SERVICE;

public class Compass extends ParticleFilter implements SensorEventListener {
    private static String TAG = "Compass";

    private Context context;
    private SensorManager mSensorManager;

    private float currentDegree = 0f;

    //direction buttons if not on home


    public String heading;

    /**
     * Initialize the compass
     * @param c context
     */
    public Compass(Context c, SensorManager mSensorManager){
        context = c;

        //create sensor manager
        this.mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if(hasCorrectSensors()){
            Log.d("Compass", "it does have correct sensors");
            this.mSensorManager.registerListener(this, this.mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        }
    }

    public boolean hasCorrectSensors(){
        return ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) && (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(Math.abs(currentDegree - event.values[0]) > 2){
            String direction;

            if(currentDegree - event.values[0] > 0){
                direction = " moving counter clockwise";
            }
            else{
                direction = " moving clockwise";
            }
            currentDegree = event.values[0];

        }
        else{
            currentDegree = event.values[0];
        }

        float finalAngle = ((event.values[0])) - 150.f;
            //and calculate the heading
        computeHeading(Math.round(finalAngle));

    }

    public void computeHeading(int finalAngle){


        //get the value between 0 and 360
        while(finalAngle < 0){
            finalAngle = finalAngle + 360;
        }
        finalAngle = finalAngle % 360;


        if(finalAngle > 45 && finalAngle < 135){
            heading = "right";
        }
        else if(finalAngle >= 135 && finalAngle < 215){
            heading = "down";
        }
        else if(finalAngle >= 215 && finalAngle < 315){
            heading = "left";
        }
        else{
            heading = "up";
        }

        ParticleFilter.heading = heading;
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
