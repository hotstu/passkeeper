package hotstu.github.passkeeper;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;

import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import hotstu.github.passkeeper.databinding.PwdformBinding;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PwdformBinding binding = DataBindingUtil.setContentView(this, R.layout.pwdform);

        final WatchDogViewModel viewModel = ViewModelProviders.of(this).get(WatchDogViewModel.class);

        Consumer<Throwable> erroCumsumer = new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        };
        RxCompoundButton.checkedChanges(binding.cbShowPwd).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (!aBoolean) {
                    binding.etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    binding.etPassword.setTransformationMethod(null);
                }
            }
        }, erroCumsumer);
        RxTextView.textChangeEvents(binding.etPassword).subscribe(new Consumer<TextViewTextChangeEvent>() {
            @Override
            public void accept(TextViewTextChangeEvent textViewTextChangeEvent) throws Exception {
                viewModel.input.setValue(textViewTextChangeEvent.text().toString());
            }
        }, erroCumsumer);
        binding.setViewModel(viewModel);
        binding.setActivity(this);
        viewModel.input.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                binding.btnOk.setEnabled(s != null && !"".equals(s));
            }
        });
    }



}
