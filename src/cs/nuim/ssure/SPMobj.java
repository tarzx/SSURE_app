/************************************************************
File Name: SPMobj.java
Unused class.
This file is define the detail of SPM Object which is used to pass in from readSong to playsong thread.


Version		Date		Name		Description
------------------------------------------------------------
1			11/06/2014	Patomporn	Create new
/***********************************************************/

package cs.nuim.ssure;

/**
 * Detail for SPM Object
 * @author Patomporn
 *
 */
public class SPMobj {
	private int id;
	private double SPM;
	private int BPM;
	private SongList song;
	
	public SPMobj(int _id, double _SPM) {
		this.id = _id;
		this.SPM = _SPM;
		this.BPM = 0;
	}
	
	public void setBPM(int _BPM) { this.BPM = _BPM; }
	public void setSong(SongList _song) { this.song = _song; }
	
	
	public int getId() { return this.id; }
	public double getSPM() { return this.SPM; }
	public int getBPM() { return this.BPM; }
	public SongList getSong() { return this.song; }
	
}