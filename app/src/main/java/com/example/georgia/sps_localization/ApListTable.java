package com.example.georgia.sps_localization;

/*************************************Table where we save a list of the access points we use for prediction*********************************************/

public class ApListTable {
    String AccessPoint;
    int id;

    /*******************************************Constructors********************************/
    public ApListTable(){}

    public ApListTable(String point){
       AccessPoint=point;
    }

    /*****************************************Setter and Getter methods**********************************/
    public String getAccessPoint() {
        return AccessPoint;
    }

    public int getId() {
        return id;
    }

    public void setAccessPoint(String accessPoint) {
        AccessPoint = accessPoint;
    }

    public void setId(int id) {
        this.id = id;
    }
}
