/************************************************************
File Name: songThread.java
Unused class.
This file is to create songThread which is used to control thread to process Phase Vocoder.


Version		Date		Name		Description
------------------------------------------------------------
1			11/06/2014	Patomporn	Create new
/***********************************************************/

package cs.nuim.ssure;

import java.util.LinkedList;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class songThread extends Thread {
	
	private Context context;
	private AssetManager am;
	private SongList song;
	private dataHelper dHelper;
	private boolean SaveMode;
	
	private static LinkedList<SPMobj> SPMlist = new LinkedList();
	
	songThread(Context _context, AssetManager _am, SongList _song, dataHelper _dHelper, boolean _SaveMode) {
		super("SongThread");
		this.context = _context;
		this.am = _am;
		this.song = _song;
		this.dHelper = _dHelper;
		this.SaveMode = _SaveMode;
	}
	
	public void run() {
        // Long time comsuming operation
		while (SaveMode) {
			//Log.d("songThread", "Number = " + SPMlist.size());
			if (SPMlist.size()!=0) { 
	        	Log.d("songThread", "running");
	        	//readSong.chooseSong(SPMlist.getFirst(), dHelper);
	        	//readSong.processSong(context, am, song, dHelper, SPMlist.getFirst());
	        	//readSong.test(context, am, song.getPath(), song.getRate());
	        	SPMlist.removeFirst();
			}
    	}
		return;
    }
	
	public void setMode(boolean _SaveMode) {
		//Log.d("songThread", "mode");
		this.SaveMode = _SaveMode;
	}

	public static void addSPM(SPMobj SPMtemp) {
		//Log.d("songThread", "SPMlist added");
		SPMlist.add(SPMtemp);
	}
	
	public static void clearSPM() {
		//Log.d("songThread", "SPMlist cleared");
		SPMlist.clear();
	}
}
