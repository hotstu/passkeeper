package hotstu.github.passkeeper;

import hotstu.github.passkeeper.db.AppDatabase;

/**
 * @author hglf
 * @since 2018/1/16
 */
public class Injection {
    public static AppDatabase getDataBase() {
        return AppDatabase.getInMemoryDatabase(PassKeepApp.sInstance);
    }
}
