package com.apprevelations.whoscallingme;

import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class PhoneState extends Service {
	private CallStateListener mCallStateListener = new CallStateListener();
	private TelephonyManager mTelephonyManager;
	private TextToSpeech tts;
	private int mCallState;

	@Override
	public void onCreate() {
		super.onCreate();
		tts=new TextToSpeech(getApplicationContext(), 
			      new TextToSpeech.OnInitListener() {
		      @Override
		      public void onInit(int status) {
		    	  if (status == TextToSpeech.SUCCESS) {
		    	        int result = tts.setLanguage(Locale.US);
		    	        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
		    	            Log.e("TTS", "This Language is not supported");
		    	            Intent installIntent = new Intent();
		    	            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
		    	            Toast.makeText(getApplicationContext(), "TTS not installed please install it", Toast.LENGTH_LONG).show();
		    	            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    	            startActivity(installIntent);
		    	            
		    	        } else {
		    	        	tts.setLanguage(Locale.UK);
		    	        	tts.setSpeechRate((float) 0.6);
		    	        }
		    	    } else {
		    	        Log.e("TTS", "Initilization Failed!");
		    	    }         
		    	  if(status != TextToSpeech.ERROR){
		             tts.setLanguage(Locale.UK);
		             tts.setSpeechRate((float) 0.6);
		            }				
		         }
		      });
		mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mCallState = mTelephonyManager.getCallState();
		mTelephonyManager.listen(mCallStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public void onDestroy() {
		Log.d("onDestroy", "onDestroy");
		mTelephonyManager.listen(mCallStateListener, PhoneStateListener.LISTEN_NONE);
		        if (tts != null) {
		            tts.stop();
		            tts.shutdown();
		    }
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null; //-- not a bound service--
	}

	private final class CallStateListener extends PhoneStateListener {
		
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			Toast.makeText(getApplicationContext(),"call from "+ incomingNumber, Toast.LENGTH_SHORT).show();
			String Name = getContactName(incomingNumber);
			Toast.makeText(getApplicationContext(), Name, Toast.LENGTH_LONG).show();
			switch (mCallState) {

				case TelephonyManager.CALL_STATE_IDLE:
					if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
						Toast.makeText(getApplicationContext(),"idle --> off hook = new outgoing call", Toast.LENGTH_SHORT).show();
						Log.d("state", "idle --> off hook = new outgoing call");
						// idle --> off hook = new outgoing call
						//triggerSenses(Sense.CallEvent.OUTGOING);
					} else if (state == TelephonyManager.CALL_STATE_RINGING) {

						//change the incoming number here 
						//change the log as well
						
						speakOut("Call recieved from" , Name, incomingNumber);	
						Toast.makeText(getApplicationContext(),"idle --> ringing = new incoming call", Toast.LENGTH_SHORT).show();
						Log.d("state", "idle --> ringing = new incoming call");
						// idle --> ringing = new incoming call
						//triggerSenses(Sense.CallEvent.INCOMING);
						
					}
					break;

				case TelephonyManager.CALL_STATE_OFFHOOK:
					if (state == TelephonyManager.CALL_STATE_IDLE) {

						//change here as well

						Toast.makeText(getApplicationContext(),"off hook --> idle  = disconnected", Toast.LENGTH_SHORT).show();
						Log.d("state", "off hook --> idle  = disconnected");
						// off hook --> idle  = disconnected
						//triggerSenses(Sense.CallEvent.ENDED);
					} else if (state == TelephonyManager.CALL_STATE_RINGING) {

						//change here too

						Toast.makeText(getApplicationContext(), "off hook --> ringing = another call waiting", Toast.LENGTH_SHORT).show();
						Log.d("state", "off hook --> ringing = another call waiting");
						// off hook --> ringing = another call waiting
						//triggerSenses(Sense.CallEvent.WAITING);
					}
					Toast.makeText(getApplicationContext(),"CALL_STATE_OFFHOOK", Toast.LENGTH_SHORT).show();
					Log.d("CALL_STATE_OFFHOOK", String.valueOf(state));
					break;

				case TelephonyManager.CALL_STATE_RINGING:
					if (state == TelephonyManager.CALL_STATE_OFFHOOK) {

						//change here to

						Toast.makeText(getApplicationContext(),"ringing --> off hook = received", Toast.LENGTH_SHORT).show();
						Log.d("state", "ringing --> off hook = received");
						// ringing --> off hook = received
						//triggerSenses(Sense.CallEvent.RECEIVED);
					} else if (state == TelephonyManager.CALL_STATE_IDLE) {

						//change log
						//change notification bar
						//change call screen

						speakOut("Missed Call of", Name, incomingNumber);
						Toast.makeText(getApplicationContext(),"ringing --> idle = missed call", Toast.LENGTH_SHORT).show();
						Log.d("state", "ringing --> idle = missed call");
						// ringing --> idle = missed call
						//triggerSenses(Sense.CallEvent.MISSED);
					}
					break;
			}

			mCallState = state;
		}
	}

	 
	 
	    
	    private void speakOut(String status,String name,String phonenumber) {
	    	
	    	
	    	String text="nothin recieved";
	    	if(name!=null)
	    	{
	    		text = status  + name + "       Phone Number" + phonenumber;
	    	}
	    	else
	    	{
	    		text = status + phonenumber;
	    	}

	        Toast.makeText(getApplicationContext(), text, 
	        Toast.LENGTH_LONG).show();
	        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	    }
	public String getContactName(String incomingNumber) {
		// TODO Auto-generated method stub
		String name = null;
		String contactId = null;

		// define the columns the query returns
		String[] projection = new String[] {
		        ContactsContract.PhoneLookup.DISPLAY_NAME,
		        ContactsContract.PhoneLookup._ID};

		// encode the phone number and build the filter URI
		Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));

		// query time
		Cursor cursor = this.getContentResolver().query(contactUri, projection, null, null, null);

		if (cursor.moveToFirst()) {

		    // Get values from contacts database:
		    contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
		    name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
 
		    return name;
		} else {

		    Log.v("ffnet", "Started uploadcontactphoto: Contact Not Found @ " + incomingNumber);
		    return incomingNumber; // contact not found
		}
	}
	
}