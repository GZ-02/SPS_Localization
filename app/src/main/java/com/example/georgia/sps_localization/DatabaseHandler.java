package com.example.georgia.sps_localization;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="LocalizationApp.db";
    public int i,j;

    //Columns for the table Prior
    private static final String TABLE_NAME="Prior";
    private static final String TABLE_NAME2="FunctionForAP";
    private static final String COLUMN_ID="_id";
    private static final String COLUMN_PROBABILITY="probability";

    //Columns for table CellFunction
    private static final String COLUMN_CELLNUMBER="cell";

    private String TAG="com.example.georgia.sps_localization";

    //Constructor
    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    //Override methods
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query ="CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_PROBABILITY + " TEXT NULL " + ");";
        Log.i(TAG,query);
        db.execSQL(query);

        String query2="CREATE TABLE " + TABLE_NAME2;
        for (i=1; i<=19;i++){
            query2=query2 + Integer.toString(i);
            query2=query2 + COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," + ");";
        }
    }

    @Override
    //Upgrade the table
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME );
        onCreate(db);
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


    //Add a new row to the table Prior
    public void addRow2(CellFunctionTable cft){
        SQLiteDatabase db=this.getWritableDatabase();
        // Use ContentValues to add a row in the table
        ContentValues values = new ContentValues();

        //Add values for each column
        values.put(COLUMN_PROBABILITY,cft.getCell_name());

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

    //Delete all rows from table Prior
    public void deleteAll1(){
        SQLiteDatabase db=getWritableDatabase();
        String query="DELETE FROM "+ TABLE_NAME;
        db.execSQL(query);
    }

    //Delete all rows from table CellFunctionTable
    public void deleteAll2(){
        SQLiteDatabase db=getWritableDatabase();
        String query="DELETE FROM "+ TABLE_NAME2;
        db.execSQL(query);
    }

    /*
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
            dbString+=c.getString(c.getColumnIndex(COLUMN_PROBABILITY));
            dbString+="\n";
        }while(c.moveToNext());
        c.close();
        return dbString;

    }
*/



}
