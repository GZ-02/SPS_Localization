package com.example.georgia.sps_localization;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

public class ParticleClass extends ShapeDrawable  {
    int x;
    int y;
    int size;
    int clusterIndex=0;
    int distance=26000;
    ShapeDrawable shape = new ShapeDrawable(new OvalShape());

    public ParticleClass() {

    }
    public ParticleClass deepCopy(int particleSize) {
        ParticleClass particle = new ParticleClass();
        particle.x = this.x;
        particle.y = this.y;
        particle.size = this.size;
        particle.distance=this.distance;
        particle.shape = this.shape;
        particle.shape.getPaint().setColor(Color.CYAN);
        particle.shape.setBounds(particle.x - particleSize, particle.y - particleSize, particle.x + particleSize, particle.y + particleSize);
        return particle;
    }

}
