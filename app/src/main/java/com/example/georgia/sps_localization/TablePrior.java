package com.example.georgia.sps_localization;


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
