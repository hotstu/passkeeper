package hotstu.github.passkeeper.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.support.annotation.NonNull;

import hotstu.github.passkeeper.Injection;
import hotstu.github.passkeeper.ListActivity;
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

    public WatchDogViewModel(@NonNull Application application) {
        super(application);
        db = Injection.getDataBase();
        input = new MutableLiveData<>();
    }

    public void login(Activity activity) {
        String inputText = input.getValue();
        if (inputText == null || "".equals(inputText)) {
            return;
        }
        final String hash = db.hashModel().checkHash();
        final String input = Util.md5(PassKeepApp.sInstance.getSalt() + inputText);
        if (hash != null && hash.equals(input)) {
            PassKeepApp.sInstance.setKey(inputText);
            Intent i = new Intent(activity, ListActivity.class);
            activity.startActivity(i);
        } else if (hash == null) {
            //the first time running this app
            final String newhash = Util.md5(PassKeepApp.sInstance.getSalt() + inputText);
            db.hashModel().addHash(new HashEntity(newhash));
            PassKeepApp.sInstance.setKey(inputText);
            Intent i = new Intent(activity, ListActivity.class);
            activity.startActivity(i);
        }
        activity.finish();
    }
}
