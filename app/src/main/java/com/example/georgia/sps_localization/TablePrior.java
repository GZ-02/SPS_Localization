package com.example.georgia.sps_localization;

/**
 * Created by Georgia on 17-Mar-18.
 */

public class TablePrior {
    String probability;
    int id;

    //Constructors
    public TablePrior(String prob){
        probability=prob;
    }

    public TablePrior(){

    }

    //Setter and Getter methods
    public int getId() {
        return id;
    }

    public String getProbability() {

        return probability;
    }

    public void setProbability(String probability) {
        this.probability = probability;
    }

    public void setId(int id) {
        this.id = id;
    }
}
