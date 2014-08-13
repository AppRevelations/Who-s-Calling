package com.apprevelations.whoscallingme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
	
	Button start,stop;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		start= (Button) findViewById(R.id.bstart);
		stop= (Button) findViewById(R.id.bstop);
		start.setOnClickListener(this);	
		stop.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.bstart:
			startService(new Intent(MainActivity.this, PhoneState.class));
			break;
		case R.id.bstop:
			stopService(new Intent(MainActivity.this, PhoneState.class));
			break;
		
		}
	}
}