package hotstu.github.passkeeper;

import java.io.File;


import android.app.Application;
import android.content.Context;
import android.util.Log;

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
	
	private static final String TAG = "APP";

    public static int DEBUG = -1;

    public static Context CONTEXT;

    public static File APP_FILES_DIR;
    public static File APP_CACHE_DIR;
    public static File APP_EXTERNAL_FILES_DIR;
    public static File APP_EXTERNAL_CACHE_DIR;

	@Override
	public void onCreate() {
		super.onCreate();
		CONTEXT = this;

        APP_FILES_DIR = getFilesDir();
        APP_CACHE_DIR = getCacheDir();
        APP_EXTERNAL_FILES_DIR = CONTEXT.getExternalFilesDir(null);
        APP_EXTERNAL_CACHE_DIR = CONTEXT.getExternalCacheDir();
        Log.d(TAG, "APP_FILES_DIR:"+APP_FILES_DIR);
        Log.d(TAG, "APP_CACHE_DIR:"+APP_CACHE_DIR);
        Log.d(TAG, "APP_EXTERNAL_FILES_DIR:"+APP_EXTERNAL_FILES_DIR);
        Log.d(TAG, "APP_EXTERNAL_CACHE_DIR:"+APP_EXTERNAL_CACHE_DIR);
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
