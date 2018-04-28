/************************************************************
File Name: playingSong.java
This file is to create a playingsong thread which run along the UI thread.


Version		Date		Name		Description
------------------------------------------------------------
1			10/06/2014	Patomporn	Create new
/***********************************************************/

package cs.nuim.ssure;

import java.io.FileDescriptor;
import java.util.LinkedList;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.MediaStore.Audio;
import android.util.Log;
 
/**
 * Plays a series of audio files.
 */
public class playingSong {
	private static final int SAMPLE_RATE = 44100;
    private static final int PLAY = 1;
    private static final int STOP = 2;
    private static final int PAUSE = 3;
    private static final boolean mDebug = false;
    
    private AudioTrack at;
    private boolean isPlay = true;
 
    /**
     * Private class : Command to control thread
     * @author Patomporn
     *
     */
    private static final class Command {
        int code;
        Context context;
        FileDescriptor uri;
        boolean looping;
        int stream = AudioManager.STREAM_MUSIC;
        AssetManager am;
        SongList song;
        dataHelper dHelper;
        String path;
        Handler mHandler;
 
        public String toString() {
            return "{ code=" + code + " looping=" + looping + " stream=" + stream
                    + " uri=" + uri + " }";
        }
    }
 
    //Command Queue to control thread
    private LinkedList<Command> mCmdQueue = new LinkedList();
   
    //Static List to pass the SPM Object from readSong thread
    public static LinkedList<SPMobj> mSongObjects = new LinkedList();
    
