package com.example.tethertranspose;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class LogView extends Activity{

	private static String TAG = "TetherTranspose";
	
	public static String dataPath;
	
	private static final String head = "<html><head><title>background-color</title> "+
		 	"<style type=\"text/css\"> "+
		 	"body { background-color:#181818; font-family:Arial; font-size:100%; color: #ffffff } "+
		 	".date { font-family:Arial; font-size:80%; font-weight:bold} "+
		 	".done { font-family:Arial; font-size:80%; color: #2ff425} "+
		 	".failed { font-family:Arial; font-size:80%; color: #ff3636} "+
		 	".skipped { font-family:Arial; font-size:80%; color: #6268e5} "+
		 	"</style> "+
		 	"</head><body>";
	
	private static final String tail = "</body></html>";
	
	private WebView webView = null;
	
	public void setPath(String path)
	{
		dataPath=path;
	}
	
	public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
	        setContentView(R.layout.log_view);
	        
	        // Init Application
	        
	        this.webView = (WebView) findViewById(R.id.webviewLog);
	        this.webView.getSettings().setJavaScriptEnabled(false);
	        this.webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
	        this.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
	        
	        this.webView.getSettings().setSupportMultipleWindows(false);
	        this.webView.getSettings().setSupportZoom(false);
	        this.setupWebView();
	    }

	private void setupWebView() {
		// TODO Auto-generated method stub
		this.webView.loadDataWithBaseURL("fake://tetherTranspose.logs", head+this.readLog()+tail, "text/html", "UTF-8", "fake://tetherTranspose.logs");
	}

	private String readLog() {
		// TODO Auto-generated method stub
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		String data = "";
		try
		{
			File logFile= new File("");
			fileInputStream = new FileInputStream(logFile);
			inputStreamReader = new InputStreamReader(fileInputStream,"utf-8");
			char[] buffer = new char[(int) logFile.length()];
			inputStreamReader.read(buffer);
			data = new String(buffer);
			inputStreamReader.close();
			fileInputStream.close();
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception occured in reading Log file. "+e.getMessage());
		}
		
		return data;
	}
}
