package com.example.georgia.sps_localization;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleFilter extends AppCompatActivity implements SensorEventListener,StepListener{

    /********************************************Declaring Variables***************************************************/
    public String TAG="com.example.georgia.sps_localization";
    double distancePerStep;
    private SensorManager mySensorManager;
    private Sensor accelerometer,magnetometer;
    private SensorEventListener myListener1;
    private  int azimuth;
    public int NumberOfSteps=0;
    public String direction;
    public int directionInt;
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
   // TextView txt_compass,txtSteps;
    //ImageView compass_img;
    public String result = "1";
    private int floor;
    private int screen_height;
    private int screen_width;
    private Canvas canvas;

    //Assumption always landscape
    private int floorWidthInCm = 14400;
    private int floorHeightInCm = 26000;
    private float normalizing_constant_width;
    private float normalizing_constant_height;
    private int particleSize = 10;
    private int particle_number = 5;
    List<ShapeDrawable> walls;
    List<ShapeDrawable> banned;
    List<ParticleClass> particles = new ArrayList<>();
    public static final String FLOOR = "floor";
    private Button move;
    private ImageView playground;

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
        floor=Integer.parseInt(choicesData.getString(FLOOR));
        Log.i(TAG,floor+" "+String.valueOf(floor));

        distancePerStep=choicesData.getDouble("distancePerStep");
        Log.i(TAG,floor+" "+String.valueOf(distancePerStep));

        //Assign values to the variables
        mySensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        /*********************************WILL REMOVE, ADDED FOR TESTING PURPOSES****************************/
        /*compass_img = (ImageView) findViewById(R.id.img_compass);
        txt_compass = (TextView) findViewById(R.id.txt_azimuth);
        txtSteps=(TextView) findViewById(R.id.steps);*/
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


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_height = size.y;
        screen_width = size.x;

        playground = (ImageView) findViewById(R.id.floorPlan);
        Bitmap blankBitmap = Bitmap.createBitmap(screen_width,screen_height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        playground.setImageBitmap(blankBitmap);
       // playground.performClick();
        move = (Button) findViewById(R.id.move);



        if(floor == 4) {
            walls = getWallsForFloor4(screen_width, screen_height);
            for(ShapeDrawable wall : walls) {
                wall.draw(canvas);
            }
            banned = getRestrictedAreas4(screen_width, screen_height);
            for(ShapeDrawable ban : banned) {
                //ban.draw(canvas);
            }
        } else {
            // update to 3
            walls = getWallsForFloor3(screen_width, screen_height);
            for(ShapeDrawable wall : walls) {
                wall.draw(canvas);
            }
            banned = getRestrictedAreas3(screen_width, screen_height);
            for(ShapeDrawable ban : banned) {
                // ban.draw(canvas);
            }
        }
        for (int i=0;i<particle_number;i++) {
            particles.add(createNewParticle());
            particles.get(particles.size()-1).shape.draw(canvas);
        }
        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //refresh the information
                updateParticlePosition(350,90);
            }
        });

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
        //txtSteps.setText("Steps: "+NumberOfSteps);
        distanceTraveled=NumberOfSteps*distancePerStep;
       // txt_compass.setText(azimuth + "° " + direction);

        Log.i(TAG,"Steps: "+String.valueOf(NumberOfSteps)+", Distance: "+String.valueOf(distanceTraveled)+" "+String.valueOf(azimuth)+", Direction: "+String.valueOf(direction));
        /*if ( Integer.parseInt(result)==1) {
            result = "0";
            Log.i(TAG, "calling our update");
            updateParticlePosition((int) 500, 90);
        } else {
            Log.i(TAG, "Wait for result to update");
        }*/
        move.performClick();
        /*try {
            runOnUiThread(new Runnable() {
                public void run() {
                    canvas.drawColor(Color.WHITE);
                    for(ShapeDrawable wall : walls) {
                        wall.draw(canvas);
                    }

                    for(ShapeDrawable ban : banned) {
                        ban.draw(canvas);
                    }
                    for(ParticleClass particle : particles){
                        redraw(particle);
                    }
                    Toast.makeText(getBaseContext(),"You might be in the wrong building",Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (Throwable t)
        {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getBaseContext(),"URL exeption!",Toast.LENGTH_SHORT).show();
                }
            });
        }*/
        /*synchronized (this) {
            onPause();
            move.performClick();
            onResume();
        }*/
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
//                compass_img.setRotation(-azimuth);
                direction=" " ;

                if (azimuth >= 320 && azimuth <= 360){
                    directionInt=0;
                    direction="E";
                }
                if (azimuth >=0  && azimuth <= 40){
                    directionInt=0;
                    direction="E";
                }
                if (azimuth >= 230 && azimuth <=310)
                {
                    directionInt=90;
                    direction="N";
                }
                if (azimuth >= 140 && azimuth <= 220){
                    directionInt=180;
                    direction="W";
                }
                if (azimuth >= 50 && azimuth <=130){
                    directionInt=270;
                    direction="S";
                }

                //     Log.i(TAG,direction+ " "+String.valueOf(azimuth));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        mySensorManager.registerListener(myListener1,accelerometer,SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(myListener1,magnetometer,SensorManager.SENSOR_DELAY_UI);
    }

    public synchronized void redrawParticlesSynchronized() {
        Toast.makeText(this,"in redraw particle",Toast.LENGTH_SHORT).show();

        canvas.drawColor(Color.WHITE);
        for(ShapeDrawable wall : walls) {
            wall.draw(canvas);
        }

        for(ShapeDrawable ban : banned) {
            //ban.draw(canvas);
        }
        for(ParticleClass particle : particles){
            redraw(particle);
        }
    }
    public void drawSomething(View view) {
        canvas.drawColor(Color.WHITE);
        for(ShapeDrawable wall : walls) {
            wall.draw(canvas);
        }

        for(ShapeDrawable ban : banned) {
            //ban.draw(canvas);
        }
        for(ParticleClass particle : particles){
            redraw(particle);
        }
        view.invalidate();
    }

    public class redrawParticles extends AsyncTask<Void, Void, Void> {

        public redrawParticles(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
//            Bitmap blankBitmap = Bitmap.createBitmap(screen_width,screen_height, Bitmap.Config.ARGB_8888);
//            canvas = new Canvas(blankBitmap);
//            playground.setImageBitmap(blankBitmap);
        }




        @Override
        protected Void doInBackground(Void... voids) {
            canvas.drawColor(Color.WHITE);
            for(ShapeDrawable wall : walls) {
                wall.draw(canvas);
            }

            for(ShapeDrawable ban : banned) {
                //ban.draw(canvas);
            }
            for(ParticleClass particle : particles){
                redraw(particle);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private void redraw(ParticleClass particleClass) {
        particleClass.shape.draw(canvas);
    }

    public void updateParticlePosition(int distance, double direction) {

        int newX = (int) Math.round(distance * Math.sin(Math.toRadians(direction)));
        int newY = (int) Math.round(distance * Math.cos(Math.toRadians(direction)));

        //normalizing
        newX = newX * screen_width / floorWidthInCm;
        newY = newY * screen_height / floorHeightInCm;
        int flag=0;
        for (int index = 0; index < particle_number; index++) {
            flag=0;
            ParticleClass particle = particles.get(index);
            Rect rect = formRectangle(particle.x,particle.y,particle.x+newX,particle.y+newY);

            for(ShapeDrawable wall : walls) {
                if ( isCollision(wall,rect)) {
                    particles.remove(index);
                    particles.add(createNewParticle());
                    flag=1;
                    break;
                }
            }
            if (flag==0) {
                particle.x = particle.x+newX;
                particle.y = particle.y+newY;
                particle.shape.getPaint().setColor(Color.RED);
                particle.shape.setBounds(particle.x - particleSize, particle.y - particleSize, particle.x + particleSize, particle.y + particleSize);
            }
        }
        playground.performClick();
        //redrawParticlesSynchronized();


    }


    public List getWallsForFloor4(int width, int height) {
        List walls = new ArrayList<>();
        // distance between top right of cell 1 and door
        // door width at cell 1

        // distance from top right corner of cell 5 (staircase) to door
        // door width at cell 5

        // distance of wall between doors of cell 5 and cell 7

        // door width at cell 7

        // door width at cell 9

        // distance between bottom left office's top horizontal wall and cell 9 door

        // distance between top left corner of cell 16 and door

        walls.add(getBoundsForWall(0,0,14400,true));
        walls.add(getBoundsForWall(0,0,26000,false));
        walls.add(getBoundsForWall(14400,0,26000,false));
        walls.add(getBoundsForWall(0,26000,14400,true));
        walls.add(getBoundsForWall(7500,0,1000,false));
        walls.add(getBoundsForWall(7500,2500,5500,false));
        walls.add(getBoundsForWall(9200,0,4400,false));
        walls.add(getBoundsForWall(7500,4400,5600,true));
        walls.add(getBoundsForWall(13100,0,4600,false));
        walls.add(getBoundsForWall(7500,5800,4200,true));
        walls.add(getBoundsForWall(11000,4400,1400,false));
        walls.add(getBoundsForWall(7500,8000,4200,true));
        walls.add(getBoundsForWall(11700,5800,2375,false));
        walls.add(getBoundsForWall(7500,10100,4200,true));
        walls.add(getBoundsForWall(7500,10100,350,false));
        walls.add(getBoundsForWall(7500,10100+350+1500,6350,false));
        walls.add(getBoundsForWall(7500,18200,5600,true));
        walls.add(getBoundsForWall(7500,13800,5600,true));
        walls.add(getBoundsForWall(13100,13800,4600,false));
        walls.add(getBoundsForWall(11700,10100,3700,false));
        walls.add(getBoundsForWall(7500,12400,4200,true));
        walls.add(getBoundsForWall(5200,0,10000,false));
        walls.add(getBoundsForWall(5200,11500,2000,false));
        walls.add(getBoundsForWall(5200,15000,3500,false));
        walls.add(getBoundsForWall(0,9200,5200,true));
        walls.add(getBoundsForWall(0,12600,5200,true));
        walls.add(getBoundsForWall(0,16000,5200,true));
        walls.add(getBoundsForWall(0,18300,5200,true));
        walls.add(getBoundsForWall(2100,18300,3400,false));
        walls.add(getBoundsForWall(0,21700,4000,true));
        walls.add(getBoundsForWall(5500,21700,150,true));
        walls.add(getBoundsForWall(3600,21700,4300,false));
        walls.add(getBoundsForWall(5900,21700,4300,false));
        walls.add(getBoundsForWall(9400,19500,6500,false));
        walls.add(getBoundsForWall(9400,19500,5000,true));


        return walls;

    }

    private ShapeDrawable getBoundsForWall(int leftD, int topD, int thickness, boolean isHorizontal){
        ShapeDrawable wall = new ShapeDrawable(new RectShape());

        int horizontalT = (leftD/this.floorWidthInCm)*5;
        int verticalT = (topD/this.floorHeightInCm)*5;
        int left = (int)(((double)leftD/(double)this.floorWidthInCm) * (double)this.screen_width - (double)horizontalT);
        int top = (int) (((double)topD/(double)this.floorHeightInCm) * (double)this.screen_height - (double)verticalT);
        //right and bottom depends on if vertical or horizontal line
        int right;
        int bottom;
        if ( isHorizontal) {
            right =  (int)((((double)leftD+thickness)/(double)this.floorWidthInCm) * (double)this.screen_width);
            bottom = (int)(((double)leftD/(double)this.floorWidthInCm) * (double)this.screen_width + 5);
        } else {
            right = (int) ((((double)topD+thickness)/(double)this.floorHeightInCm) * (double)this.screen_height);
            bottom = (int) (((double)topD/(double)this.floorHeightInCm) * (double)this.screen_height + 5));
        }
        wall.setBounds(left,top,right,bottom);
        return wall;
    }

    public List getWallsForFloor3(int width, int height) {
        List walls = new ArrayList<>();
        walls.add(getBoundsForWall(0,0,14400,true));
        walls.add(getBoundsForWall(0,0,26000,false));
        walls.add(getBoundsForWall(14400,0,26000,false));
        walls.add(getBoundsForWall(0,26000,14400,true));

        walls.add(getBoundsForWall(7500,0,6900,true));
        walls.add(getBoundsForWall(7500,1800,3300,true));
        // walls.add(getBoundsForWall(7500,0,cell18DoorFromTop,false));
        walls.add(getBoundsForWall(7500,1800,6200,false));

        walls.add(getBoundsForWall(10800,0,4400,false));
        walls.add(getBoundsForWall(7500,4400,6900,true));
        walls.add(getBoundsForWall(7500,5800,4200,true));
        walls.add(getBoundsForWall(9300,4400,1400,false));
        walls.add(getBoundsForWall(7500,8000,4200,true));
        walls.add(getBoundsForWall(11700,5800,2300,false));


        walls.add(getBoundsForWall(7500,10100,4200,true));

        walls.add(getBoundsForWall(7500,12400,5800,false));
        walls.add(getBoundsForWall(7500,18200,5600,true));
        walls.add(getBoundsForWall(7500,13800,5600,true));
        walls.add(getBoundsForWall(13100,13800,4500,false));
        walls.add(getBoundsForWall(11700,10100,3700,false));
        walls.add(getBoundsForWall(7500,12400,4200,true));

        walls.add(getBoundsForWall(5200,0,18450,false));

        walls.add(getBoundsForWall(0,9200,5200,true));
        walls.add(getBoundsForWall(0,12600,5200,true));
        walls.add(getBoundsForWall(0,16000,5200,true));
        walls.add(getBoundsForWall(0,18300,5200,true));
        walls.add(getBoundsForWall(2100,18300,3400,false));

        walls.add(getBoundsForWall(0,21700,9400,true));
        walls.add(getBoundsForWall(9400,21800,4300,false));

        walls.add(getBoundsForWall(9400,19500,5000,true));

        return walls;

    }

    public List getRestrictedAreas4(int width, int height){

        List restricted_areas = new ArrayList<>();

        restricted_areas.add(getResrictedSection(0,0,5200,9200));
        restricted_areas.add(getResrictedSection(0,15850,5200,18400));
        restricted_areas.add(getResrictedSection(0,18400,2100,21500));
        restricted_areas.add(getResrictedSection(0,21500,3600,26000));
        restricted_areas.add(getResrictedSection(5900,21500,9400,26000));
        restricted_areas.add(getResrictedSection(7500,13600,13100,18200));
        restricted_areas.add(getResrictedSection(9200,0,13100,4400));
        restricted_areas.add(getResrictedSection(7500,4400,11200,5800));
        restricted_areas.add(getResrictedSection(7500,5800,11700,8000));
        restricted_areas.add(getResrictedSection(9400,19300,14400,26000));
        restricted_areas.add(getResrictedSection(0,26000,14400,33800));

        return restricted_areas;
    }


    public List getRestrictedAreas3(int width, int height){
        List restricted_areas = new ArrayList<>();

        restricted_areas.add(getResrictedSection(0,0,5200,18200));
        restricted_areas.add(getResrictedSection(0,18200,2100,18200+3400));
        restricted_areas.add(getResrictedSection(0,21500,9400,26000));
        restricted_areas.add(getResrictedSection(7500,13700,13100,18200));
        restricted_areas.add(getResrictedSection(7500,12300,11700,13800));
        restricted_areas.add(getResrictedSection(10800,0,14400,4400));
        restricted_areas.add(getResrictedSection(7500,1800,10800,4400));
        restricted_areas.add(getResrictedSection(7500,4400,9300,5800));
        restricted_areas.add(getResrictedSection(7500,5800,11700,8000));
        return restricted_areas;
    }

    private ShapeDrawable getResrictedSection(int left_input, int top_input, int right_input, int bottom_input){
        ShapeDrawable d = new ShapeDrawable(new RectShape());
        d.getPaint().setColor(Color.BLACK);
        d.setBounds(
                normalizeWidth(left_input),
                normalizeHeight(top_input),
                normalizeWidth(right_input),
                normalizeHeight(bottom_input));
        return d;
    }

    private int normalizeWidth(int input) {
        return (int) (((double)input/(double)this.floorWidthInCm) * (double)this.screen_width);
    }

    private int normalizeHeight(int input) {
        return (int) (((double)input/(double)this.floorHeightInCm) * (double)this.screen_height);
    }

    public ParticleClass createNewParticle(){
        ParticleClass particleClass = new ParticleClass();
        int flag=0;
        do {
            flag=0;
            Random r = new Random();
            particleClass.x = r.nextInt(screen_width - particleSize / 2);
            particleClass.y = r.nextInt(screen_height - particleSize / 2);
            if(!isItAppropiate(particleClass.x,particleClass.y)) {
                flag=1;
            }
        } while (flag==1);
        particleClass.shape.getPaint().setColor(Color.CYAN);
        particleClass.shape.setBounds(particleClass.x - particleSize, particleClass.y - particleSize, particleClass.x + particleSize, particleClass.y + particleSize);
        return particleClass;
    }

    public boolean isItAppropiate(int X, int Y) {
        if ( checkWalls(X,Y) || checkRestrictedAreas(X,Y)) {
            return false;
        }
        return true;
    }
    public boolean checkWalls(int X, int Y) {
        for (ShapeDrawable wall : walls) {
            if (contains(wall, X, Y)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkRestrictedAreas(int X, int Y) {
        for (ShapeDrawable restricted : banned) {
            if (contains(restricted, X, Y)) {
                return true;
            }
        }
        return false;
    }

    public Rect formRectangle(int X, int Y,int newX,int newY) {
        int left = X<=newX?X:newX;
        int right = X>newX?X:newX;
        int top = Y<=newY?Y:newY;
        int bottom = Y>newY?Y:newY;

        Rect rect = new Rect();
        rect.set(
                left,
                top,
                right,
                bottom);
        return rect;
    }


    public boolean contains(ShapeDrawable restricted,int X, int Y){
        Rect holder = new Rect(restricted.getBounds());
        return holder.intersect(X-particleSize,Y-particleSize,X+particleSize,Y+particleSize);
    }

    public boolean isCollision(ShapeDrawable restricted,Rect particle){
        Rect holder = new Rect(restricted.getBounds());
        return holder.intersect(particle.left-particleSize,particle.top-particleSize,particle.right+particleSize,particle.bottom+particleSize);
    }




}