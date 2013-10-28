package com.example.tethertranspose;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.CommandCapture;
import com.stericson.RootTools.Shell;
import com.stericson.RootTools.RootTools;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TetherSystemCalls {
	private static String TAG = "TetherTranspose";
	private static AtomicInteger counter = new AtomicInteger();

	boolean checkForRoot()
	{
		return RootTools.isRootAvailable();
	}
	
	boolean checkForIfconfig()
	{
		return RootTools.checkUtil("ifconfig");
	}
	
	boolean checkForNetcfg()
	{
		return RootTools.checkUtil("netcfg");
	}
	
	boolean checkForIp()
	{
		return RootTools.checkUtil("ip");
	}
	
	boolean checkTetheringSupport(final ConnectivityManager connMan)
	{
		try
		{
			boolean support = (Boolean) ConnectivityManager.class.getMethod("isTetheringSupported").invoke(connMan);
			Log.d(TAG, "Tethering supported =" + support);
			return support;
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in checkTetheringSupport. Message = " + e.getMessage());
			return false;
		}
	}
	
	String[] getTetheredIfaces(final ConnectivityManager connMan)
	{
		try
		{
			String[] tetheredIfaces = (String[]) ConnectivityManager.class.getMethod("getTetheredIfaces").invoke(connMan);
			Log.d(TAG, "TetheredIfaces =" + Arrays.toString(tetheredIfaces));
			return tetheredIfaces;
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in getTetherdIfaces. Message = " + e.getMessage());
			return new String[0];
		}
	}
	
	int untether(final ConnectivityManager connMan, String tetherdedIface)
	{
		try
		{
			int returnCode = (Integer) ConnectivityManager.class.getMethod("untether", String.class).invoke(connMan, tetherdedIface);
			Log.d(TAG, "Untethering " + tetherdedIface);
			if(returnCode != 0)
			{
				Log.i(TAG, "Failed to untether " + tetherdedIface +".");
			}
			return returnCode;
		}
		catch(Exception e)
		{
			Log.e(TAG,"Exception caught in untether " + tetherdedIface + ". Message = " + e.getMessage());
			return -1;
		}
	}
	
	String[] getTetherableUSBRegexs(final ConnectivityManager connMan)
	{
		try
		{
			String[] usbRegexs = (String[]) ConnectivityManager.class.getMethod("getTetherableUsbRegexs").invoke(connMan);
			Log.d(TAG, "TetherableUSBRegexs = " + Arrays.toString(usbRegexs));
			return usbRegexs;
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in getTetherableUSBRegexs. Message = " + e.getMessage());
			return new String[0];
		}
	}
	
	String findUSBIface(String[] interfaces, String[] regexes) {
        for (String iface : interfaces) {
            for (String regex : regexes) {
                if (iface.matches(regex)) {
                    return iface;
                }
            }
        }
        return null;
    }
	
	String[] getTetherableIfaces(final ConnectivityManager connMan)
	{
		try
		{
			String[] tetherableIfaces = (String[]) connMan.getClass().getMethod("getTetherableIfaces").invoke(connMan);
			Log.d(TAG, "TetherableIfaces = " + Arrays.toString(tetherableIfaces) );
			return tetherableIfaces;
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in getTetherableIfaces. Message = " + e.getMessage());
			return new String[0];
		}
	}
	
	int tether(final ConnectivityManager connMan, String tetherableIface)
	{
		try
		{
			int returnCode = (Integer) ConnectivityManager.class.getMethod("tether", String.class).invoke(connMan, tetherableIface);
			if(returnCode != 0)
			{
				Log.i(TAG, "Failed to tether " + tetherableIface+" "+returnCode);
			}
			return returnCode;
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in tether " + tetherableIface + ". Mesage = " + e.getMessage());
			return -1;
		}
	}
	
	Shell getRootShell()
	{
		try
		{
			Log.d(TAG, "Returning RootShell to MainActivity");
			return RootTools.getShell(true);
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in getRootShell. Mesaage = " + e.getMessage());
			return null;
		}
	}
	
	int pingGateway(Shell shell, String gateway)
	{
		try
		{
			Command comm = new CommandCapture(counter.incrementAndGet(), "ping -c 4 " + gateway);
			RootTools.getShell(true).add(comm).waitForFinish();
			Log.d(TAG, "ping " + gateway);
			return comm.exitCode();
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in pingGateway. Message = " + e.getMessage());
			return -1;
		}
	}
	
	int ifconfigIP(Shell shell, String dev, String ip)
	{
		try
		{
			Command comm = new CommandCapture(counter.incrementAndGet(), "ifconfig " + dev + " " + ip + " netmask 255.255.255.0");
			Log.d(TAG, "ifconfig " + dev + " " + ip + " netmask 255.255.255.0");
			shell.add(comm).waitForFinish(1000);
			return comm.exitCode();
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in ifconfigIP. Message = " + e.getMessage());
			return -1;
		}
	}
	
	int addGateway(Shell shell, String dev, String gateway)
	{
		try
		{
			Command comm = new CommandCapture(counter.incrementAndGet(), "ip route add default via " + gateway + " dev " + dev);
			Log.d(TAG, "ip route add default via " + gateway + " dev " + dev);
			RootTools.getShell(true).add(comm).waitForFinish();
			return comm.exitCode();
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in addGateway. Message = " + e.getMessage());
			return -1;
		}
	}
	
	int deleteGateway()
	{
		try
		{
			Command comm = new CommandCapture(counter.incrementAndGet(), "ip route del default");
			Log.d(TAG, "ip route del default");
			RootTools.getShell(true).add(comm).waitForFinish();
			return comm.exitCode();
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in deleteGateway. Message = " + e.getMessage());
			return -1;
		}
	}
	
	
	boolean checkUSBPlugged(Context context)
	{
		Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return (plugged == BatteryManager.BATTERY_PLUGGED_USB);
	}
	
	int setDNS1(String dns1)
	{
		try
		{
			Command comm = new CommandCapture(counter.incrementAndGet(), "setprop net.dns1 " + dns1);
			Log.d(TAG,"setprop net.dns1 " + dns1);
			RootTools.getShell(true).add(comm).waitForFinish();
			return comm.exitCode();
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in setDNS1. Message = " + e.getMessage());
			return -1;
		}
	}
	
	int setDNS2(String dns2)
	{
		try
		{
			Command comm = new CommandCapture(counter.incrementAndGet(), "setprop net.dns1 " + dns2);
			Log.d(TAG,"setprop net.dns1 " + dns2);
			RootTools.getShell(true).add(comm).waitForFinish();
			return comm.exitCode();
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in setDNS1. Message = " + e.getMessage());
			return -1;
		}
	}
	
	String getData(long data)
	{
		if(data < 1e6)
		{
			float  bytes = (float)((int)(data*10/1024))/10;
			return bytes +" kB";
		}
		else
		{
			float bytes = (float)((int)(data*100/1024/1024))/100;
			return bytes + " MB";
		}
	}
	
	String getRate(long rate)
	{
		if(rate < 1e6)
		{
			float bytes = (float)((int)(rate*10/1024))/10;
			return bytes + " kbps";
		}
		else
		{
			float bytes = (float)((int)(rate*100/1024/1024))/100;
			return bytes +"mbps";
		}
	}

	public int configureDHCP(String dev) {
		// TODO Auto-generated method stub
		try
		{
			Command comm = new CommandCapture(counter.incrementAndGet(),"netcfg "+dev+" dhcp");
			Log.d(TAG, "netcfg "+dev+" dhcp");
			RootTools.getShell(true).add(comm).waitForFinish(10000);
			return comm.exitCode();
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception caught in configureDHCP. Message = " + e.getMessage());
			return -1;
		}
	}
	
	public void addToLog(String data)
	{
		String logFileName=LogView.dataPath+"/tetherTranspose.log";
		File logFile=new File(logFileName);
		if(!logFile.exists())
		{
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(logFile,true));
			writer.append(data);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
