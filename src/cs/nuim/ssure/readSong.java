/************************************************************
File Name: readSong.java
This file is to process audio track and select song to play.


Version		Date		Name		Description
------------------------------------------------------------
1			13/06/2014	Patomporn	Create new
/***********************************************************/

package cs.nuim.ssure;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class readSong {
	 private static final double MAX_16_BIT = Short.MAX_VALUE;     // 32,767
	 private static final int SAMPLE_BUFFER_SIZE = 8192;

	 /**
	  * Read audio file from Asset Path
	  * @param am - AssetManager to access the file
	  * @param path - File Path
	  * @return audio signal in term of Array of double 
	  */
	 public static double[] read(AssetManager am, String path) {
		byte[] bytes = new byte[0];
	    try {
	    	InputStream inputStream = am.open(path);
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();				
			byte[] buffer = new byte[1024];
			int read = 0;
			while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
				baos.write(buffer, 0, read);
			}		
			baos.flush();	
			bytes = baos.toByteArray();
	    } catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        Log.e("readSong", e.getMessage());
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        Log.e("readSong", e.getMessage());
	    }
	    
       int N = bytes.length;
       double[] d = new double[N/2];
       for (int i = 0; i < N/2; i++) {
           d[i] = ((short) (((bytes[2*i+1] & 0xFF) << 8) + (bytes[2*i] & 0xFF))) / ((double) MAX_16_BIT);
       }
       return d;
	}
	 
	 /**
	  * Read audio file from Asset Path
	  * @param am - AssetManager to access the file
	  * @param path - File Path
	  * @return audio signal in term of Array of byte 
	  */
	 public static byte[] readByte(AssetManager am, String path) {
			byte[] bytes = new byte[0];
		    try {
		    	InputStream inputStream = am.open(path);
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();				
				byte[] buffer = new byte[1024];
				int read = 0;
				while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
					baos.write(buffer, 0, read);
				}		
				baos.flush();	
				bytes = baos.toByteArray();
		    } catch (FileNotFoundException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		        Log.e("readSong", e.getMessage());
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		        Log.e("readSong", e.getMessage());
		    }
		    
	       return bytes;
		}
	 
	 /**
	  * Read audio file from Path (Temp File)
	  * @param path - File Path
	  * @return audio signal in term of Array of byte 
	  */
	 public static byte[] readByte(String path) {
		byte[] bytes = new byte[0];
	    try {
	    	File file = new File(path);
	    	InputStream inputStream = new FileInputStream(file);
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();				
			byte[] buffer = new byte[1024];
			int read = 0;
			while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
				baos.write(buffer, 0, read);
			}		
			baos.flush();	
			bytes = baos.toByteArray();
	    } catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        Log.e("readSong", e.getMessage());
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        Log.e("readSong", e.getMessage());
	    }
	    
       return bytes;
	}


	 /**
	  * Converse signal in term of Array of Double to Array of byte
	  * @param in - Array of Double
	  * @return - Array of byte
	  */
	public static byte[] converse(double[] in) {
		byte[] byteData = new byte[in.length*2];
		
		for (int i = 0; i<in.length; i++) {
	       // clip if outside [-1, +1]
	       if (in[i] < -1.0) in[i] = -1.0;
	       if (in[i] > +1.0) in[i] = +1.0;

	       // convert to bytes
	       short s = (short) (MAX_16_BIT * in[i]);
	       byteData[2*i] = (byte) s;
	       byteData[2*i+1] = (byte) (s >> 8);   // little Endian
		}
		
		return byteData;
	}
	
	/**
	 * Choose the song from list which match with input SPM, 
	 * then add the Object back to List in PlaySong Thread in order to play
	 * @param SPMin - input SPM object with its detail
	 * @param dHelper - dataHelper to access Database
	 */
	public static void chooseSong(SPMobj SPMin, dataHelper dHelper) {
		SongList tempSong;
		
		if (SPMin!=null) {
			Log.d("ReadSong", "ChooseSong");
			int BPM = 0;
			double ratio;
			if (SPMin.getSPM() < 70) {
				BPM = 60;
				ratio = SPMin.getSPM() * Math.pow(BPM, -1);
				
				if (ratio < 0.90) {
					tempSong = SongList.BPM60_090;
				} else if (ratio < 0.95) {
					tempSong = SongList.BPM60_095;
				} else if (ratio < 1.05) {
					tempSong = SongList.BPM60_100;
				} else if (ratio < 1.1) {
					tempSong = SongList.BPM60_105;
				} else {
					tempSong = SongList.BPM60_110;
				}
				
			} else if (SPMin.getSPM() < 90) {
				BPM = 80;
				ratio = SPMin.getSPM() * Math.pow(BPM, -1);
				
				if (ratio < 0.90) {
					tempSong = SongList.BPM80_090;
				} else if (ratio < 0.95) {
					tempSong = SongList.BPM80_095;
				} else if (ratio < 1.05) {
					tempSong = SongList.BPM80_100;
				} else if (ratio < 1.1) {
					tempSong = SongList.BPM80_105;
				} else {
					tempSong = SongList.BPM80_110;
				}
				
			} else if (SPMin.getSPM() < 110) {
				BPM = 100;
				ratio = SPMin.getSPM() * Math.pow(BPM, -1);
				
				if (ratio < 0.90) {
					tempSong = SongList.BPM100_090;
				} else if (ratio < 0.95) {
					tempSong = SongList.BPM100_095;
				} else if (ratio < 1.05) {
					tempSong = SongList.BPM100_100;
				} else if (ratio < 1.1) {
					tempSong = SongList.BPM100_105;
				} else {
					tempSong = SongList.BPM100_110;
				}
				
			} else if (SPMin.getSPM() < 130) {
				BPM = 120;
				ratio = SPMin.getSPM() * Math.pow(BPM, -1);
				
				if (ratio < 0.90) {
					tempSong = SongList.BPM120_090;
				} else if (ratio < 0.95) {
					tempSong = SongList.BPM120_095;
				} else if (ratio < 1.05) {
					tempSong = SongList.BPM120_100;
				} else if (ratio < 1.1) {
					tempSong = SongList.BPM120_105;
				} else {
					tempSong = SongList.BPM120_110;
				}
				
			} else if (SPMin.getSPM() < 150) {
				BPM = 140;
				ratio = SPMin.getSPM() * Math.pow(BPM, -1);
				
				if (ratio < 0.90) {
					tempSong = SongList.BPM140_090;
				} else if (ratio < 0.95) {
					tempSong = SongList.BPM140_095;
				} else if (ratio < 1.05) {
					tempSong = SongList.BPM140_100;
				} else if (ratio < 1.1) {
					tempSong = SongList.BPM140_105;
				} else {
					tempSong = SongList.BPM140_110;
				}
				
			} else if (SPMin.getSPM() < 170) {
				BPM = 160;
				ratio = SPMin.getSPM() * Math.pow(BPM, -1);
				
				if (ratio < 0.90) {
					tempSong = SongList.BPM160_090;
				} else if (ratio < 0.95) {
					tempSong = SongList.BPM160_095;
				} else if (ratio < 1.05) {
					tempSong = SongList.BPM160_100;
				} else if (ratio < 1.1) {
					tempSong = SongList.BPM160_105;
				} else {
					tempSong = SongList.BPM160_110;
				}
				
			} else {
				BPM = 180;
				ratio = SPMin.getSPM() * Math.pow(BPM, -1);
				
				if (ratio < 0.90) {
					tempSong = SongList.BPM180_090;
				} else if (ratio < 0.95) {
					tempSong = SongList.BPM180_095;
				} else if (ratio < 1.05) {
					tempSong = SongList.BPM180_100;
				} else if (ratio <1.1) {
					tempSong = SongList.BPM180_105;
				} else {
					tempSong = SongList.BPM180_110;
				}
			}
			
			SPMin.setBPM(BPM);
	        SPMin.setSong(tempSong);
	        playingSong.mSongObjects.add(SPMin);
	        Log.d("readSong", "addBPM");
	        
        	dHelper.updateBPM(SPMin.getId(), BPM);
		}
		
	}
	
	//Cannot be used for real-time response
	/**
	 * Process the audio file using Phase Vocoder using buffer and create temp file to play
	 * @param c - Context
	 * @param am - AssetManager to access the audio file
	 * @param s - Detail of song
	 * @param dHelper - dataHelper to access Database
	 * @return - Path
	 */
	public static String process(Context c, AssetManager am, SongList s, dataHelper dHelper) {
		String tempPath = "";
		
		double[] song = read(am, s.getPath());
		int N = song.length;
		int bufferBlock = (int) Math.ceil(N * Math.pow(SAMPLE_BUFFER_SIZE, -1));
		int bufferSize = 0, bufTime = 0;
		
		ByteArrayOutputStream bufbytes = new ByteArrayOutputStream();	

		try {
			int crow = 0;
			List<SPMobj> row = dHelper.getDataBlankBPM();
			while (bufTime < bufferBlock) {
				
				double[] bufferSong = new double[SAMPLE_BUFFER_SIZE];
				int len = (bufTime == bufferBlock-1 ? N%SAMPLE_BUFFER_SIZE : SAMPLE_BUFFER_SIZE);
				System.arraycopy(song, bufTime * SAMPLE_BUFFER_SIZE, bufferSong, 0, len);
				
				if (row!=null) {
					if (crow < row.size()) { 
						Log.d("ReadSong", "Loop");
						double ratio = s.getBPM() * Math.pow((row.get(crow)).getSPM(), -1);
						
						double[] outSong = PVocoder.doPV(bufferSong, ratio);
						byte[] bytesTemp = converse(outSong);
						
						bufbytes.write(bytesTemp, 0, bytesTemp.length);
						bufferSize += bytesTemp.length;
						
						dHelper.updateBPM((row.get(crow)).getId(), s.getBPM());
						crow++; bufTime++;
					} else {
						crow = 0;
						row = dHelper.getDataBlankBPM();
					}
				} else {
					crow = 0;
					row = dHelper.getDataBlankBPM();
					
					byte[] bytesTemp = converse(bufferSong);
						
					bufbytes.write(bytesTemp, 0, bytesTemp.length);
					bufferSize += bytesTemp.length;
					
					bufTime++;
				}
				
			}
			bufbytes.flush();
			
			byte[] bytes = bufbytes.toByteArray();
		
	        File tempWAV = File.createTempFile("tempSong", ".wav", c.getCacheDir());
	        
			tempWAV.deleteOnExit();
	        FileOutputStream fos = new FileOutputStream(tempWAV);
	        fos.write(bytes);
	        fos.close();
			
	        tempPath = tempWAV.getPath();
		} 
	    catch (IOException ex) 
	    {
	        ex.printStackTrace();
	        Log.e("readSong", ex.getMessage());
	    }
		
		return tempPath;
	}
	
	//Cannot be used for real-time response
	/**
	 * Process the audio file using Phase Vocoder using buffer and create temp file to play
	 * then add the Object back to List in PlaySong Thread in order to play
	 * @param c - Context
	 * @param am - AssetManager to access the audio file
	 * @param s - Detail of song
	 * @param dHelper - dataHelper to access Database
	 * @param SPMin - input SPM Object
	 */
	public static void process2(Context c, AssetManager am, SongList s, dataHelper dHelper, SPMobj SPMin) {
		double[] song = read(am, s.getPath());
		int N = song.length;
		int bufferBlock = (int) Math.ceil(N * Math.pow(SAMPLE_BUFFER_SIZE, -1));
		int bufTime = 0;
		
		ByteArrayOutputStream bufbytes = new ByteArrayOutputStream();	

		try {
			Log.d("PVocoder", "pv-run-start");
			double[] sumSong = new double[0];
			while (bufTime < bufferBlock) {
				
				double[] bufferSong = new double[SAMPLE_BUFFER_SIZE];
				int len = (bufTime == bufferBlock-1 ? N%SAMPLE_BUFFER_SIZE : SAMPLE_BUFFER_SIZE);
				System.arraycopy(song, bufTime * SAMPLE_BUFFER_SIZE, bufferSong, 0, len);
				
				if (SPMin!=null) {
					Log.d("ReadSong", "Loop");
					double ratio = s.getBPM() * Math.pow(SPMin.getSPM(), -1);
					
					double[] outSong = PVocoder.doPV(bufferSong, 1.5);
					
					Log.d("ReadSong", "Last value = "+outSong[outSong.length-1]);
					
					sumSong = PVocoder.append(TrimZero(outSong), sumSong);
					
					bufTime++;
				}
				
				dHelper.updateBPM(SPMin.getId(), s.getBPM());
					
			}
			sumSong = PVocoder.setAmp(sumSong);
			byte[] bytesTemp = converse(sumSong);
			
			bufbytes.write(bytesTemp, 0, bytesTemp.length);
			bufbytes.flush();
			Log.d("PVocoder", "pv-run-end");
			
			byte[] bytes = bufbytes.toByteArray();
		
			File tempWAV = File.createTempFile("tempSong", ".wav", c.getExternalCacheDir());
			tempWAV.deleteOnExit();
	        FileOutputStream fos = new FileOutputStream(tempWAV);
	        fos.write(bytes);
	        fos.close();
        
	        SPMin.setBPM(s.getBPM());
	        playingSong.mSongObjects.add(SPMin);
	        
	        dHelper.updateBPM(SPMin.getId(), s.getBPM());
		} 
	    catch (IOException ex) 
	    {
	        ex.printStackTrace();
	        Log.e("readSong", ex.getMessage());
	    }
	}
	
	//Cannot be used for real-time response
	/**
	 * Process the audio file using Phase Vocoder and create temp file to play
	 * then add the Object back to List in PlaySong Thread in order to play
	 * @param c - Context
	 * @param am - AssetManager to access the audio file
	 * @param s - Detail of song
	 * @param dHelper - dataHelper to access Database
	 * @param SPMin - input SPM Object
	 */
	public static void processSong(Context c, AssetManager am, SongList s, dataHelper dHelper, SPMobj SPMin) {		
		double[] song = read(am, s.getPath());	

		try {		
			if (SPMin!=null) {
				Log.d("PVocoder", "pv-run-start");
				double ratio = s.getBPM() * Math.pow(SPMin.getSPM(), -1);
				
				double[] outSong = PVocoder.doPV(song, 1.05);
				Log.d("PVocoder", "pv-run-end");
				
				byte[] bytes = converse(outSong);
				
				File tempWAV = File.createTempFile("tempSong", ".wav", c.getExternalCacheDir());
				tempWAV.deleteOnExit();
		        FileOutputStream fos = new FileOutputStream(tempWAV);
		        fos.write(bytes);
		        fos.close();
	        
		        SPMin.setBPM(s.getBPM());
		        playingSong.mSongObjects.add(SPMin);
		        
	        	dHelper.updateBPM(SPMin.getId(), s.getBPM());
			}

		} 
	    catch (IOException ex) 
	    {
	        ex.printStackTrace();
	        Log.e("readSong", ex.getMessage());
	    }
	}

	//Unused
	/**
	 * Delete temp File
	 * @param path
	 */
	public static void deleteFile(String path) {
		File file = new File(path);
	    boolean deleted = file.delete();
	    Log.d("readSong","deleted: " + deleted);
	}
	
	//--- Support Method
	/**
	 * Trim zero value at the end of signal
	 * @param inp
	 * @return
	 */
	private static double[] TrimZero(double[] inp) {
		int size = inp.length;
		while (inp[size-1]==0) size--;
		
		double[] out = new double[size];
		System.arraycopy(inp, 0, out, 0, size);
		
		Log.d("readSong", "Before Trim = "+inp.length+" After Trim = "+out.length);
		
		return out;
	}
}
