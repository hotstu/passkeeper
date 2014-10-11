package hotstu.github.passkeeper;

import hotstu.github.passkeeper.model.Host;
import hotstu.github.passkeeper.model.User;

import java.util.ArrayList;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @host |_id |hostname|
 * @user |_id |username|pwdlenth|hostid|
 * 
 */
public class DBHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "passkeeper.db";
	public static final String TABLE_HOST = "hosts";
	public static final String TABLE_NAME = "users";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE_HOST = "CREATE TABLE IF NOT EXISTS hosts"
				+ "(_id INTEGER PRIMARY KEY,hostname TEXT)";
		db.execSQL(CREATE_TABLE_HOST);
		String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS users"
				+ "(_id INTEGER PRIMARY KEY,username TEXT,pwdlenth INTEGER,hostid INTEGER)";
		db.execSQL(CREATE_TABLE_USER);
		String CREATE_TABLE_MASTER = "CREATE TABLE IF NOT EXISTS master"
				+ "(_id INTEGER PRIMARY KEY,hash TEXT)";
		db.execSQL(CREATE_TABLE_MASTER);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @param host
	 * @return true when insert success,or false when failed
	 */
	public boolean addHost(Host host) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("hostname", host.getHostname());
		long result = db.insert(TABLE_HOST, null, values);
		db.close();
		if (result >= 0)
			return true;
		else
			return false;
	}

	public boolean addUser(User user) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("username", user.getUsername());
		values.put("pwdlenth", user.getPwdLength());
		values.put("hostid", user.getHostId());
		long result = db.insert(TABLE_NAME, null, values);
		db.close();
		if (result >= 0)
			return true;
		else
			return false;
	}

	/**
	 * 
	 * @param user
	 * @return the number of rows affected 
	 */
	public int delUser(User user) {
		SQLiteDatabase db = this.getWritableDatabase();
		int count = db.delete(TABLE_NAME, "_id=?", new String[]{String.valueOf(user.get_id())});
		db.close();
		return count;
	}
	
	public int delHost(Host host){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, "hostid=?", new String[]{String.valueOf(host.get_id())});
		int count = db.delete(TABLE_HOST, "_id=?", new String[]{String.valueOf(host.get_id())});
		db.close();
		return count;
	}

	public ArrayList<Host> queryAllHosts() {
		ArrayList<Host> list = new ArrayList<Host>();
		// Select All Query
		String selectQuery = "SELECT _id,hostname FROM hosts";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				list.add(new Host(cursor.getInt(0), cursor.getString(1)));
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();

		return list;
	}

	public ArrayList<User> queryAllUsers() {
		ArrayList<User> list = new ArrayList<User>();
		// Select All Query
		String selectQuery = "SELECT _id,username,pwdlenth,hostid FROM users";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				list.add(new User(cursor.getInt(0), cursor.getString(1), cursor
						.getInt(2), cursor.getInt(3)));
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();

		return list;
	}

	public String checkHash(){
		SQLiteDatabase db = getReadableDatabase();
		String result = "";
		Cursor cursor = db.query("master", new String[] {"hash"}, "_id=?", new String[] {"1"}, null, null, null);
	    if (cursor.moveToFirst()){
	    	result = cursor.getString(0);
	    }
	    cursor.close();
	    db.close();
	    return result;
	}
	
	public boolean addHash(String hash){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("hash", hash);
		long result = db.insert("master", null, values);
		db.close();
		if (result >= 0)
			return true;
		else
			return false;
	}
}









