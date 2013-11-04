package com.example.tethertranspose;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class UserGuideView extends Activity {
	
	private static String TAG = "TetherTranspose";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userguide_view);
		Log.d(TAG, "Setting up guide");
	}

}
