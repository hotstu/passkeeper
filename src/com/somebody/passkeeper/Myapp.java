package com.somebody.passkeeper;

import android.app.Application;

/**
 * hold variables
 * @author Owner
 *
 */
public class Myapp extends Application {
	private static final String salt = "mh2ngbbfdsx4";
	/** the receiver when backup using email **/
	private static final String backupemailaddr = "xxxxx";
	private String key = null;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public String getSalt() {
		return salt;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getEmail() {
		return backupemailaddr;
	}
	

	

}
