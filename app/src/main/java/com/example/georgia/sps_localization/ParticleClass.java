package com.example.georgia.sps_localization;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

public class ParticleClass extends ShapeDrawable  {
    int x;
    int y;
    int size;
    ShapeDrawable shape = new ShapeDrawable(new OvalShape());

    public ParticleClass() {

    }
}
