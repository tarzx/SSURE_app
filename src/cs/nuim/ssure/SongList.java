/************************************************************
File Name: SongList.java
This file is collect the detail of each audio file using enum.


Version		Date		Name		Description
------------------------------------------------------------
1			14/06/2014	Patomporn	Create new
/***********************************************************/

package cs.nuim.ssure;

import android.content.res.AssetManager;

public enum SongList {
	BPM60_090("60 BPM", "60_090.wav", 44100, 60),
	BPM60_095("60 BPM", "60_095.wav", 44100, 60),
	BPM60_100("60 BPM", "60_100.wav", 44100, 60),
	BPM60_105("60 BPM", "60_105.wav", 44100, 60),
	BPM60_110("60 BPM", "60_110.wav", 44100, 60),
    BPM80_090("80 BPM", "80_090.wav", 44100, 80),
    BPM80_095("80 BPM", "80_095.wav", 44100, 80),
    BPM80_100("80 BPM", "80_100.wav", 44100, 80),
    BPM80_105("80 BPM", "80_105.wav", 44100, 80),
    BPM80_110("80 BPM", "80_110.wav", 44100, 80),
    BPM100_090("100 BPM", "100_090.wav", 44100, 100),
    BPM100_095("100 BPM", "100_095.wav", 44100, 100),
    BPM100_100("100 BPM", "100_100.wav", 44100, 100),
    BPM100_105("100 BPM", "100_105.wav", 44100, 100),
    BPM100_110("100 BPM", "100_110.wav", 44100, 100),
    BPM120_090("120 BPM", "120_090.wav", 44100, 120),
    BPM120_095("120 BPM", "120_095.wav", 44100, 120),
    BPM120_100("120 BPM", "120_100.wav", 44100, 120),
    BPM120_105("120 BPM", "120_105.wav", 44100, 120),
    BPM120_110("120 BPM", "120_110.wav", 44100, 120),
    BPM140_090("140 BPM", "140_090.wav", 44100, 140),
    BPM140_095("140 BPM", "140_095.wav", 44100, 140),
    BPM140_100("140 BPM", "140_100.wav", 44100, 140),
    BPM140_105("140 BPM", "140_105.wav", 44100, 140),
    BPM140_110("140 BPM", "140_110.wav", 44100, 140),
    BPM160_090("160 BPM", "160_090.wav", 44100, 160),
    BPM160_095("160 BPM", "160_095.wav", 44100, 160),
    BPM160_100("160 BPM", "160_100.wav", 44100, 160),
    BPM160_105("160 BPM", "160_105.wav", 44100, 160),
    BPM160_110("160 BPM", "160_110.wav", 44100, 160),
    BPM180_090("180 BPM", "180_090.wav", 44100, 180),
    BPM180_095("180 BPM", "180_095.wav", 44100, 180),
    BPM180_100("180 BPM", "180_100.wav", 44100, 180),
    BPM180_105("180 BPM", "180_105.wav", 44100, 180),
    BPM180_110("180 BPM", "180_110.wav", 44100, 180);

    private String name;
    private String path;
    private int rate;
    private int BPM;
    private byte[] bytes = null;
    private SongList(String _name, String _path, int _rate, int _BPM) {
        name = _name;
        path = _path;
        rate = _rate;
        BPM = _BPM;
    }

    public String getName() {
        return name;
    }
    public String getPath() {
        return path;
    }
    public int getRate() {
    	return rate;
    }
    public int getBPM() {
        return BPM;
    }
    public void setByte(byte[] _bytes) {
    	this.bytes = _bytes;
    }

    public static SongList getNormalBPM(int BPM) {
    	if (BPM==60) return BPM60_100;
    	else if (BPM==80) return BPM80_100;
    	else if (BPM==100) return BPM100_100;
    	else if (BPM==120) return BPM120_100;
    	else if (BPM==140) return BPM140_100;
    	else if (BPM==160) return BPM160_100;
    	else return BPM180_100;
    }
}
