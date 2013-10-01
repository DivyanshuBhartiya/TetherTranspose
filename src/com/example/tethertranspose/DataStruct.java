package com.example.tethertranspose;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

public class DataStruct {

	long downloadData;
	long uploadData;
	long downloadRate;
	long uploadRate;
	private static String TAG = "TetherTranspose";
	
	public ArrayList<String> readLinesFromFile(String filename) {
    	String line = null;
    	BufferedReader br = null;
    	InputStream ins = null;
    	ArrayList<String> lines = new ArrayList<String>();
    	Log.d(TAG, "Reading lines from file: " + filename);
    	//appendLog(MSG_TAG + "Reading lines from file: " + filename); //added temp
    	try {
    		ins = new FileInputStream(new File(filename));
    		br = new BufferedReader(new InputStreamReader(ins), 8192);
    		while((line = br.readLine())!=null) {
    			lines.add(line.trim());
    		}
    	} catch (Exception e) {
    		Log.d(TAG, "Unexpected error - Here is what I know: "+e.getMessage());
    		//appendLog(MSG_TAG +"Unexpected error - Here is what I know: "+e.getMessage()); //added temp
    	}
    	finally {
    		try {
    			ins.close();
    			br.close();
    		} catch (Exception e) {
    			// Nothing.
    		}
    	}
    	return lines;
    }
}
