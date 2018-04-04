package com.example.georgia.sps_localization;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class ParticleFilter extends AppCompatActivity implements SensorEventListener,StepListener{

    /********************************************Declaring Variables***************************************************/
    public String TAG="com.example.georgia.sps_localization";
    String floor;
    double distancePerStep;
    private SensorManager mySensorManager;
    private Sensor accelerometer,magnetometer;
    private SensorEventListener myListener1;
    private  int azimuth;
    public int NumberOfSteps=0;
    public String direction;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private StepDetector simpleStepDetector;
    double distanceTraveled=0.0;
    public boolean exitLoop=false;

    //WIL REMOVE
    TextView txt_compass,txtSteps;
    ImageView compass_img;

    /*****************************************Function that creates the Particle activity*********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particle_filter);

        //Line to keep screen on permanently
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i(TAG,"Entered Particle filters");

        //Get values from previous activity
        Bundle choicesData = getIntent().getExtras();
        floor=choicesData.getString("floor");
        distancePerStep=choicesData.getDouble("distancePerStep");
        Log.i(TAG,floor+" "+String.valueOf(distancePerStep));

        //Assign values to the variables
        mySensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        /*********************************WILL REMOVE, ADDED FOR TESTING PURPOSES****************************/
        compass_img = (ImageView) findViewById(R.id.img_compass);
        txt_compass = (TextView) findViewById(R.id.txt_azimuth);
        txtSteps=(TextView) findViewById(R.id.steps);
        /*************************************************************************************************/

        //Register Listeners
        StartListeners();

        //Thread with which we start listeners for compass
        Runnable r=new Runnable(){
            @Override
            public void run(){
                Log.i(TAG,"Started thread");
                long present=System.currentTimeMillis();
                long future=present+60*60*1000;
                long check=present+1000;
                //Start loop that repeats itself every 10 seconds
                while(System.currentTimeMillis()<future){
                    if(System.currentTimeMillis()==check){
                        check+=1000;
                        collectDirectionData();
                    }
                    if(exitLoop){break;}
                }
                Log.i(TAG,"Exited Loop");
            }
        };
        Thread myThread= new Thread(r);
        myThread.start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //NOTHING HAPPENS HERE
    }

    @Override
    public void step(long timeNs) {
        NumberOfSteps++;
        txtSteps.setText("Steps: "+NumberOfSteps);
        distanceTraveled=NumberOfSteps*distancePerStep;
        txt_compass.setText(azimuth + "° " + direction);
        Log.i(TAG,"Steps: "+String.valueOf(NumberOfSteps)+", Distance: "+String.valueOf(distanceTraveled)+" "+String.valueOf(azimuth)+", Direction: "+String.valueOf(direction));

    }


    @Override
    protected void onPause() {
        super.onPause();
        StopListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StartListeners();
    }

    @Override
    protected void onStop() {
        StopListeners();
        mySensorManager.unregisterListener(myListener1);
        exitLoop=true;
        mySensorManager=null;
        super.onStop();
    }

    //Method that registers listeners needed for motion model
    public void StartListeners(){
        mySensorManager.registerListener(ParticleFilter.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    //Method that unregisters all listeners
    public void StopListeners(){
        mySensorManager.unregisterListener(ParticleFilter.this);
    }

    /**************************************************Method that creates listener**************************************/
    public void collectDirectionData(){
        myListener1=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                    mLastAccelerometerSet = true;
                }
                else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                    mLastMagnetometerSet = true;
                }
                if (mLastAccelerometerSet && mLastMagnetometerSet) {
                    SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
                    SensorManager.getOrientation(rMat, orientation);
                    azimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
                }
                azimuth=Math.round(azimuth);
                compass_img.setRotation(-azimuth);
                direction=" " ;

                if (azimuth >= 320 && azimuth <= 360)
                    direction = "N";
                if (azimuth >=0  && azimuth <= 40)
                    direction = "N";
                if (azimuth >= 230 && azimuth <=310)
                    direction = "W";
                if (azimuth >= 140 && azimuth <= 220)
                    direction = "S";
                if (azimuth >= 50 && azimuth <=130)
                    direction = "E";

           //     Log.i(TAG,direction+ " "+String.valueOf(azimuth));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        mySensorManager.registerListener(myListener1,accelerometer,SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(myListener1,magnetometer,SensorManager.SENSOR_DELAY_UI);
    }

}
