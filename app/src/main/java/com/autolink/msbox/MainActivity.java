package com.autolink.msbox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;

import com.autolink.msbox.ui.LoginFragment;
import com.autolink.msbox.util.Utils;

public class MainActivity extends AppCompatActivity implements FMCallback{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final DataRepository repository = DataRepository.getInstance();
    private FragmentManager fm;
    private MsgBoxViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MsgBoxViewModel.class);
        fm = getSupportFragmentManager();
        if (savedInstanceState == null){
            tryConnect(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.close();
    }

    @Override
    public void replace(Fragment fragment, boolean isCanBack) {
        if (isCanBack){
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }else {
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void remove(Fragment fragment, boolean isCanBack) {
        if (isCanBack){
            fm.beginTransaction()
                    .remove(fragment)
                    .addToBackStack(null)
                    .commit();
        }else {
            fm.beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    @Override
    public void add(Fragment fragment, boolean isCanBack) {
        if (isCanBack){
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }else {
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void tryConnect(boolean flag){
        repository.connect()
                .observe(this, socket -> {
                    if (socket != null){
                        viewModel.setSocket(socket);
                        Utils.warning(this, getString(R.string.server_connect));
                    }else {
                        Utils.alert(this,
                                R.drawable.disconnect,
                                getResources().getString(R.string.alert_title),
                                getString(R.string.failed_connect),
                                null);
                    }
                    if (flag){
                        fm.beginTransaction()
                                .replace(R.id.fragment_container, LoginFragment.newInstance(new Bundle()))
                                .commit();
                    }
                });
    }
}