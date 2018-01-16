package hotstu.github.passkeeper;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;

import hotstu.github.passkeeper.db.AppDatabase;
import hotstu.github.passkeeper.db.HashEntity;
import hotstu.github.passkeeper.viewmodel.WatchDogViewModel;
import io.reactivex.functions.Consumer;

/**
 * an activity to prevent viewing accounts without masterkey
 * the first time the app run will get the masterkey and a
 * email address to sendto when backup.
 *
 * @author foo
 */
public class WatchdogActivity extends AppCompatActivity {
    private Myapp app;
    private EditText etPassword;
    private AppDatabase db;
    private View btn_ok;
    private Consumer<Throwable> erroCumsumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pwdform);
        Log.d("WatchdogActivity", "onCreate");
        WatchDogViewModel viewModel = ViewModelProviders.of(this).get(WatchDogViewModel.class);
        db = AppDatabase.getInMemoryDatabase(getApplicationContext());

        etPassword = (EditText) findViewById(R.id.etPassword);
        btn_ok =  findViewById(R.id.btn_ok);
        erroCumsumer = new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        };
        final CheckBox ckPasswordToggle = (CheckBox) findViewById(R.id.cbShowPwd);
        RxCompoundButton.checkedChanges(ckPasswordToggle).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (!aBoolean) {
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    etPassword.setTransformationMethod(null);
                }
            }
        }, erroCumsumer);
        RxView.clicks(btn_ok).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                login();
            }
        }, erroCumsumer);
    }

    private void login() {
        final String inputText  = etPassword.getText().toString();
        if ("".equals(inputText)) {
            return;
        }
        final String hash = db.hashModel().checkHash();
        final String input = ListActivity.md5(app.getSalt() + inputText);
        if (hash != null && hash.equals(input)) {
            app.setKey(etPassword.getText().toString());
            Intent i = new Intent(getApplicationContext(), ListActivity.class);
            startActivity(i);
            finish();
        } else if (hash == null) {
            //the first time running this app
            final String newhash = ListActivity.md5(app.getSalt() + etPassword.getText().toString());
            db.hashModel().addHash(new HashEntity(newhash));
            app.setKey(etPassword.getText().toString());
            Intent i = new Intent(getApplicationContext(), ListActivity.class);
            startActivity(i);
            finish();
        } else {
            finish();
        }
    }

}
