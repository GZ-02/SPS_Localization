package com.example.georgia.sps_localization;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="LocalizationApp.db";
    public int i,j;

    //Table Cell 1
    private static final String TABLE_NAME="AccessPoint";
    private static final String COLUMN_ID="_id";
    private static final String COLUMN_TIMESTAMP="timestamp";
    private static final String COLUMN_RSSI="rssi";

    private String TAG="com.example.georgia.collectdatabayes";


    //Constructor
    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }


    //Override methods
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query ="CREATE TABLE "+ TABLE_NAME + "(" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_TIMESTAMP + " TEXT NULL,";

        for(i=0;i<256;i++){
            if (i==255){
                query=query + COLUMN_RSSI + String.valueOf(i) + " TEXT NULL";
            }
            else{
                query=query + COLUMN_RSSI + String.valueOf(i) + " TEXT NULL,";
            }
        }
        query=query+ ");";
        Log.i(TAG,query);
        db.execSQL(query);
    }

    @Override
    //Upgrade the table
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME );
        onCreate(db);
    }
/*
    //Methods
    //Add a new row to the database
    public void addRow(SensorsTable snst){
        SQLiteDatabase db=this.getWritableDatabase();
        // Use ContentValues to add a row in the table
        ContentValues values = new ContentValues();
        //Add values for each column
        values.put(COLUMN_TIMESTAMP,snst.get_tmst());
        values.put(COLUMN_SSID,snst.get_SSID());
        values.put(COLUMN_RSSI,snst.get_RSSI());
        values.put(COLUMN_LOCTIME,snst.getLocalTime());
        values.put(COLUMN_CELL,snst.getCellNo());
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



    public void deleteAll(){
        SQLiteDatabase db=getWritableDatabase();
        String query="DELETE FROM "+ TABLE_NAME;
        db.execSQL(query);
    }

    public String DatabaseToString(){
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
            dbString+=" , ";
            dbString+=c.getString(c.getColumnIndex(COLUMN_TIMESTAMP));
            dbString+=" , ";
            dbString+=c.getString(c.getColumnIndex(COLUMN_SSID));
            dbString+=" , ";
            dbString+=c.getString(c.getColumnIndex(COLUMN_RSSI));
            dbString+=" , ";
            dbString+=c.getString(c.getColumnIndex(COLUMN_LOCTIME));
            dbString+=" , ";
            dbString+=c.getString(c.getColumnIndex(COLUMN_CELL));
            dbString+="\n";
        }while(c.moveToNext());
        c.close();
        return dbString;

    }
*/



}
