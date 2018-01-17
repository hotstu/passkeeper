package hotstu.github.passkeeper;

import android.app.Application;

/**
 * hold variables
 * @author Owner
 *
 */
public class PassKeepApp extends Application {
	private static final String salt = "mh2ngbbfdsx4";
	/** the receiver when backup using email **/
	private static final String backupemailaddr = "xxxxx";
	private String key = null;
	private boolean stale = true;
    public static PassKeepApp sInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
	}

	public String getSalt() {
		return salt;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
		this.stale = false;
	}

	public boolean isStale() {
		return stale;
	}

	public String getEmail() {
		return backupemailaddr;
	}


}
