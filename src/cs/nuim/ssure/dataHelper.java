/************************************************************
File Name: dataHelper.java
This file is manage the internal database on mobile.
Table: SSURE_ACC_SENSOR - to collect the data from sensor and used used a buffer
Table: SSURE_SPM_BPM_DATA - to collect the analysed data (SPM) matching to the tempo of song (BPM)

Version		Date		Name		Description
------------------------------------------------------------
1			10/06/2014	Patomporn	Create new
/***********************************************************/

package cs.nuim.ssure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class dataHelper extends SQLiteOpenHelper {

	static final String TABLE_NAME_A = "SSURE_ACC_SENSOR";
	static final String TABLE_NAME_SPM_BPM = "SSURE_SPM_BPM_DATA";
	static final String COL_ID = "_ID";
	static final String COL_AY = "AY_VAL";
	static final String COL_TIMESTAMP = "TIMESTAMP";
	static final String COL_SPM = "SPM_VAL";
	static final String COL_BPM = "BPM_VAL";
	
	private static final int DATABASE_VERSION = 1;	
	private static final String DATABASE_NAME = "SSURE_DATA.db";
	private static final String DEBUG_TAG = "dataHelper";
	private static final String DB_SCHEMA_A = "CREATE TABLE " + TABLE_NAME_A + "("
	        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
	        + COL_AY + " REAL, "
			+ COL_TIMESTAMP + " INTEGER NOT NULL " + "); ";
	private static final String DB_SCHEMA_SPM_BPM = "CREATE TABLE " + TABLE_NAME_SPM_BPM + "("
	        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
	        + COL_SPM + " REAL, "
	        + COL_BPM + " INTEGER " + "); ";
	
	private SQLiteDatabase db;

    dataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        open();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_SCHEMA_A);
        Log.d("Database", "Acc Created");     
        db.execSQL(DB_SCHEMA_SPM_BPM);
        Log.d("Database", "SPM_BPM Created");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DEBUG_TAG,
                "Warning: Dropping all tables; data migration not supported");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_A);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SPM_BPM);
        onCreate(db);
    }
    
    // Open Database 
	public void open() {
		db = getWritableDatabase();	
	}
	
	// Close Database 
    public void close() {
        if (db != null) db.close();
    }
    
    /**
     * Insert Captured data to Database
     * @param id - unique ID
     * @param AY - longitudinal accelerometer data
     * @param time - current time
     */
    public void insertDataAY(int id, float AY, long time) {
    	if (db!= null) {
    		//Log.d("Database", "Opened");
	        ContentValues values = new ContentValues();
	        values.put(COL_ID, id);
	        values.put(COL_AY, AY);
	        values.put(COL_TIMESTAMP, time);
	        db.insert(TABLE_NAME_A, null, values);
    	} else { Log.e("Database", "Acce Error open"); }
    }
    
    /**
     * Get sensor data
     * @param id_s - start ID
     * @param id_e - end ID
     * @return 2D Array of data ([0]:ID, [1]:longitudinal accelerometer data, [2]:timestamp)
     */
    public double[][] getDataAY(int id_s, int id_e) {
    	double[][] res = null;
    	if (db!= null) {
    		//Log.d("Database", "Opened");
    		Cursor c = db.rawQuery(" SELECT " + COL_ID + ", " + COL_AY + ", " + COL_TIMESTAMP +
    							   " FROM " + TABLE_NAME_A + " WHERE " + COL_ID + " BETWEEN " + id_s + " AND " + id_e + 
    							   " ORDER BY " + COL_TIMESTAMP + ";", null);
    	    if (c.moveToFirst()) {
    	    	//Log.e("Database", "Cursor");
    	    	List<Double> IDlist = new ArrayList<Double>();
    	    	List<Double> AYlist = new ArrayList<Double>();
    	    	List<Double> TSlist = new ArrayList<Double>();
    	    	int size = 0;
    	    	
                do {
                	IDlist.add(c.getDouble(0));
                	AYlist.add(c.getDouble(1));
                	TSlist.add(c.getDouble(2));
                	size++;
                } while (c.moveToNext());
                //Log.d("Database", "End-Loop-Do");
                
                res = new double[3][size];
                for (int i=0; i<size; i++) {
                	res[0][i] = (Double) (IDlist.get(i));
                	res[1][i] = (Double) (AYlist.get(i));
                	res[2][i] = (Double) (TSlist.get(i));
                }
            }
    	}
    	//Log.d("Database", "Return");
        return res;
    }
    
    /**
     * Clear the data that has already processed
     * @param id_s - start ID
     * @param id_e - end ID
     */
    public void clearAY(int id_s, int id_e) {
    	if (db!= null) {
    		//Log.d("Database", "Opened");
    		db.delete(TABLE_NAME_A,new String(COL_ID + " BETWEEN ? AND ?"),new String[]{String.valueOf(id_s),String.valueOf(id_e)});
    		//Log.d("Database", "Clear");
    	}
    }
    
    /**
     * Insert analysed data (SPM) to Database
     * @param id - unique ID
     * @param SPM - SPM value
     */
    public void insertDataSPM(int id, double SPM) {
    	if (db!= null) {
    		//Log.d("Database", "Opened");
	        ContentValues values = new ContentValues();
	        values.put(COL_ID, id);
	        values.put(COL_SPM, SPM);
	        db.insert(TABLE_NAME_SPM_BPM, null, values);
    	} else { Log.e("Database", "SPM Error open"); }
    }
    
    /**
     * get SPM data
     * @param id - unique ID
     * @return - List of SPM Object
     */
    public List<SPMobj> getDataSPM(int id) {
    	List<SPMobj> list = new ArrayList();
    	if (db!= null) {
    		//Log.d("Database", "Opened");
    		Cursor c = db.rawQuery(" SELECT " + COL_ID + ", " + COL_SPM +
    							   " FROM " + TABLE_NAME_SPM_BPM + " WHERE " + COL_ID + " = " + id + ";", null);
    	    if (c.moveToFirst()) {
    	    	//Log.d("Database", "Cursor");
                do {
                	SPMobj SPMtemp = new SPMobj(c.getInt(0), c.getDouble(1));
                	list.add(SPMtemp);
                } while (c.moveToNext());
                //Log.d("Database", "End-Loop-Do");
            }
    	}
    	//Log.d("Database", "Return");
        return list;
    }
    
    /**
     * get the data that hasn't be processed with Phase Vocoder
     * @return  - List of SPM Object
     */
    public List<SPMobj> getDataBlankBPM() {
    	List<SPMobj> list = null;
    	if (db!= null) {
    		//Log.d("Database", "Opened");
    		Cursor c = db.rawQuery(" SELECT " + COL_ID + ", " + COL_SPM +
    							   " FROM " + TABLE_NAME_SPM_BPM + " WHERE " + COL_BPM + " IS NULL" +
    							   " ORDER BY " + COL_ID + ";", null);
    		if (c.moveToFirst()) {
    	    	//Log.d("Database", "Cursor-SPM");
    			list = new ArrayList();
                do {
                	SPMobj SPMtemp = new SPMobj(c.getInt(0), c.getDouble(1));
                	list.add(SPMtemp);
                } while (c.moveToNext());
                //Log.d("Database", "End-Loop-Do");
            }
    	}
    	//Log.d("Database", "Return");
        return list;
    }
    
    /**
     * update synchronised BPM value to SPM record
     * @param id - unique ID
     * @param BPM - BPM value
     */
    public void updateBPM(int id, int BPM) {
    	if (db!= null) {
	        ContentValues args = new ContentValues();
	        args.put(COL_BPM, BPM);
	        db.update(TABLE_NAME_SPM_BPM, args, COL_ID + "=" + id, null);
	        Log.d("Database", "Update-BPM");
    	}
    }
    
    /**
     * Clear All SPM data
     */
    public void clearSPM() {
    	if (db!= null) {
    		//Log.d("Database", "Opened");
    		db.delete(TABLE_NAME_SPM_BPM, null, null);
    		//Log.d("Database", "Clear");
    	}
    }
    
    /**
     * Reset Database
     */
    public void reset() {
    	db.delete(TABLE_NAME_A, null, null);
    	db.delete(TABLE_NAME_SPM_BPM, null, null);
    }
    
    //--- Unused methods
    /*public List<SPMobj> getDataSPMAll() {
    	List<SPMobj> list = null;
    	if (db!= null) {
    		//Log.d("Database", "Opened");
    		Cursor c = db.rawQuery(" SELECT " + COL_ID + ", " + COL_SPM +
    							   " FROM " + TABLE_NAME_SPM_BPM + ";", null);
    	    if (c.moveToFirst()) {
    	    	//Log.d("Database", "Cursor");
    	    	list = new ArrayList();
                do {
                	SPMobj SPMtemp = new SPMobj(c.getInt(0), c.getDouble(1));
                	list.add(SPMtemp);
                } while (c.moveToNext());
                //Log.d("Database", "End-Loop-Do");
            }
    	}
    	//Log.d("Database", "Return");
        return list;
    }
    
    public SPMobj getDataFirstBlankBPM() {
    	SPMobj res = null;
    	if (db!= null) {
    		//Log.d("Database", "Opened");
    		Cursor c = db.rawQuery(" SELECT " + COL_ID + ", " + COL_SPM +
    							   " FROM " + TABLE_NAME_SPM_BPM + " WHERE " + COL_BPM + " IS NULL" +
    							   " ORDER BY " + COL_ID + ";", null);
    		if (c.moveToFirst()) {
    	    	//Log.d("Database", "Cursor-SPM");
            	res = new SPMobj(c.getInt(0), c.getDouble(1));
                //Log.d("Database", "End-Loop-Do");
            }
    	}
    	//Log.d("Database", "Return");
        return res;
    }*/

}
