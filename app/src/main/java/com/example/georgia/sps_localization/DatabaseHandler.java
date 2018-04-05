package com.example.georgia.sps_localization;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*******************************************************Class that handles the Database **************************************************************/

public class DatabaseHandler extends SQLiteOpenHelper {

    //Initialize variables
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="LocalizationApp.db";
    public int i;
    private static final String TABLE_NAME="Prior";
    private String TABLE_NAME2="FunctionForAP";
    private String TABLE_NAME3="ProbabilityPerAccessPoint";
    private String TABLE_NAME4="OurAccessPoints";

    //Columns for the table Prior and ProbabilityPerAccessPoint
    private static final String COLUMN_ID="_id";
    private static final String COLUMN_PROBABILITY="probability";

    //Columns for table OurAccessPoints
    private static final String COLUMN_AP="Point";

    //Columns for table CellFunction
    private static final String COLUMN_CELL="cell";
    private static  final String COLUMN_MEAN="mean";
    private static final String COLUMN_STANDARD="sd";

    private String TAG="com.example.georgia.sps_localization";

    //Constructor
    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    //Override methods
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query2,query3,query4;
        // Query for Prior table
        String query ="CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CELL + " TEXT_NULL," +COLUMN_PROBABILITY + " TEXT NULL" + ");";
        Log.i(TAG,query);
        db.execSQL(query);

        //Query for  43 FunctionForAp and ProbabilityPerAccessPoint tables
        //Rssi values range from -33 to -92
        String q="CREATE TABLE " + TABLE_NAME2;
        String q1="CREATE TABLE " + TABLE_NAME3;
        for (i=1; i<=43; i++){
            Log.i(TAG,Integer.toString(i));
            query2=q + Integer.toString(i);
            query3=q1 + Integer.toString(i);
            query2=query2 +"("+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CELL + " TEXT NULL,"+
            COLUMN_MEAN + " TEXT NULL," + COLUMN_STANDARD + " TEXT NULL"+ ");";
            query3 =query3 + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_PROBABILITY + " TEXT NULL" + ");";
            Log.i(TAG,query2);
            db.execSQL(query2);
            Log.i(TAG,query3);
            db.execSQL(query3);
            query2="null";
            query3="null";
        }

        //Query for table to save the access points
        query4 ="CREATE TABLE " + TABLE_NAME4 + "(" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_AP + " TEXT NULL" + ");";
        Log.i(TAG,query4);
        db.execSQL(query4);
    }

    @Override
    //Upgrade the tables
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int ind;
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS" +TABLE_NAME4);
        for(ind=1;ind<=43;ind++) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2+String.valueOf(i));
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME+String.valueOf(i));
            onCreate(db);
        }
    }

    //Methods
    //Add a new row to the table Prior
    public void addRow(TablePrior tbl){
        SQLiteDatabase db=this.getWritableDatabase();
        // Use ContentValues to add a row in the table
        ContentValues values = new ContentValues();

        //Add values for each column
        values.put(COLUMN_PROBABILITY,tbl.getProbability());
        long g=db.insert(TABLE_NAME, null, values);
        Log.i(TAG,String.valueOf(g));

        if(g!=-1){
            Log.i(TAG,"Row added");
        }
        else{
            Log.i(TAG,"Row not added");
        }
        db.close();
    }


    //Method that updates a specific row of table Prior
    public void update1(int index,String newPr){
        SQLiteDatabase db=this.getWritableDatabase();
        // Use ContentValues to add a row in the table
        ContentValues values = new ContentValues();
        //Add values for each column
        values.put(COLUMN_PROBABILITY,newPr);
        long g=db.update(TABLE_NAME,values,"_id="+String.valueOf(index),null);
        if(g!=-1){
            Log.i(TAG,"Row updated");
        }
        else{
            Log.i(TAG,"Row not updated");
        }
        db.close();
    }


    //Method that returns the prior probability for specific cell
    public String returnPriorProb(int r){
        String probability="-1";
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT probability FROM " + TABLE_NAME+" WHERE _id="+String.valueOf(r);
        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()){
            //  Log.i(TAG,"Nothing wrong");
        }
        else{
            Log.i(TAG,"Something wrong");
            return probability;
        }
        probability=c.getString(c.getColumnIndex(COLUMN_PROBABILITY));
        c.close();
        return probability;
    }


    //Add a new row to one of the tables CellFunction
    public void addRow2(CellFunctionTable cft,int i){
        TABLE_NAME2+=Integer.toString(i);
        Log.i(TAG,TABLE_NAME2);
        SQLiteDatabase db=this.getWritableDatabase();
        // Use ContentValues to add a row in the table
        ContentValues values = new ContentValues();

        //Add values for each column
        values.put(COLUMN_CELL,cft.getCell_name());
        values.put(COLUMN_MEAN,cft.getMean());
        values.put(COLUMN_STANDARD,cft.getSd());
        long g=db.insert(TABLE_NAME2, null, values);
        Log.i(TAG,String.valueOf(g));
        if(g!=-1){
            Log.i(TAG,"Row added");
        }
        else{
            Log.i(TAG,"Row not added");
            Log.i(TAG,"WHAT WAS NOT ADDED"+  cft.getCell_name()+ " "+cft.getMean() +" " +cft.getSd() );
        }
        db.close();
        TABLE_NAME2="FunctionForAP";
    }

    //Add a new row to the tables ProbAPTable
    public void addRow3(ProbAPTable pt,int i){
        TABLE_NAME3+=Integer.toString(i);
        Log.i(TAG,TABLE_NAME3);
        SQLiteDatabase db=this.getWritableDatabase();
        // Use ContentValues to add a row in the table
        ContentValues values = new ContentValues();
        //Add values for each column
        values.put(COLUMN_PROBABILITY,pt.getProbability());
        long g=db.insert(TABLE_NAME3, null, values);
        Log.i(TAG,String.valueOf(g));
        if(g!=-1){
            Log.i(TAG,"Row added");
        }
        else{
            Log.i(TAG,"Row not added");
        }
        db.close();
        TABLE_NAME3="ProbabilityPerAccessPoint";
    }

    //Method that updates a specific row from one of the ProbAPTables
    public void update3(ProbAPTable pt,int index,int r){
        TABLE_NAME3+=Integer.toString(index);
        Log.i(TAG,TABLE_NAME3);
        SQLiteDatabase db=this.getWritableDatabase();
        // Use ContentValues to add a row in the table
        ContentValues values = new ContentValues();
        //Add values for each column
        values.put(COLUMN_PROBABILITY,pt.getProbability());
        long g=db.update(TABLE_NAME3,values,"_id="+String.valueOf(r),null);
        if(g!=-1){
            Log.i(TAG,"Row updated");
        }
        else{
            Log.i(TAG,"Row not updated");
        }
        db.close();
        TABLE_NAME3="ProbabilityPerAccessPoint";
    }

    //Method that returns the probability for specific cell and access point
    public String returnProb(int index,int r){
        String probability="-1";

        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT probability FROM " + TABLE_NAME3+String.valueOf(index)+" WHERE _id="+String.valueOf(r);
        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()){
          //  Log.i(TAG,"Nothing wrong");
        }
        else{
            Log.i(TAG,"Something wrong");
            return probability;
        }
        probability=c.getString(c.getColumnIndex(COLUMN_PROBABILITY));
        c.close();
        return probability;
    }


    //Add a new row to the table OurAccessPoints
    public void addRow4(ApListTable tbl){
        SQLiteDatabase db=this.getWritableDatabase();
        // Use ContentValues to add a row in the table
        ContentValues values = new ContentValues();
        //Add values for each column
        values.put(COLUMN_AP,tbl.getAccessPoint());
        long g=db.insert(TABLE_NAME4, null, values);
        Log.i(TAG,String.valueOf(g));

        if(g!=-1){
            Log.i(TAG,"Row added");
        }
        else{
            Log.i(TAG,"Row not added");
        }
        db.close();
    }



    //Delete all rows from table Prior
    public void deleteAll1(){
        SQLiteDatabase db=getWritableDatabase();
        String query="DELETE FROM "+ TABLE_NAME;
        db.execSQL(query);
    }

    //Delete all rows from table CellFunctionTable
    public void deleteAll2(int index){
        SQLiteDatabase db=getWritableDatabase();
        String query="DELETE FROM "+ TABLE_NAME2 +Integer.toString(index);
        db.execSQL(query);
    }

    //Delete all rows from table CellFunctionTable
    public void deleteAll3(int index){
        SQLiteDatabase db=getWritableDatabase();
        String query="DELETE FROM "+ TABLE_NAME3 +Integer.toString(index);
        db.execSQL(query);
    }

    //Delete all rows from table OurAccessPoints
    public void deleteAll4(){
        SQLiteDatabase db=getWritableDatabase();
        String query="DELETE FROM "+ TABLE_NAME4;
        db.execSQL(query);
    }


    //Method that prints the contents of Prior table
    public String DatabaseToString1(){
        int sa;
        String dbString="";
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;

        //Cursor that points to a row of the table
        Cursor c = db.rawQuery(query, null);

        //Move to first row
        if(c.moveToFirst()){
            Log.i(TAG,"There are records in the table");
        }
        else{
            dbString="No records in the table!";
            return dbString;
        }
        do{
            sa=c.getInt(c.getColumnIndex(COLUMN_ID));
            dbString +=String.valueOf(sa);
            dbString+=", ";
            dbString+=c.getString(c.getColumnIndex(COLUMN_PROBABILITY));
            dbString+="\n";
        }while(c.moveToNext());
        c.close();
        return dbString;
    }

    //Method that prints the contents of one of the CellFunction tables
    public String DatabaseToString2(int index){
        int sa;
        String dbString="";
        TABLE_NAME2+=Integer.toString(index);
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME2;

        //Cursor that points to a row of the table
        Cursor c = db.rawQuery(query, null);

        //Move to first row
        if(c.moveToFirst()){
            Log.i(TAG,"There are records in the table");
        }
        else{
            dbString="No records in the table!";
            TABLE_NAME2="FunctionForAP";
            return dbString;
        }

        do{
            sa=c.getInt(c.getColumnIndex(COLUMN_ID));
            dbString +=String.valueOf(sa);
            dbString+=" , ";
            dbString+=c.getString(c.getColumnIndex(COLUMN_CELL));
            dbString+=" , ";
            dbString+=c.getString(c.getColumnIndex(COLUMN_MEAN));
            dbString+=" , ";
            dbString+=c.getString(c.getColumnIndex(COLUMN_STANDARD));
            dbString+="\n";
        }while(c.moveToNext());
        c.close();
        TABLE_NAME2="FunctionForAP";
        return dbString;
    }


    //Method that prints the contents of one of the ProbAP tables
    public String DatabaseToString3(int index){
        int sa;
        String dbString="";
        TABLE_NAME3+=Integer.toString(index);
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME3;

        //Cursor that points to a row of the table
        Cursor c = db.rawQuery(query, null);

        //Move to first row
        if(c.moveToFirst()){
            Log.i(TAG,"There are records in the table");
        }
        else{
            dbString="No records in the table!";
            TABLE_NAME3="ProbabilityPerAccessPoint";
            return dbString;
        }

        do{
            sa=c.getInt(c.getColumnIndex(COLUMN_ID));
            dbString +=String.valueOf(sa);
            dbString+=", ";
            dbString+=c.getString(c.getColumnIndex(COLUMN_PROBABILITY));
            dbString+="\n";
        }while(c.moveToNext());
        c.close();
        TABLE_NAME3="ProbabilityPerAccessPoint";
        return dbString;
    }

    //Method that prints the contents of ApList table
    public String DatabaseToString4(){
        int sa;
        String dbString="";
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME4;

        //Cursor that points to a row of the table
        Cursor c = db.rawQuery(query, null);

        //Move to first row
        if(c.moveToFirst()){
            Log.i(TAG,"There are records in the table");
        }
        else{
            dbString="No records in the table!";
            return dbString;
        }

        do{
            sa=c.getInt(c.getColumnIndex(COLUMN_ID));
            dbString +=String.valueOf(sa);
            dbString+=", ";
            dbString+=c.getString(c.getColumnIndex(COLUMN_AP));
            dbString+="\n";
        }while(c.moveToNext());
        c.close();
        return dbString;
    }


    //Method that returns the index assigned to a specific access point
    public int ReturnIndex(String mac){
        int number=0,sa;
        String dbString="";
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME4;

        //Cursor that points to a row of the table
        Cursor c = db.rawQuery(query, null);

        //Move to first row
        if(c.moveToFirst()){
            Log.i(TAG,"There are records in the table");
        }
        else{
            Log.i(TAG,"No records in the table!");
        }
        do{
            sa=c.getInt(c.getColumnIndex(COLUMN_ID));
            dbString=c.getString(c.getColumnIndex(COLUMN_AP));
            if(mac.equals(dbString)){
                number=sa;
                break;
            }
        }while(c.moveToNext());
        c.close();
        return number;
    }


    //Function used to perform Gaussian equation for APs read
    public String[] PerformGauss(int index,double x){
        String[] results=new String[19];
        int sa;
        double mean,sd,g,sqr,power;
        sqr=Math.sqrt(2*3.14);
        TABLE_NAME2+=Integer.toString(index);
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME2;

        //Cursor that points to a row of the table
        Cursor c = db.rawQuery(query, null);

        //Move to first row
        if(c.moveToFirst()){
            Log.i(TAG,"There are records in the table");
        }
        else{
            Log.i(TAG,"No records in the table!");
        }

        do{
            sa=c.getInt(c.getColumnIndex(COLUMN_ID));
            if(!c.getString(c.getColumnIndex(COLUMN_MEAN)).equals(" ")) {
                mean = Double.parseDouble(c.getString(c.getColumnIndex(COLUMN_MEAN)));
                sd = Double.parseDouble(c.getString(c.getColumnIndex(COLUMN_STANDARD)));
                power=-(Math.pow(x-mean,2))/(2*Math.pow(sd,2));
                g=(1.0/(sd*sqr))*Math.pow(2.72,power);
                results[sa-1]=String.valueOf(g);
            }
            else{
                results[sa-1]="0";
            }

        }while(c.moveToNext());
        c.close();
        TABLE_NAME2="FunctionForAP";
        return results;
    }

}
