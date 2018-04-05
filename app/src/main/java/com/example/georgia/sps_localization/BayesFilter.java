package com.example.georgia.sps_localization;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class BayesFilter extends AppCompatActivity {

    /********************************************Declaring Variables***************************************************/
    //Declaring TAG
    public String TAG="com.example.georgia.sps_localization";

    String sid;

    String cell,mac,mean,sd,PreviousMac,RSSID,RSSI,subRSSID,previousMac="0";
    int index,myIndex,k=1,i=0,j=0,counter=0;
    double pr;
    private WifiManager wifiManager;
    List<ScanResult> scanResults;
    TextView txt1,txt2;
    //String table that contains gaussian results
    String[] ResultingProb=new String[19];
    //Database Handler and tables needed to add values to tables
    DatabaseHandler myDb;
    TablePrior prior;
    CellFunctionTable accessPoints;
    ApListTable ApList;
    ProbAPTable results;
    double[] FinalPosterior={0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
    double maxValue=0.0,sum=0.0;
    int cellNumber=0;
    boolean exitLoop=false,accessed=false;

    //String Table to save the 4 items found in each row of the file
    String[] words;

    //Table that shows up to which cell an access point reaches in order to complete the ones left with null in CellFunction Table
    int[] RowsLeft=new int[43];

/*************************************Function that creates the Bayes activity************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bayes_filter);

        //Line to keep screen on permanently
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean alreadyVisited = prefs.getBoolean("called",false);

        //Initialize variables
        myDb=new DatabaseHandler(this,null,null,1);
        prior=new TablePrior();
        ApList=new ApListTable();
        accessPoints=new CellFunctionTable();
        results=new ProbAPTable();
        txt1=(TextView)findViewById(R.id.cellNumber);
        txt2=(TextView)findViewById(R.id.possibility);

        // If activity was already visited
        if(!alreadyVisited){
            //Create prior table for Bayes Filters
            prior.setProbability(Double.toString(1.0/19.0));
            for (index=1;index<=19;index++) {
                myDb.addRow(prior);
            }

            readFile();
            RowsLeft[42]=k;
            for(i=1;i<=43;i++) {
                if(RowsLeft[i-1]!=0){
                    while(RowsLeft[i-1]<=19){
                        accessPoints.setMean(" ");
                        accessPoints.setCell_name(String.valueOf(RowsLeft[i-1]));
                        accessPoints.setSd(" ");
                        myDb.addRow2(accessPoints,i);
                        RowsLeft[i-1]=RowsLeft[i-1]+1;
                    }
                }
            }
            for(i=1;i<=43;i++){
                results.setProbability("0");
                for(j=1;j<=19;j++){
                    myDb.addRow3(results,i);
                }
            }
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putBoolean("called",true);
            editor.commit();
        }
        else{
            Log.i(TAG,"Already created prior. Now update");
            for (index=1;index<=19;index++) {
                myDb.update1(index,String.valueOf(1.0/19.0));
            }
        }

      //  Log.i(TAG,"Print prior table."+"\n" + myDb.DatabaseToString1());

    /*    for(i=1;i<=43;i++) {
            Log.i(TAG,"FunctionForAP"+ String.valueOf(i)+" " +myDb.DatabaseToString2(i));
            Log.i(TAG,"ProbabilityPerAP"+ String.valueOf(i) +myDb.DatabaseToString3(i));
        }
        Log.i(TAG,"AccessPoints List:"+ myDb.DatabaseToString4());
     */
    }


 /*************************************Function that reads Information File**************************************/
    public void readFile(){
        //Declaring variables for function readFile()
        String data=" ";
       // StringBuffer myBuffer = new StringBuffer();
        InputStream myStream =this.getResources().openRawResource(R.raw.pmfs);
        BufferedReader reader = new BufferedReader(new InputStreamReader(myStream));
        myIndex=0;
        index=0;
        PreviousMac="0";

        if(myStream !=null){
            try{
                while((data=reader.readLine())!= null){
                    words=data.split(",");
                    i++;

                    //Check if format of our data is correct
                        cell=words[0];
                        mac=words[1];
                        mean=words[2];
                        sd=words[3];

                    if(!(mac.equals(PreviousMac))){
                        ApList.setAccessPoint(mac);
                        myDb.addRow4(ApList);
                        if(k-1<19 && myIndex!=0){
                            RowsLeft[myIndex-1]=k;
                        }
                        myIndex++;
                        PreviousMac=mac;
                        k=1;
                    }
                    while(k<=Integer.parseInt(cell)){
                        if (k!=Integer.parseInt(cell)){
                            accessPoints.setMean(" ");
                            accessPoints.setCell_name(String.valueOf(k));
                            accessPoints.setSd(" ");
                        }
                        else{
                            accessPoints.setMean(mean);
                            accessPoints.setCell_name(cell);
                            accessPoints.setSd(sd);
                        }
                        myDb.addRow2(accessPoints,myIndex);
                        k++;
                    }
                    index++;
                }
                myStream.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }



    /*************************************Function that gets called when button Locate Me is pushed**************************************/
    public void LocateMe(View view){
        Log.i(TAG,"Button clicked");
        cellNumber=0;
        maxValue=0.0;
        sum=0.0;
        Runnable r=new Runnable(){
            @Override
            public void run(){
                long present=System.currentTimeMillis();
                long future=present+3*60*1000;
                long check=present+25000;
                //Start loop that repeats itself every 10 seconds
                while(System.currentTimeMillis()<future){
                    if(System.currentTimeMillis()==check){
                        check+=25000;
                        Log.i(TAG,"25 seconds passed");
                        ScanForAP();
                    }
                    if(exitLoop){break;}
                }
                Log.i(TAG,"Exited Loop");
                if(!accessed){
                    try {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                txt1.post(new Runnable() {
                                    public void run() {
                                        txt1.setText("Cell Number: Unkown");
                                    }
                                });
                                txt2.post(new Runnable() {
                                    public void run() {
                                        txt2.setText("Probability: X");
                                    }
                                });
                                Toast.makeText(getBaseContext(),"You might be in the wrong building",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    catch (Throwable t)
                    {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(),"URL exeption!",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else{
                    if(!exitLoop){
                        try {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    txt1.post(new Runnable() {
                                        public void run() {
                                            txt1.setText("Cell Number: "+String.valueOf(cellNumber));
                                        }
                                    });
                                    txt2.post(new Runnable() {
                                        public void run() {
                                            txt2.setText("Probability:"+ String.valueOf(maxValue));
                                        }
                                    });
                                }
                            });
                        }
                        catch (Throwable t)
                        {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getBaseContext(),"URL exeption!",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }
        };
        Thread myThread= new Thread(r);
        myThread.start();

    }

    public void ScanForAP() {
        // Set wifi manager.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Start a wifi scan.
        wifiManager.startScan();
        // Store results in a list.
        scanResults = wifiManager.getScanResults();
        // Write results to a label
        Log.i(TAG,Integer.toString(scanResults.size()));
        for (ScanResult scanResult : scanResults) {
            RSSID=scanResult.BSSID;
            RSSI=Integer.toString(scanResult.level);
            subRSSID=RSSID.substring(0,RSSID.length()-3);
            //Perform gaussian formula
            Log.i(TAG,subRSSID+" "+RSSI);
            if( (myDb.ReturnIndex(subRSSID)!=0) && !(subRSSID.equals(previousMac)) ){
                Log.i(TAG,"Found matching access point");
                accessed=true;
                previousMac=subRSSID;
                ResultingProb=myDb.PerformGauss(myDb.ReturnIndex(subRSSID),Double.parseDouble(RSSI));
                for(i=1;i<=19;i++){
                    results.setProbability(ResultingProb[i-1]);
                    myDb.update3(results,myDb.ReturnIndex(subRSSID),i);
                }
            }
            else{
                Log.i(TAG,"No match");
            }
        }
    /*    for(i=1;i<=43;i++) {
            Log.i(TAG," ProbabilityPerAP"+String.valueOf(i) + " " + myDb.DatabaseToString3(i));
        }*/
        if(accessed){
            //Calculate Average of all Probabilities gathered
            for(i=1;i<=19;i++){
                for(j=1;j<=43;j++){
                    pr=Double.parseDouble(myDb.returnProb(j,i));
                    if(myDb.returnProb(j,i)!="0"){
                        counter++;
                    }
                    FinalPosterior[i-1]+=pr;
                    /**************************************Added yesterday********************************/
                    results.setProbability("0");
                    myDb.update3(results,j,i);
                    /***********************************************************************************/
                }
                FinalPosterior[i-1]=FinalPosterior[i-1]/counter;
                sum+=FinalPosterior[i-1];
                if(maxValue<FinalPosterior[i-1]){
                    maxValue=FinalPosterior[i-1];
                    cellNumber=i;
                }
                counter=0;
                Log.i(TAG,"AvgProb = " + String.valueOf(FinalPosterior[i-1]));
            }
            Log.i(TAG,"Prob= "+String.valueOf(maxValue)+" Cell= "+String.valueOf(cellNumber)+" sum= "+String.valueOf(sum));
            double p=Double.parseDouble(myDb.returnPriorProb(cellNumber));
            maxValue=maxValue/sum;
            String str1=String.format("%.2f",p);
            String str2=String.format("%.2f",maxValue);
            Log.i(TAG,str1+" "+str2+" "+String.valueOf(p));
            if(str1.equals(str2)){
                exitLoop=true;
                try {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            txt1.post(new Runnable() {
                                public void run() {
                                    txt1.setText("Cell Number: "+String.valueOf(cellNumber));
                                }
                            });
                            txt2.post(new Runnable() {
                                public void run() {
                                    txt2.setText("Probability:"+ String.valueOf(maxValue));
                                }
                            });
                        }
                    });
                }
                catch (Throwable t)
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getBaseContext(),"URL exeption!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            for(i=1;i<=19;i++){
                if(FinalPosterior[i-1]==0){
                    myDb.update1(i,String.valueOf(0.0000001));
                }
                else{
                    myDb.update1(i,String.valueOf(FinalPosterior[i-1]/sum));
                }
                /************************************Added yesterday************************/
                FinalPosterior[i-1]=0;
            }
        }
    }
}
