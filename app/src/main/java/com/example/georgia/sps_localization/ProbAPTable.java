package com.example.georgia.sps_localization;

/*************************************Table where we save the resulting probabilities per access point*********************************************/
public class ProbAPTable {
    int _id;
    String probability;

    /*******************************************Constructors********************************/
    public ProbAPTable(String prob){
        probability=prob;
    }

    public ProbAPTable(){

    }

    /*****************************************Setter and Getter methods**********************************/
    //Getter Methods
    public int get_id() {
        return _id;
    }

    public String getProbability() {
        return probability;
    }

    //Setter Methods
    public void set_id(int _id) {
        this._id = _id;
    }

    public void setProbability(String probability) {
        this.probability = probability;
    }
}

