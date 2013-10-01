package com.example.tethertranspose;

import java.util.Date;

import android.os.Message;

public class TrafficCounter implements Runnable 
{
	String network;
	long lastTime;
	long lastUpload;
	long lastDownload;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		DataStruct dataPacket = new DataStruct();
		this.lastDownload =0;
		this.lastUpload=0;
		long[] data = new long[]{0,0};
		long[] dataStart = new long[2];
		for (String line : dataPacket.readLinesFromFile("/proc/net/dev")) {
    		if (line.startsWith(network) == false)
    			continue;
    		line = line.replace(':', ' ');
    		String[] values = line.split(" +");
    		dataStart[0] = Long.parseLong(values[1]);
    		dataStart[1] = Long.parseLong(values[9]);
    	}
		this.lastTime = new Date().getTime();
		while(!Thread.currentThread().isInterrupted())
		{
			if (network == "")
			{
				dataPacket.downloadData=0;
				dataPacket.downloadRate=0;
				dataPacket.uploadData=0;
				dataPacket.uploadRate=0;
				Message msg = Message.obtain();
		    	msg.what = 1;
		    	msg.obj = dataPacket;
		    	MainActivity.getCurrentInstance.trafficHandler.sendMessage(msg);
		    	try {
	                Thread.sleep(3000);
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
			}
	    	for (String line : dataPacket.readLinesFromFile("/proc/net/dev")) {
	    		if (line.startsWith(network) == false)
	    			continue;
	    		line = line.replace(':', ' ');
	    		String[] values = line.split(" +");
	    		data[0] += Long.parseLong(values[1]);
	    		data[1] += Long.parseLong(values[9]);
	    	}
	    	long currentTime = new Date().getTime();
	    	float totalTime = (float) (currentTime - this.lastTime)/1000;
	    	dataPacket.uploadData = data[0]-dataStart[0];
	    	dataPacket.downloadData = data[1]-dataStart[1];
	    	dataPacket.uploadRate = (long) ((dataPacket.uploadData - this.lastUpload)*8/totalTime);
	    	dataPacket.downloadRate = (long) ((dataPacket.downloadData - this.lastDownload) *8 /totalTime);
	    	
	    	this.lastDownload = dataPacket.downloadData;
	    	this.lastUpload = dataPacket.uploadData;
	    	this.lastTime = currentTime;
	    	Message msg = Message.obtain();
	    	msg.what = 1;
	    	msg.obj = dataPacket;
	    	MainActivity.getCurrentInstance.trafficHandler.sendMessage(msg);
	    	try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
		}
    	
	}
	

}