    /**
     * Read selected sound 
     * @param cmd : Track control information
     */
    private void readSound(final Command cmd) {
    	
    	new Thread(new Runnable() {
		    public void run() {	
		    	int intSize = android.media.AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
		    	AudioFormat.ENCODING_PCM_16BIT); 
		        at = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
		        AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM); 
		
				at.play();
		        Log.d(mTag, "before main while");
		     
		        byte[] bytes = readSong.readByte(cmd.am, cmd.song.getPath());
		        
				SPMobj mSPMobj = new SPMobj(-1, 0);
				while (isPlay) {
			            Log.d(mTag, "number of addBPM = "+mSongObjects.size());
				    	
				    	if (mSongObjects.size()!=0) { 
				    		mSPMobj = mSongObjects.getFirst();
				    		Message myMessage = new Message();
				            myMessage.obj = mSPMobj;
				            cmd.mHandler.sendMessage(myMessage);
				            
				            Log.d(mTag, "before while");
				    	
				    		try {
					            Log.d(mTag, "Start playing from addBPM");
					            if (mDebug) Log.d(mTag, "Starting playback");

					            Log.d(mTag, "Read Byte Start");
					            bytes = readSong.readByte(cmd.am, (mSPMobj.getSong()).getPath());
					            Log.d(mTag, "Read Byte End");
					            
		            		    // Write the byte array to the track
		            		    at.write(bytes, 0, bytes.length); 
		            		
		            		    mSongObjects.removeFirst();		            		    
		            		    cmd.song = mSPMobj.getSong();
					        }
					        catch (Exception e) {
					            Log.w(mTag, "error loading sound for " + cmd.uri, e);
					        }
					    	
				    	} else {
				    		mSPMobj.setBPM(cmd.song.getBPM());
				    		Message myMessage = new Message();
				            myMessage.obj = mSPMobj;
				            cmd.mHandler.sendMessage(myMessage);
				    		
				    		try {
					            Log.d(mTag, "Start playing from default");
					            if (mDebug) Log.d(mTag, "Starting playback");
					            
		            		    at.write(bytes, 0, bytes.length); 
					        }
					        catch (Exception e) {
					            Log.w(mTag, "error loading sound for " + cmd.uri, e);
					        }
				    	}
			            
		    	}		 
				
				at.stop();
			    at.release();
			    
			    Log.d(mTag, "play dead thread");
			    return;
    		}
    	}).start();
    }

    /**
     * private class to control thread
     * @author Patomporn
     *
     */
    private final class ControlThread extends java.lang.Thread {
    	ControlThread() {
            super("AsyncPlayer-" + mTag);
            Log.d(mTag, "control thread created");
        }
 
        public void run() {
            Log.d(mTag, "control thread started");
            while (true) {
                Command cmd = null;
 
                synchronized (mCmdQueue) {
                    if (mDebug) Log.d(mTag, "RemoveFirst");
                    cmd = mCmdQueue.removeFirst();
                }
 
                switch (cmd.code) {
                case PLAY:
                    if (mDebug) Log.d(mTag, "READ");
                    readSound(cmd);
                    isPlay = true;
                    break;
                case PAUSE:
                    if (mDebug) Log.d(mTag, "STOP");
                    pauseSound();
                    isPlay = false;
                    break;
	            case STOP:
	                if (mDebug) Log.d(mTag, "STOP");
	                stopSound();
	                isPlay = false;
	                break;
	            }
                Log.d(mTag, "mCmdQueue = " + mCmdQueue.size());
 
                synchronized (mCmdQueue) {
                    if (mCmdQueue.size() == 0) {
                        Log.d(mTag, "dead thread");
                        
                        mThread = null;
                        releaseWakeLock();
                        return;
                    }
                }
            }
        }

		private void pauseSound() {
			at.pause();
			at.flush();
		}
		
		private void stopSound() {
			if (at.getPlayState()==AudioTrack.PLAYSTATE_PLAYING) {
				at.pause();
				at.flush();
			}
			
			mSongObjects.clear();

		}
    }
 
    private String mTag;
    private Thread mThread;
    private PowerManager.WakeLock mWakeLock;
    private int mState = STOP; //initial state
 
    /**
     * Construct an AsyncPlayer object.
     *
     * @param tag a string to use for debugging
     */
    public playingSong(String tag) {
        if (tag != null) {
            mTag = tag;
        } else {
            mTag = "AsyncPlayer";
        }
    }
    
    /**
     * Start playing the sound.
     *  
     * @param c - Context
     * @param am - AssestManager to access the file
     * @param s - Selected Song detail
     * @param dHelper - dataHelper to access Database
     * @param mHandler - Handler to send message back to UI Thread
     */
    public void play(Context c, AssetManager am, SongList s, dataHelper dHelper, Handler mHandler) {
        Command cmd = new Command();
        cmd.code = PLAY;
        cmd.context = c;
        cmd.am = am;
        cmd.song = s;
        cmd.dHelper = dHelper;
        cmd.mHandler = mHandler;
        synchronized (mCmdQueue) {
            enqueueLocked(cmd);
            mState = PLAY;
        }
    }
     
    /**
     * Stop a previously played sound.  It can't be played again or unpaused at this point.  
     * Calling this multiple times has no ill effects.
     */
    public void stop() {
    	Log.d(mTag, "STOP called from activity, mstate = "+mState);
        synchronized (mCmdQueue) {
            // This check allows stop to be called multiple times without starting
            // a thread that ends up doing nothing.
            if (mState != STOP) {
                Command cmd = new Command();
                cmd.code = STOP;
                enqueueLocked(cmd);
                mState = STOP;
            }
        }
    }
   
    /**
     * Pause a previously played sound.  It can be played again at this point.  
     * Calling this multiple times has no ill effects.
     */
    public void pause() {
    	Log.d(mTag, "PAUSE called from activity, mstate = "+mState);
        synchronized (mCmdQueue) {
            // This check allows stop to be called multiple times without starting
            // a thread that ends up doing nothing.
            if (mState != PAUSE) {
                Command cmd = new Command();
                cmd.code = PAUSE;
                enqueueLocked(cmd);
                mState = PAUSE;
            }
        }
    }
 
    /**
     * set Queue for input command
     * @param cmd - Command
     */
    private void enqueueLocked(Command cmd) {
        mCmdQueue.add(cmd);
        if (mThread == null) {
            acquireWakeLock();
            mThread = new ControlThread();
            mThread.start();
        }
    }
 
    /**
     * set WakeLock
     */
    private void acquireWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }
 
    /**
     * Release WakeLock
     */
    private void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }
}
