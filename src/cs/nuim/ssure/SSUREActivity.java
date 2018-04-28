/************************************************************
File Name: SSUREActivity.java
MainActivity in Android Application.

Version		Date		Name		Description
------------------------------------------------------------
1			15/06/2014	Patomporn	Create new
/***********************************************************/

package cs.nuim.ssure;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import cs.nuim.ShotTimeLomb;

public class SSUREActivity extends ActionBarActivity implements SensorEventListener {
	private AssetManager am;
	private static SongList soli;
	private SongList song;
	        		
	private boolean mInitialized; 
	private SensorManager mSensorManager; 
	private Sensor mAccelerometer; 
	private boolean SaveMode = false;
	private dataHelper dataHelper;
	private int idA = 0, buf = 0;
	private double prevSPM = -1;
	
	//Data for Test
	/*private double[] test = {188, 186, 184, 182, 180, 178, 176, 174, 172, 170,
							 168, 166, 164, 162, 160, 158, 156, 154, 152, 150,
							 148, 146, 144, 142, 140, 138, 136, 134, 132, 130,
							 128, 126, 124, 122, 120, 118, 116, 114, 112, 110,
							 108, 106, 104, 102, 100, 98, 96, 94, 92, 90,
							 88, 86, 84, 82, 80, 78, 76, 74, 72, 70,
							 68, 66, 64, 62, 60, 58, 56, 54, 52, 50
							};*/
	
	//Show Current SPM
	private Handler mHandler = new Handler() {
		@Override
	    public void handleMessage(Message msg) {
	        // Code to process the response and update UI.
			if (SaveMode) {
				if (msg.obj != null) {
					SPMobj curSPM = (SPMobj) msg.obj;
					
					TextView SPM= (TextView) findViewById(R.id.TextSPM);
					if (curSPM.getSPM() >=50 && curSPM.getSPM() <=200) {
						SPM.setTextColor(Color.GREEN);
					} else {
						SPM.setTextColor(Color.YELLOW);
					}
					
					SPM.setText(String.format("%.2f", curSPM.getSPM()) + " SPM");
				}
			}
	    }
	};
	
	//Show Current song's BPM
	private Handler mHandler2 = new Handler() {
		@Override
	    public void handleMessage(Message msg) {
	        // Code to process the response and update UI.
			if (msg.obj != null) {
				SPMobj curSPM = (SPMobj) msg.obj;
				
				String l_BPM = "--", l_SPM = "--";
				if (curSPM.getSPM() != 0) l_SPM = String.format("%.2f", curSPM.getSPM());
				if (curSPM.getBPM() != 0) l_BPM = String.valueOf(curSPM.getBPM());
				
				TextView BPM= (TextView) findViewById(R.id.TextBPM);
				BPM.setText("Song: " + l_BPM + " BPM on " + l_SPM + " SPM ");
			}
	    }
	};
	
	// for Lomb-Periodogram
	private static int framesize = 512;
	private static int hopsize = 120;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ssure);
		
		am = getAssets();
		song = soli.BPM120_100;
		
		mInitialized = false;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		
		dataHelper = new dataHelper(getApplicationContext());
        			
		final playingSong ap = new playingSong("PlaySong");
		final ImageButton BStart= (ImageButton) findViewById(R.id.but_start); 
        final ImageButton BPause= (ImageButton) findViewById(R.id.but_pause);  
        final ImageButton BStop= (ImageButton) findViewById(R.id.but_stop);
        
        BStart.setVisibility(Button.VISIBLE);
        BStart.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		SaveMode = true;
        		Log.d("SPM", "Start");
        		if (idA==0) dataHelper.reset();
        		
        		BStart.setVisibility(Button.INVISIBLE);
        		BPause.setVisibility(Button.VISIBLE);
        		BStop.setVisibility(Button.VISIBLE);
        		
        		try {        		
	        		ap.play(getApplicationContext(), am, song, dataHelper, mHandler2);
        		} catch (Exception e) {
        			Log.e("UI", e.getMessage());
        		}
        	}
        });
        BPause.setVisibility(Button.INVISIBLE);
        BPause.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		SaveMode = false;
        		
        		BStart.setVisibility(Button.VISIBLE);
        		BPause.setVisibility(Button.INVISIBLE);
        		
        		try {        		
	        		ap.pause();
        		} catch (Exception e) {
        			Log.e("UI", e.getMessage());
        		}
        	}
        });
        BStop.setVisibility(Button.VISIBLE);
        BStop.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		SaveMode = false;
        		
        		try {        		
	        		ap.stop();
        		} catch (Exception e) {
        			Log.e("UI", e.getMessage());
        		}
        		
        		TextView SPM= (TextView) findViewById(R.id.TextSPM);
        		SPM.setTextColor(Color.RED);
    			SPM.setText("-- SPM");
    			
    			TextView BPM= (TextView) findViewById(R.id.TextBPM);
        		BPM.setText("Song: -- BPM on -- SPM ");
        		
        		idA=0;
        		buf=0;
        		dataHelper.reset();
        		playingSong.mSongObjects.clear();
        		
        		BStart.setVisibility(Button.VISIBLE);
        		BPause.setVisibility(Button.INVISIBLE);
        	}
        });
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		final float AY;
		if (mInitialized) { 
			AY = 0; 
			mInitialized = false;
		} else { AY = event.values[0]; }
		
		Sensor sensor = event.sensor;  	
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {   
    		final int id = idA++;
    		    if (SaveMode) {
    			Thread t = new Thread(){
    		        public void run(){
    		            // Long time comsuming operation
        				dataHelper.insertDataAY(id, AY, System.currentTimeMillis());
    		        	
    		        	if (id>=framesize && id%hopsize==0) bufData(id);
    		        	return;
    		        }
    		    };
    		    t.start();			
    		}
        }
	}
	
	private void bufData(int id) {
		double[][] frame = dataHelper.getDataAY(id-framesize, id);
		
		if (frame!=null) {
			//time
			for (int i=0; i<frame[2].length; i++) frame[2][i] *= (0.001*Math.pow(60,-1));

			double[][] Lomb = ShotTimeLomb.doShotLomb(frame[1], frame[2]);
			double MaxFreq = ShotTimeLomb.findMaxFreq(Lomb[0], Lomb[1]);
			Log.d("SPM", "Lomb");
			
			Message myMessage = new Message();
            SPMobj res = new SPMobj(0, MaxFreq);
            myMessage.obj = res;
            mHandler.sendMessage(myMessage);
            
			//if SPM isn't in this range, we consider it as noise.
			final double curSPM = (MaxFreq >=50 && MaxFreq <=200) ? MaxFreq : 0; //test[(buf)%test.length]; //prevSPM;
			
			if (curSPM>0) {
				final int buff = buf++;
    		    Thread ts = new Thread(){
    		        public void run(){
    		        	// Long time comsuming operation
        				Log.d("SPM", "Lomb-OK");
						dataHelper.insertDataSPM(buff, curSPM);
						
    		        	return;
    		        }
    		    };
    		    ts.start();	
    		    
    		    Log.d("SPM", "addBPM");
				readSong.chooseSong(new SPMobj(buff, curSPM), dataHelper);
			}
		}

		dataHelper.clearAY(id-framesize, id-framesize+hopsize);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ssure, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
