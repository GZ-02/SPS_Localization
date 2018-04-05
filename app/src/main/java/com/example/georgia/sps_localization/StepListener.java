package com.example.georgia.sps_localization;

// New listener dedicated to detecting when a step was taken
public interface StepListener {

    public void step(long timeNs);

}