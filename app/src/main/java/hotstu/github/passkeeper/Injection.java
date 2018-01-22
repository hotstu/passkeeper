package hotstu.github.passkeeper;

import hotstu.github.passkeeper.db.AppDatabase;
import hotstu.github.passkeeper.viewmodel.ListViewModelFactory;
import hotstu.github.passkeeper.viewmodel.ViewModelFactory;

/**
 * @author hglf
 * @since 2018/1/16
 */
public class Injection {
    public static AppDatabase getDataBase() {
        return AppDatabase.getInMemoryDatabase(PassKeepApp.sInstance);
    }

    public static PassKeepApp getApplicaitonContext() {
        return PassKeepApp.sInstance;
    }

    public static ViewModelFactory getViewModelFactory() {
        return ViewModelFactory.getInstance(getApplicaitonContext());
    }

    public static ListViewModelFactory getViewModelFactory(ListActivity activity) {
        return new ListViewModelFactory(getApplicaitonContext(), getDataBase(), activity);
    }
}
