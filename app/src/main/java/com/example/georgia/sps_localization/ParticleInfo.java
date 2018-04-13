package com.example.georgia.sps_localization;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ParticleInfo extends AppCompatActivity implements SensorEventListener,StepListener{

    public int NumOfSteps;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    TextView txt2,txt1;
    StepDetector simpleStepDetector;
    double distancePerStep=0.0;
    String floor="0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particle_info);

        //Line to keep screen on permanently
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        txt1=(TextView) findViewById(R.id.floor);
        txt2=(TextView) findViewById(R.id.Steps);
        sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Nothing happens here
    }

    @Override
    public void step(long timeNs) {
        NumOfSteps++;
        txt2.setText("Steps: " +NumOfSteps);
    }

    // Method that gets called when we press the button Start Training
    /*******************************************************Registers SensorListener************************************************/
    public void StartTraining(View view){
        NumOfSteps=0;
        sensorManager.registerListener(ParticleInfo.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

    }

    // Method that gets called when we press the button Stop Training
    /*******************************************************Unregisters SensorListener************************************************/
    public void StopTraining(View view){
        sensorManager.unregisterListener(ParticleInfo.this);
        distancePerStep=18.2/NumOfSteps;
    }

    // Method that gets called when we press the button Start
    /*******************************************************Moves us to the particle filter activity************************************************/
    public void GoToParticles(View view){
        Intent i= new Intent(this,ParticleFilter.class);
        boolean check =true;
        floor=txt1.getText().toString();

        if(distancePerStep==0.0){
            Toast.makeText(this,"The step counter was not trained.",Toast.LENGTH_LONG).show();
            check=false;
        }
        if(!floor.equals("3") && !floor.equals("4")){
            Toast.makeText(this, "You did not give starting floor. Write either 3 or 4!", Toast.LENGTH_LONG).show();
            check=false;
        }
        distancePerStep = 150;
        if(check ){
            i.putExtra("distancePerStep", distancePerStep);
            i.putExtra("floor", floor);
            startActivity(i);
            finish();
        }
    }

}
