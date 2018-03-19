package com.example.georgia.sps_localization;


public class CellFunctionTable {

    int id;
    String cell_name,mean,sd;

//Create constructors
    public void CellFunctionTable(){}

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
