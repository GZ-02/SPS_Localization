package com.example.georgia.sps_localization;

/********************************Table where we save info needed for the prediction, mean and standard deviations********************************/

public class CellFunctionTable {
    int id;
    String cell_name,mean,sd;

    /*******************************************Constructors********************************/
    public void CellFunctionTable(){}

    /*****************************************Setter and Getter methods**********************************/
    // Getter methods
    public int getId() {
        return id;
    }

    public String getCell_name() {
        return cell_name;
    }

    public String getMean() {
        return mean;
    }

    public String getSd() {
        return sd;
    }


    //Setter Methods
    public void setId(int id) {
        this.id = id;
    }

    public void setCell_name(String cell_name) {
        this.cell_name = cell_name;
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public void setSd(String sd) {
        this.sd = sd;
    }
}
