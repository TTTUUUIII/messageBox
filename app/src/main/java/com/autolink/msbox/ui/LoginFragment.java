package com.autolink.msbox.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.autolink.msbox.DataRepository;
import com.autolink.msbox.FMCallback;
import com.autolink.msbox.MainActivity;
import com.autolink.msbox.MessageHandler;
import com.autolink.msbox.MsgBoxViewModel;
import com.autolink.msbox.R;
import com.autolink.msbox.util.Utils;

import java.util.ArrayList;

public class LoginFragment extends BaseFragment {
    private static final String TAG = LoginFragment.class.getSimpleName();
    private static final DataRepository repository = DataRepository.getInstance();
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLogInBtn;
    private Button mRegisterBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ((MainActivity)requireActivity()).getSupportActionBar()
                .setWindowTitle(getString(R.string.login_window_title));
        ((MainActivity)requireActivity()).getSupportActionBar()
                .setSubtitle("");
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mUsernameEditText = view.findViewById(R.id.username_edit);
        mPasswordEditText = view.findViewById(R.id.password_edit);
        mLogInBtn = view.findViewById(R.id.login_btn);
        mRegisterBtn = view.findViewById(R.id.register_btn);
        setListener();
        return view;
    }

    public static LoginFragment newInstance(Bundle bundle) {
        LoginFragment instance = new LoginFragment();
        instance.setArguments(bundle);
        return instance;
    }

    private void setListener() {
        mLogInBtn.setOnClickListener(view -> {
            if (!viewModel.hasSocket()) {
                Utils.alert(requireActivity(),
                        R.drawable.disconnect,
                        getString(R.string.alert_title),
                        getString(R.string.failed_connect),
                        null);
            } else {
                String msg = String.format("client:login~%s:%s",
                        mUsernameEditText.getText().toString(),
                        mPasswordEditText.getText().toString());
                repository.write(viewModel.getOut(), msg);
            }
        });
        mRegisterBtn.setOnClickListener(view -> {
            if (!viewModel.hasSocket()){
                Utils.alert(requireActivity(),
                        R.drawable.disconnect,
                        getString(R.string.alert_title),
                        getString(R.string.failed_connect),
                        null);
            }else {
                callback.add(RegisterFragment.newInstance(), true);
            }
        });
    }

    @Override
    public boolean msgHandle(String msg) {
        super.msgHandle(msg);
        boolean isHandle = true;
        String[] resp = msg.split("~");
        // 指令
        String order = resp[0];
        switch (order) {
            case "SERVER:WELCOME":
                Utils.warning(requireContext(),
                        String.format(getString(R.string.tem_welcome_text), mUsernameEditText.getText()));
                UserListFragment userListFragment = UserListFragment.newInstance();
                userListFragment.getArguments().putString(UserListFragment.PAR_M_INFO, mUsernameEditText.getText().toString());
                callback.replace(userListFragment, false);
                break;
            default:
                isHandle = false;
        }
        return isHandle;
    }
}