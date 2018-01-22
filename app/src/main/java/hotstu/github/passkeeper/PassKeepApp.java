package hotstu.github.passkeeper;

import android.app.Application;

/**
 * hold variables
 *
 * @author Owner
 */
public class PassKeepApp extends Application {
    /**
     * !!!this field must NOT be changed
     **/
    private static final String salt = BuildConfig.SALT;
    /**
     * the receiver when backup using email
     **/
    private static final String backupemailaddr = BuildConfig.EMAIL;
    public static PassKeepApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public String getSalt() {
        return salt;
    }

    public String getEmail() {
        return backupemailaddr;
    }


}
