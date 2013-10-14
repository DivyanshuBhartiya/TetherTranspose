package com.example.tethertranspose;

import com.example.tethertranspose.R;
import com.stericson.RootTools.Shell;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static String TAG = "Tether Transpose";
	
	private static Button startButton = null;
	private static Button stopButton = null;
	
	private RelativeLayout trafficRow = null;
	private TextView downloadData = null;
	private TextView uploadData = null;
	private TextView downloadRate = null;
	private TextView uploadRate = null;
	
	private CheckBox pingGateway = null;
	private CheckBox gatewayRoute = null;
	private CheckBox dns1CheckBox = null;
	private CheckBox dns2CheckBox = null;
	
	private static TetherSystemCalls tetherCalls = null;
	private static Shell shell = null;
	private static boolean firstTime = true;
	private static String dns1 = "8.8.8.8";
	private static String dns2 = "4.2.2.2";
	private Thread traffic = null;
	public static MainActivity getCurrentInstance= null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        getCurrentInstance = this;
        startButton = (Button) findViewById(R.id.button1);
        stopButton = (Button) findViewById(R.id.button2);
        tetherCalls = new TetherSystemCalls();
        
        this.trafficRow = (RelativeLayout)findViewById(R.id.trafficRow);
        this.downloadData = (TextView)findViewById(R.id.trafficDown);
        this.uploadData = (TextView)findViewById(R.id.trafficUp);
        this.downloadRate = (TextView)findViewById(R.id.trafficDownRate);
        this.uploadRate  = (TextView)findViewById(R.id.trafficUpRate);
        
        this.pingGateway = (CheckBox) findViewById(R.id.gwPing);
        this.pingGateway.setVisibility(View.VISIBLE);
        this.pingGateway.setClickable(false);
        this.gatewayRoute = (CheckBox) findViewById(R.id.gwRoute);
        this.gatewayRoute.setVisibility(View.VISIBLE);
        this.gatewayRoute.setClickable(false);
        this.dns1CheckBox = (CheckBox) findViewById(R.id.dns1);
        this.dns1CheckBox.setVisibility(View.VISIBLE);
        this.dns1CheckBox.setClickable(false);
        this.dns2CheckBox = (CheckBox) findViewById(R.id.dns2);
        this.dns2CheckBox.setVisibility(View.VISIBLE);
        this.dns2CheckBox.setClickable(false);
        
        if(!tetherCalls.checkForRoot())
        {
        	new AlertDialog.Builder(this)
            .setMessage(
                    "Root is required, root your android phone.")
            .setCancelable(true).create().show();
        	return;
        }
        
        if(!tetherCalls.checkForIfconfig())
        {
        	new AlertDialog.Builder(this)
        	.setMessage("Ifconfig is needed. Check your bin")
        	.setCancelable(true).create().show();
        	return;
        }
        
        if(!tetherCalls.checkForIp())
        {
        	new AlertDialog.Builder(this)
        	.setMessage("Ip is needed. Check you bin")
        	.setCancelable(true).create().show();
        	return ;
        }
        
        final ConnectivityManager connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.d(TAG, "Connectivit Manager = " + connMan.toString());
        
        if(!tetherCalls.checkTetheringSupport(connMan))
		{
			Log.e(TAG, "Tethering is not supported");
			new AlertDialog.Builder(MainActivity.this)
			.setMessage("Tethering is not supported")
			.setCancelable(true).create().show();
			return;
		}
		Log.d(TAG, "Tethering supported");
		if(!tetherCalls.checkUSBPlugged(MainActivity.this))
		{
			Toast.makeText(MainActivity.this,
                    "USB is not pluged, Retry.",
                    Toast.LENGTH_LONG).show();
            return;
		}
		
        startButton.setOnClickListener(new OnClickListener(){
        	
			public void onClick(View arg0) {
				
				for (String tetheredIface : tetherCalls.getTetheredIfaces(connMan))
				{
					int returnCode = tetherCalls.untether(connMan, tetheredIface);
					if(returnCode != 0) 
					{
						Log.e(TAG, "Untethering failed for " + tetheredIface);
					}
					Log.d(TAG, "Untethering succesfull for " + tetheredIface);
				}
				
				shell = tetherCalls.getRootShell();
				
				
				
				/*tetheredDev = tetherCalls.findUSBIface(tetherCalls.getTetherableIfaces(connMan), tetherCalls.getTetherableUSBRegexs(connMan));
				tetheredDev = tetheredDev; 
				/*if(tetheredDev==null)
				{
					Log.e(TAG, "TetheredDev is null");
					new AlertDialog.Builder(MainActivity.this)
					.setMessage("Tethered dev is null")
					.setCancelable(true).create().show();
					return;
				}
				int tethered = tetherCalls.tether(connMan, "rndis0");
				if(tethered!=0)
				{
					Log.d(TAG, "Failed to tether " + tetheredDev);
					new AlertDialog.Builder(MainActivity.this)
					.setMessage("Failed to tether " + tetheredDev)
					.setCancelable(true).create().show();
					return;
				}
				Log.d(TAG, "Tethering "+tetheredDev + "is succesfull.");
				*/
				Intent tetherSettings = new Intent();
		        tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
		        startActivityForResult(tetherSettings,1);
		        
		        
			}
        });
        
        stopButton.setOnClickListener(new OnClickListener(){
        	
        	@Override
			public void onClick(View arg0) 
        	{
				Log.d(TAG, "Stop Button pressed");
				String tetheredDev = tetherCalls.findUSBIface(tetherCalls.getTetheredIfaces(connMan), tetherCalls.getTetherableUSBRegexs(connMan));
				int ret = tetherCalls.untether(connMan, tetheredDev);
				if(ret != 0)
				{
					Log.e(TAG, "Untethering failed for " + tetheredDev);
				}
				Log.d(TAG, "Untethering succesfull for " + tetheredDev);
				
			}
        });
    }
    
    public  Handler trafficHandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{
    		switch(msg.what)
    		{
    		case 1: MainActivity.this.trafficRow.setVisibility(View.VISIBLE);
        	long uploadTraffic = ((DataStruct)msg.obj).uploadData;
        	long downloadTraffic = ((DataStruct)msg.obj).downloadData;
        	long uploadRate = ((DataStruct)msg.obj).uploadRate;
        	long downloadRate = ((DataStruct)msg.obj).downloadRate;

        	// Set rates to 0 if values are negative
        	if (uploadRate < 0)
        		uploadRate = 0;
        	if (downloadRate < 0)
        		downloadRate = 0;
        	
    		MainActivity.this.uploadData.setText(tetherCalls.getData(uploadTraffic));
    		MainActivity.this.downloadData.setText(tetherCalls.getData(downloadTraffic));
    		MainActivity.this.downloadData.invalidate();
    		MainActivity.this.uploadData.invalidate();

    		MainActivity.this.uploadRate.setText(tetherCalls.getRate(uploadRate));
    		MainActivity.this.downloadRate.setText(tetherCalls.getRate(downloadRate));
    		MainActivity.this.downloadRate.invalidate();
    		MainActivity.this.uploadRate.invalidate();
    		break;
    		}
    	}
    	
    };
    
    protected void onStop()
    {
    	Log.d(TAG, "Calling onStop");
    	super.onStop();
    }
    
    public void onDestroy() {
    	Log.d(TAG, "Calling onDestroy()");
    	super.onDestroy();
	}

	public void onResume() {
		Log.d(TAG, "Calling onResume()");
		super.onResume();
	}

    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Activity ResultCode " +resultCode+" Request Code " + requestCode);
        String gateway = "192.168.42.130";
        int ret = tetherCalls.pingGateway(shell, gateway);
        if(ret!=0)
        {
        	Toast.makeText(MainActivity.this,
                    "Gateway "+gateway+"cannot be pinged.",
                    Toast.LENGTH_LONG).show();
        	this.pingGateway.setChecked(false);
            return;
        }
        Log.d(TAG, "Pinging succesfull . return to main activity");
        Toast.makeText(MainActivity.this,
                "Gateway " + gateway +"ping successfull.",
                Toast.LENGTH_LONG).show();
        this.pingGateway.setChecked(true);
        
        ret=tetherCalls.addGateway(shell, "rndis0", "192.168.42.130");
        if(ret!=0)
        {
        	Toast.makeText(MainActivity.this,
                    "Gateway "+gateway+"cannot be added, Retry.",
                    Toast.LENGTH_LONG).show();
        	this.gatewayRoute.setChecked(false);
            return;
        }
        Log.d(TAG, "Gateway command return code " + ret);
        Toast.makeText(MainActivity.this,
                "Gateway " + gateway +"added successfully.",
                Toast.LENGTH_LONG).show();
        this.gatewayRoute.setChecked(true);
        
        ret = tetherCalls.setDNS1(dns1);
        if(ret!=0)
        {
        	Toast.makeText(MainActivity.this,
                    "DNS "+dns1+"cannot be set, Retry.",
                    Toast.LENGTH_LONG).show();
        	this.dns1CheckBox.setChecked(false);
            return;
        }
        Log.d(TAG, "Set DNS1 command return code = " + ret);
        Toast.makeText(MainActivity.this,
                "DNS "+dns1+"set successfully.",
                Toast.LENGTH_LONG).show();
        this.dns1CheckBox.setChecked(true);
        
        ret = tetherCalls.setDNS2(dns2);
        if(ret!=0)
        {
        	Toast.makeText(MainActivity.this,
                    "DNS "+dns2+"cannot be set, Retry.",
                    Toast.LENGTH_LONG).show();
        	this.dns2CheckBox.setChecked(false);
            return;
        }
        Log.d(TAG, "Setprop DNS2 command return code = "+ret);
        Toast.makeText(MainActivity.this,
                "DNS "+dns2+"set successfully.",
                Toast.LENGTH_LONG).show();
        this.dns2CheckBox.setChecked(true);
        
        TrafficCounter counter = new TrafficCounter();
        counter.network = "rndis0";
        traffic = new Thread(counter);
       
        traffic.start();
        Toast.makeText(MainActivity.this,
                "Traffic Counter started successfully.",
                Toast.LENGTH_LONG).show();
    }
    
    protected void OnResume()
    {
    	super.onResume();
        Log.i(TAG, "onResume");
        if (firstTime){
        Log.i(TAG, "it's the first time");
        firstTime = false;
        }

        else{
        Log.i(TAG, "it's not the first time");
        int ret = tetherCalls.pingGateway(shell, "192.168.42.130");
        Log.d(TAG, "Pinging succesfull . return to main activity");
        new AlertDialog.Builder(MainActivity.this).setMessage("Pinging succesfull "+ret).create().show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
}
