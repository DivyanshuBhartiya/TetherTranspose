package com.example.tethertranspose;

import com.example.tethertranspose.R;
import com.stericson.RootTools.Shell;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static String TAG = "TetherTranspose";
	
	private static Button startButton = null;
	private static Button stopButton = null;
	
	private RelativeLayout trafficRow = null;
	private TextView downloadData = null;
	private TextView uploadData = null;
	private TextView downloadRate = null;
	private TextView uploadRate = null;
	
	private CheckBox root = null;
	private CheckBox ifconfig = null;
	private CheckBox ip = null;
	private CheckBox tetherSupport = null;
	
	private String gateway;
	public static int wait;
	
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
        startButton.setBackgroundResource(R.drawable.start);
        stopButton = (Button) findViewById(R.id.button2);
        stopButton.setBackgroundResource(R.drawable.stop);
        tetherCalls = new TetherSystemCalls();
        wait=0;
        
        this.trafficRow = (RelativeLayout)findViewById(R.id.trafficRow);
        this.downloadData = (TextView)findViewById(R.id.trafficDown);
        this.uploadData = (TextView)findViewById(R.id.trafficUp);
        this.downloadRate = (TextView)findViewById(R.id.trafficDownRate);
        this.uploadRate  = (TextView)findViewById(R.id.trafficUpRate);
        
        this.root = (CheckBox) findViewById(R.id.root);
        this.root.setVisibility(View.VISIBLE);
        this.root.setClickable(false);
        this.ifconfig = (CheckBox) findViewById(R.id.ifconfig);
        this.ifconfig.setVisibility(View.VISIBLE);
        this.ifconfig.setClickable(false);
        this.ip = (CheckBox) findViewById(R.id.ip);
        this.ip.setVisibility(View.VISIBLE);
        this.ip.setClickable(false);
        this.tetherSupport = (CheckBox) findViewById(R.id.tetherSupport);
        this.tetherSupport.setVisibility(View.VISIBLE);
        this.tetherSupport.setClickable(false);
        
        if(!tetherCalls.checkForRoot())
        {
        	this.root.setChecked(false);
        	new AlertDialog.Builder(this)
            .setMessage(
                    "Root is required, root your android phone.")
            .setCancelable(true).create().show();
        	return;
        }
        this.root.setChecked(true);
        
        if(!tetherCalls.checkForIfconfig())
        {
        	this.ifconfig.setChecked(false);
            new AlertDialog.Builder(this)
        	.setMessage("Ifconfig is needed. Check your bin")
        	.setCancelable(true).create().show();
        	return;
        }
        this.ifconfig.setChecked(true);
        
        if(!tetherCalls.checkForIp())
        {
        	this.ip.setChecked(false);
            new AlertDialog.Builder(this)
        	.setMessage("Ip is needed. Check you bin")
        	.setCancelable(true).create().show();
        	return ;
        }
        this.ip.setChecked(true);
        
        final ConnectivityManager connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.d(TAG, "Connectivity Manager = " + connMan.toString());
        
        if(!tetherCalls.checkTetheringSupport(connMan))
		{
			Log.e(TAG, "Tethering is not supported");
			this.tetherSupport.setChecked(false);
			new AlertDialog.Builder(MainActivity.this)
			.setMessage("Tethering is not supported")
			.setCancelable(true).create().show();
			return;
		}
		Log.d(TAG, "Tethering supported");
		this.tetherSupport.setChecked(true);
        
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
        gateway = "192.168.42.130";
        
        new AlertDialog.Builder(MainActivity.this)
        .setTitle("Please wait")
        .setMessage("Complete the PC side instructions")
        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
				final EditText ipView = new EditText(MainActivity.this);
		        ipView.setText(gateway);
		        
		        new AlertDialog.Builder(MainActivity.this)
		        .setTitle(
		                "please confirm your new connnection ip")
		        .setView(ipView)
		        .setPositiveButton(
		                "DONE",
		                new DialogInterface.OnClickListener() 
		                {
		                    @Override
		                    public void onClick(DialogInterface dialog,int which) 
		                    {
		                        gateway = ipView.getText().toString();
		                        if (!checkGateway(gateway)) 
		                        {
		                            new AlertDialog.Builder(
		                                    MainActivity.this)
		                                    .setMessage(
		                                            "wrong gateway!")
		                                    .setCancelable(
		                                            true)
		                                    .create()
		                                    .show();
		                            return;
		                        }

		                        
		                        
		                        int ret = tetherCalls.pingGateway(shell, gateway);
		                        if(ret!=0)
		                        {
		                        	Log.i(TAG, "Ping return code "+ret);
		                        	Toast.makeText(MainActivity.this,
		                                    "Gateway "+gateway+" cannot be pinged.",
		                                    Toast.LENGTH_LONG).show();
		                        	//fail();
		                        	//return;
		                        }
		                        Log.d(TAG, "Pinging succesfull . return to main activity");
		                        Toast.makeText(MainActivity.this,
		                                "Gateway " + gateway +" ping successfull.",
		                                Toast.LENGTH_LONG).show();
		                        
		                        
		                        ret=tetherCalls.addGateway(shell, "rndis0", gateway);
		                        if(ret!=0)
		                        {
		                        	Toast.makeText(MainActivity.this,
		                                    "Gateway "+gateway+"cannot be added, Retry.",
		                                    Toast.LENGTH_LONG).show();
		                        	int c= tetherCalls.configureDHCP("rndis0");
		                        	if(c!=0)
		                        	{
		                        		Toast.makeText(MainActivity.this, "netcfg rndis0 dhcp : Error", Toast.LENGTH_LONG).show();
		                        		fail();
		                        		return;
		                        	}
		                        	//fail();
		                        	//return;
		                        }
		                        Log.d(TAG, "Gateway command return code " + ret);
		                        Toast.makeText(MainActivity.this,
		                                "Gateway " + gateway +"added successfully.",
		                                Toast.LENGTH_LONG).show();
		                        
		                        ret = tetherCalls.setDNS1(dns1);
		                        if(ret!=0)
		                        {
		                        	Toast.makeText(MainActivity.this,
		                                    "DNS "+dns1+"cannot be set, Retry.",
		                                    Toast.LENGTH_LONG).show();
		                        	return;
		                        }
		                        Log.d(TAG, "Set DNS1 command return code = " + ret);
		                        Toast.makeText(MainActivity.this,
		                                "DNS "+dns1+"set successfully.",
		                                Toast.LENGTH_LONG).show();
		                        
		                        ret = tetherCalls.setDNS2(dns2);
		                        if(ret!=0)
		                        {
		                        	Toast.makeText(MainActivity.this,
		                                    "DNS "+dns2+"cannot be set, Retry.",
		                                    Toast.LENGTH_LONG).show();
		                        	return;
		                        }
		                        Log.d(TAG, "Setprop DNS2 command return code = "+ret);
		                        Toast.makeText(MainActivity.this,
		                                "DNS "+dns2+"set successfully.",
		                                Toast.LENGTH_LONG).show();
		                        
		                        TrafficCounter counter = new TrafficCounter();
		                        counter.network = "rndis0";
		                        traffic = new Thread(counter);
		                       
		                        traffic.start();
		                        Toast.makeText(MainActivity.this,
		                                "Traffic Counter started successfully.",
		                                Toast.LENGTH_LONG).show();
		                    }

							private void fail() {
								new AlertDialog.Builder(
		                                MainActivity.this)
		                                .setMessage("Reverse Tethering failed")
		                                .setCancelable(
		                                        true)
		                                .create()
		                                .show();
							}

							private boolean checkGateway(String gateway) {
								String[] tokens = gateway.split("\\.");

						        if (tokens.length != 4 || !tokens[0].equalsIgnoreCase("192")
						                || !tokens[1].equalsIgnoreCase("168")) {

						            return false;
						        }

						        int third = -1;
						        int fourth = -1;
						        try {
						            third = Integer.valueOf(tokens[2]);
						            fourth = Integer.valueOf(tokens[3]);
						        } catch (NumberFormatException e) {}
						        if (third < 0 || third > 255) {

						            return false;
						        }

						        if (fourth < 0 || fourth > 255) {

						            return false;
						        }
						        return true;
							}

		                }

		        ).show();
			}
		})
        .create().show();
        
        

}
        
    
    protected void OnResume()
    {
    	super.onResume();
        Log.i(TAG, "onResume");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
}
