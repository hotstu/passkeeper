package hotstu.github.passkeeper.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import hotstu.github.passkeeper.PassKeepApp;
import hotstu.github.passkeeper.Util;
import hotstu.github.passkeeper.db.AppDatabase;
import hotstu.github.passkeeper.db.HashEntity;

/**
 * @author hglf
 * @since 2018/1/15
 */
public class WatchDogViewModel extends AndroidViewModel {

    private final AppDatabase db;
    public MutableLiveData<String> input;
    public final SingleLiveEvent<String> loginOkEvent;

    public WatchDogViewModel(@NonNull Application application, AppDatabase db) {
        super(application);
        this.db = db;
        this.input = new MutableLiveData<>();
        this.loginOkEvent = new SingleLiveEvent<>();
    }

    public void login() {
        String inputText = input.getValue();
        if (inputText == null || "".equals(inputText)) {
            return;
        }
        final String hash = db.hashModel().checkHash();
        final String input = Util.md5(PassKeepApp.sInstance.getSalt() + inputText);
        if (hash != null && hash.equals(input)) {
            loginOkEvent.setValue(inputText);
        } else if (hash == null) {
            //the first time running this app
            final String newhash = Util.md5(PassKeepApp.sInstance.getSalt() + inputText);
            db.hashModel().addHash(new HashEntity(newhash));
            loginOkEvent.setValue(inputText);
        }
    }
}
