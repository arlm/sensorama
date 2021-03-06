// Copyright (c) 2015, Wojciech Adam Koszek <wojciech@koszek.com>
// All rights reserved.

package com.barvoy.sensorama;

import android.app.Activity;
import android.graphics.Color;
import android.os.Environment;
import android.os.StatFs;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Build;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseAnalytics;
import com.parse.ParseCrashReporting;

public class MainActivity extends Activity {

    public Sensorama S;
    static public int sampleNumber;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Locale.setDefault(new Locale("en", "US"));

        Button buttonStartEnd = (Button) findViewById(R.id.buttonStartEnd);
        buttonStartEnd.setBackgroundColor(SRCfg.buttonColorInitial);

        if (SRCfg.doParseBootstrap) {
		    parseBootstrap();
        }

        storageDebug();

        S = new Sensorama(this);

        sampleUpdateDate(sampleDateFmt(SRCfg.dateFormat) + "...");

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (S.isEnabled()) {
                    S.capture();
                    sampleNumber++;
                }
            }
        }, 0, SRCfg.interval);
    }

    public void forceCrash(View view) {
        throw new RuntimeException("This is a crash");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void recordingStartEnd(View view) {
        // Do something in response to button
        Button buttonStartEnd = (Button) findViewById(view.getId());

        String textCurrent = buttonStartEnd.getText().toString();

        String textStart = "Start recording";
        if (textCurrent.equals(textStart)) {
            buttonStartEnd.setText("Stop recording");
            buttonStartEnd.setBackgroundColor(SRCfg.buttonColorStopped);
            sampleNumber = 0;
            counterUpdate();
            S.enable(true);
            System.out.println("Started");
            sampleUpdateDate(sampleDateFmt(SRCfg.dateFormat));
        } else {
            buttonStartEnd.setText(textStart);
            buttonStartEnd.setBackgroundColor(SRCfg.buttonColorStart);
            S.enable(false);
            counterUpdate();
            System.out.println("Stopped");
            sampleUpdateDate(getSampleDate() + "-" + sampleDateFmt(SRCfg.timeFormat));
            recordingShare();
        }
    }

    public void counterUpdate() {
        TextView sampleCounter = (TextView)findViewById(R.id.sampleCounter);
        System.out.println("Sample number: " + sampleNumber);
        sampleCounter.setText("" + sampleNumber);
    }

    public void recordingShare() {
        String sampleDateStr = getSampleDate();
        String sampleFileName = sampleDateStr + ".json";
        System.out.printf("recordingShare: %s\n", sampleFileName);
        String sampleFilePath = makeSampleFilePath(sampleFileName);
        File sampleFile = makeSampleFile(sampleFilePath);
        SRJSON json = new SRJSON();
        try {
            BufferedWriter fo = new BufferedWriter(new FileWriter(sampleFile));
            json.dump(S, fo, sampleDateStr, SRCfg.interval, getDeviceName(), getSampleName());
            fo.close();
            sampleFileExport(sampleFileName, sampleFilePath);
        } catch (Exception e) {
            SRDbg.l("Couldn't write data to a file:" + e.toString());
        }
        SRDbg.l("--- printing " + sampleFilePath + "---\n");
        debugSampleFile(sampleFilePath);
    }

    public String sampleFileGetContent(String filePath) {
        String lineStr;
        String fileContent = "";

        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((lineStr = bufferedReader.readLine()) != null) {
                fileContent += lineStr + "\n";
            }
            bufferedReader.close();

        } catch(FileNotFoundException ex) {
            System.out.println("Unable to open a file '" + filePath + "'");
        } catch(IOException ex) {
            System.out.println("Error reading a file '" + filePath + "'");
        }
        return fileContent;
    }

    public void sampleFileExport(String fileName, String filePath) {
        String fileContent = sampleFileGetContent(filePath);
        byte[] data = fileContent.getBytes();
        ParseFile file = new ParseFile(fileName, data);

        TextView fileSize = (TextView) findViewById(R.id.sampleSize);
        fileSize.setText("" + fileContent.length());

        file.saveInBackground();
        
        ParseObject jobApplication = new ParseObject("SensoramaFile");
        jobApplication.put("fileName", fileName);
        jobApplication.put("sampleFile", file);
        jobApplication.put("devname", SRCfg.deviceName);
        jobApplication.saveInBackground();
    }

    public String sampleDateFmt(String dateFmt) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFmt, Locale.US);
        return sdf.format(new Date());
    }

    public String getSampleDate() {
        TextView userDate = (TextView)findViewById(R.id.userDate);
        return userDate.getText().toString();
    }

    public String getSampleName() {
        TextView userName = (TextView)findViewById(R.id.userName);
        return userName.getText().toString();
    }

    public void sampleUpdateDate(String str) {
        // Set the date of the start
        TextView userDate = (TextView)findViewById(R.id.userDate);
        userDate.setText(str);
    }

    public long spaceAvailableMB() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getBlockCount();
        long availableMB = bytesAvailable / 1048576;
        return availableMB;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void storageDebug() {
        if (!isExternalStorageWritable()) {
            SRDbg.l("Can't write to external storage");
        } else {
            SRDbg.l("Storage detected");
        }
        SRDbg.l("Free storage: " + spaceAvailableMB());
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(isSDPresent) {
            SRDbg.l("SD not available");
        } else {
            SRDbg.l("SD isn't available");
        }
    }

    public String makeSampleStorageDir(String sampleFileName) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), sampleFileName);
        if (!dir.mkdirs()) {
            SRDbg.l("Directory not created");
        } else {
            SRDbg.l("Directory created: " + dir.getAbsolutePath());
        }
        return dir.getAbsolutePath();
    }

    public String makeSampleFilePath(String fileName) {
        String sampleDirPath = makeSampleStorageDir(SRCfg.addDirName);
        String sampleFilePath = sampleDirPath + "/" + fileName;
        return sampleFilePath;
    }

    public File makeSampleFile(String sampleFilePath) {
        File file = null;
        try {
            file = new File(sampleFilePath);
        } catch (Exception e) {
            SRDbg.l("Failed to create a file");
        }
        return file;
    }

    public void debugSampleFile(String fileName) {
        if (SRCfg.doDebug) {
            String fileContent = sampleFileGetContent(fileName);
            SRDbg.l(fileContent);
        }
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void parseBootstrap() {
        try {
            Thread.sleep(3000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        for (int i = 0; i < 1; i++) {
            System.out.print("XXX wrinting" + i);
            ParseObject testObject = new ParseObject("TestObject");
            testObject.put("foo", "bar");
            testObject.saveInBackground();
        }

        Map<String, String> dimensions = new HashMap<String, String>();
        // What type of news is this?
        dimensions.put("category", "politics");
        // Is it a weekday or the weekend?
        dimensions.put("dayType", "weekday");
        // Send the dimensions to Parse along with the 'read' event

        ParseAnalytics.trackEventInBackground("read", dimensions);
    }
}
