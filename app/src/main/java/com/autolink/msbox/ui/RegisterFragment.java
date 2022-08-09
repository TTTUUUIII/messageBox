package com.autolink.msbox.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.autolink.msbox.DataRepository;
import com.autolink.msbox.MainActivity;
import com.autolink.msbox.MsgBoxViewModel;
import com.autolink.msbox.R;

public class RegisterFragment extends BaseFragment {
    private static final String TAG = RegisterFragment.class.getSimpleName();
    private final DataRepository repository = DataRepository.getInstance();
    private MsgBoxViewModel viewModel;
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private Button mRegisterBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MsgBoxViewModel.class);
    }

    @SuppressLint("RestrictedApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ((MainActivity)requireActivity()).getSupportActionBar()
                .setWindowTitle("用户注册");
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        mUserNameEditText = view.findViewById(R.id.username_edit);
        mPasswordEditText = view.findViewById(R.id.password_edit);
        mRegisterBtn = view.findViewById(R.id.register_btn);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListener();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
    }

    public static RegisterFragment newInstance(){
        RegisterFragment instance = new RegisterFragment();
        instance.setArguments(new Bundle());
        return instance;
    }

    public void setListener(){
        mRegisterBtn.setOnClickListener(view -> {
            String req = String.format("client:register~%s:%s",
                    mUserNameEditText.getText(), mPasswordEditText.getText());
            repository.write(viewModel.getOut(), req);
        });
    }
}