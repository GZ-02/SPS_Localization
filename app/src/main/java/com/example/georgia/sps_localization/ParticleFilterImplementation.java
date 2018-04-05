package com.example.georgia.sps_localization;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class ParticleFilterImplementation extends AppCompatActivity {

    /********************************************Declaring Variables***************************************************/
    public String TAG="com.example.georgia.sps_localization";
    private int floor;
    private int screen_height;
    private int screen_width;
    private Canvas canvas;

    //Assumption always landscape
    private int floorWidthInCm = 14400;
    private int floorHeightInCm = 26000;
    private float normalizing_constant_width;
    private float normalizing_constant_height;
    private int particleSize = 2;
    private int particle_number = 500;
    List<ShapeDrawable> walls;
    List<ShapeDrawable> banned;
    List<ParticleClass> particles = new ArrayList<>();
    public static final String FLOOR = "floor";
    private Button move;

    /*****************************************Function that creates the Particle activity*********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particle_filter);
        Log.i(TAG,"Entered Particle filters");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_height = size.y;
        screen_width = size.x;
        normalizing_constant_width = screen_width/floorWidthInCm;
        normalizing_constant_height = screen_height/floorHeightInCm;

        Bundle bundle = getIntent().getExtras();
        floor = bundle.getInt(FLOOR);
        ImageView playground = (ImageView) findViewById(R.id.floorPlan);
        Bitmap blankBitmap = Bitmap.createBitmap(screen_width,screen_height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        playground.setImageBitmap(blankBitmap);
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


        //add particles
    }


    public class redrawParticles extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
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
            return "tada!";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {}

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
        try {
            String result = new redrawParticles().execute("").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }


    public List getWallsForFloor4(int width, int height) {
        List walls = new ArrayList<>();
        // distance between top right of cell 1 and door
        int wallUntilCell1Door = 1000;
        // door width at cell 1
        int cell1DoorWidth = 1500;

        // distance from top right corner of cell 5 (staircase) to door
        int wallUntilCell5Door = 10000;
        // door width at cell 5
        int cell5DoorWidth = 1500;

        // distance of wall between doors of cell 5 and cell 7
        int wallBetween5and7 = 2000;

        // door width at cell 7
        int cell7DoorWidth = 1500;

        // door width at cell 9
        int cell9DoorWidth = 1500;

        // distance between bottom left office's top horizontal wall and cell 9 door
        int cell9Distance = 4000;

        // distance between top left corner of cell 16 and door
        int cell16DoorFromTop = 350;
        int cell16DoorWidth = 1500;

        /*
         * Define walls of floor 4
         */

        //outlines
        walls.add(functionDimensionsToWall(0,0,14400,true));
        walls.add(functionDimensionsToWall(0,0,26000,false));
        walls.add(functionDimensionsToWall(14400,0,26000,false));
        walls.add(functionDimensionsToWall(0,26000,14400,true));

        // Island Office Part 1 - Cell 1
        walls.add(functionDimensionsToWall(7500,0,wallUntilCell1Door,false));
        walls.add(functionDimensionsToWall(7500,wallUntilCell1Door+cell1DoorWidth,8000-wallUntilCell1Door-cell1DoorWidth,false));
        walls.add(functionDimensionsToWall(9200,0,4400,false));
        walls.add(functionDimensionsToWall(7500,4400,5600,true));
        walls.add(functionDimensionsToWall(13100,0,4600,false));
        walls.add(functionDimensionsToWall(7500,5800,4200,true));
        walls.add(functionDimensionsToWall(11000,4400,1400,false));
        walls.add(functionDimensionsToWall(7500,8000,4200,true));
        walls.add(functionDimensionsToWall(11700,5800,2375,false));
        // Island Office Part 2
        walls.add(functionDimensionsToWall(7500,10100,4200,true));

        // Staircase - Cell 16
        walls.add(functionDimensionsToWall(7500,10100,cell16DoorFromTop,false));
        walls.add(functionDimensionsToWall(7500,10100+cell16DoorFromTop+cell16DoorWidth,8200-(cell16DoorFromTop+cell16DoorWidth),false));
        walls.add(functionDimensionsToWall(7500,18200,5600,true));
        walls.add(functionDimensionsToWall(7500,13800,5600,true));
        walls.add(functionDimensionsToWall(13100,13800,4600,false));
        walls.add(functionDimensionsToWall(11700,10100,3700,false));
        walls.add(functionDimensionsToWall(7500,12400,4200,true));

        // long vertical corridor left wall
        walls.add(functionDimensionsToWall(5200,0,wallUntilCell5Door,false));
        walls.add(functionDimensionsToWall(5200,cell5DoorWidth+wallUntilCell5Door,wallBetween5and7,false));
        walls.add(functionDimensionsToWall(5200,cell5DoorWidth+wallUntilCell5Door+wallBetween5and7+cell7DoorWidth,18500-(cell7DoorWidth+wallUntilCell5Door+wallBetween5and7+cell7DoorWidth),false));

        // rooms left side off left corridor wall
        walls.add(functionDimensionsToWall(0,9200,5200,true));
        walls.add(functionDimensionsToWall(0,12600,5200,true));
        walls.add(functionDimensionsToWall(0,16000,5200,true));
        walls.add(functionDimensionsToWall(0,18300,5200,true));
        walls.add(functionDimensionsToWall(2100,18300,3400,false));

        // bottom offices
        walls.add(functionDimensionsToWall(0,21700,cell9Distance,true));
        walls.add(functionDimensionsToWall(cell9Distance+cell9DoorWidth,21700,(9400-cell9Distance-cell9DoorWidth),true));
        walls.add(functionDimensionsToWall(3600,21700,4300,false));
        walls.add(functionDimensionsToWall(5900,21700,4300,false));
        walls.add(functionDimensionsToWall(9400,19500,6500,false));
        walls.add(functionDimensionsToWall(9400,19500,5000,true));


        return walls;

    }

    private ShapeDrawable functionDimensionsToWall(int leftD, int topD, int sizeInCm, boolean isHorizontal){
        ShapeDrawable d = new ShapeDrawable(new RectShape());

        //correct left and top for line thinkness
        int cmFromLeftPixelWallThinknessCorrection = (leftD/this.floorWidthInCm)*5;
        int cmFromTopPixelWallThinknessCorrection = (topD/this.floorHeightInCm)*5;

        int left = (int)(((double)leftD/(double)this.floorWidthInCm) * (double)this.screen_width - (double)cmFromLeftPixelWallThinknessCorrection);
        int top = (int) (((double)topD/(double)this.floorHeightInCm) * (double)this.screen_height - (double)cmFromTopPixelWallThinknessCorrection);
        int right = (int) ((isHorizontal) ? (((double)leftD+sizeInCm)/(double)this.floorWidthInCm) * (double)this.screen_width : (((double)leftD/(double)this.floorWidthInCm) * (double)this.screen_width + 10.0));
        int bottom = (int)((!isHorizontal) ? (((double)topD+sizeInCm)/(double)this.floorHeightInCm) * (double)this.screen_height : (((double)topD/(double)this.floorHeightInCm) * (double)this.screen_height + 10.0));

        // Log.d(TAG, "partial:" + partial);
        //Log.d(TAG, "left:" + ((left/this.floorWidthInCm) * this.screenWidth));
        // Log.d(TAG, "Pixel draw left:" + left + " top:" + (top/this.floorHeightInCm) * this.screenHeight + "right: " + ((isHorizontal) ? ((left+sizeInCm)/this.floorWidthInCm) * this.screenWidth : ((left/this.floorWidthInCm) * this.screenWidth + 20)) + " bottom:"+ ((!isHorizontal) ? ((top+sizeInCm)/this.floorHeightInCm) * this.screenHeight : ((top/this.floorHeightInCm) * this.screenHeight + 10)));
        d.setBounds(
                left,
                top,
                right,
                bottom);


        return d;
    }

    public List getWallsForFloor3(int width, int height) {
        //initialize walls
        List walls = new ArrayList<>();
        //outlines
        walls.add(functionDimensionsToWall(0,0,14400,true));
        walls.add(functionDimensionsToWall(0,0,26000,false));
        walls.add(functionDimensionsToWall(14400,0,26000,false));
        walls.add(functionDimensionsToWall(0,26000,14400,true));

        //island office parts 1
        walls.add(functionDimensionsToWall(7500,0,6900,true));
        walls.add(functionDimensionsToWall(7500,1800,3300,true));
       // walls.add(functionDimensionsToWall(7500,0,cell18DoorFromTop,false));
        walls.add(functionDimensionsToWall(7500,1800,6200,false));

        walls.add(functionDimensionsToWall(10800,0,4400,false));
        walls.add(functionDimensionsToWall(7500,4400,6900,true));
        walls.add(functionDimensionsToWall(7500,5800,4200,true));
        walls.add(functionDimensionsToWall(9300,4400,1400,false));
        walls.add(functionDimensionsToWall(7500,8000,4200,true));
        walls.add(functionDimensionsToWall(11700,5800,2300,false));


        walls.add(functionDimensionsToWall(7500,10100,4200,true));

        walls.add(functionDimensionsToWall(7500,12400,5800,false));
        walls.add(functionDimensionsToWall(7500,18200,5600,true));
        walls.add(functionDimensionsToWall(7500,13800,5600,true));
        walls.add(functionDimensionsToWall(13100,13800,4500,false));
        walls.add(functionDimensionsToWall(11700,10100,3700,false));
        walls.add(functionDimensionsToWall(7500,12400,4200,true));

        walls.add(functionDimensionsToWall(5200,0,18450,false));

        walls.add(functionDimensionsToWall(0,9200,5200,true));
        walls.add(functionDimensionsToWall(0,12600,5200,true));
        walls.add(functionDimensionsToWall(0,16000,5200,true));
        walls.add(functionDimensionsToWall(0,18300,5200,true));
        walls.add(functionDimensionsToWall(2100,18300,3400,false));

        //bottom offices
        walls.add(functionDimensionsToWall(0,21700,9400,true));
        walls.add(functionDimensionsToWall(9400,21800,4300,false));

        walls.add(functionDimensionsToWall(9400,19500,5000,true));

        return walls;

    }

    public List getRestrictedAreas4(int width, int height){

        // remove top portion, subtract top from all top_input values
        int top = 7800;

        List restricted_areas = new ArrayList<>();

        //rooms below area 2 & 3 (as seen on the official floorplan)
        restricted_areas.add(getResrictedSection(0,0,5200,9200));

        //room to the right of 7 (as seen on the official floorplan)
        restricted_areas.add(getResrictedSection(0,15850,5200,18400));

        //room below 8  (as seen on the official floorplan)
        restricted_areas.add(getResrictedSection(0,18400,2100,21500));

        //floor below 9 (as seen on the official floorplan)
        restricted_areas.add(getResrictedSection(0,21500,3600,26000));

        //floor above 9 (as seen on the official floorplan)
        restricted_areas.add(getResrictedSection(5900,21500,9400,26000));

        //island between 16, 6, 11, 14 and 15
        restricted_areas.add(getResrictedSection(7500,13600,13100,26000-top));

        restricted_areas.add(getResrictedSection(9200,7800-top,13100,12200-top));
        restricted_areas.add(getResrictedSection(7500,12200-top,11200,13600-top));
        restricted_areas.add(getResrictedSection(7500,13600-top,11700,15800-top));

        restricted_areas.add(getResrictedSection(9400,19300,14400,33800-top));

        restricted_areas.add(getResrictedSection(0,33800-top,14400,33800));

        return restricted_areas;
    }


    public List getRestrictedAreas3(int width, int height){
        List restricted_areas = new ArrayList<>();

        restricted_areas.add(getResrictedSection(0,0,5200,18200));

        restricted_areas.add(getResrictedSection(0,18200,2100,18200+3400));

        restricted_areas.add(getResrictedSection(0,21500,9400,26000));

        // island part 2

        restricted_areas.add(getResrictedSection(7500,13700,13100,18200));
        restricted_areas.add(getResrictedSection(7500,12300,11700,13800));

        // island part 1
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
                normalize(left_input),
                normalize(top_input),
                normalize(right_input),
                normalize(bottom_input));
        return d;
    }

    private int normalize(int input) {
        return (int) (((double)input/(double)this.floorWidthInCm) * (double)this.screen_width);
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
//        return ((holder.left < X-2*particleSize && holder.top < Y-2*particleSize &&
//                holder.right > X+2*particleSize  &&
//                holder.bottom > Y+2*particleSize));
    }

    public boolean isCollision(ShapeDrawable restricted,Rect particle){
        Rect holder = new Rect(restricted.getBounds());
        return holder.intersect(particle.left-particleSize,particle.top-particleSize,particle.right+particleSize,particle.bottom+particleSize);
    }
}
