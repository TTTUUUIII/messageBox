package com.autolink.msbox.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
import java.util.HashSet;

public class BaseFragment extends Fragment implements MessageHandler {
    private final String TAG = this.getClass().getSimpleName();
    protected static MsgBoxViewModel viewModel;
    protected static ArrayList<String> messageBox;
    protected static ArrayList<String> broadcastBox;
    protected static HashSet<String> userSet;
    protected FMCallback callback;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(requireActivity()).get(MsgBoxViewModel.class);
        callback = (MainActivity)requireActivity();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_login_fragment, menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.getMessageLiveData()
                .removeObservers(getViewLifecycleOwner());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.reconnect:
                if (viewModel.hasSocket()){
                    Utils.alert(requireActivity(),
                            R.drawable.warning,
                            getString(R.string.warning),
                            getString(R.string.disconnected),
                            null);
                }else {
                    try {
                        callback.tryConnect(false);
                        Thread.sleep(300);
                        bindData();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "callback is null.");
                    }
                }
                break;
            default:
                // do nothing
        }
        return true;
    }

    @Override
    public boolean msgHandle(String msg) {
        //Log.d(TAG, msg);
        boolean isHandle = true;
        String[] resp = msg.split("~");
        // 指令
        String order = resp[0];
        switch (order) {
            case "SERVER:STOPPED":
                viewModel.close();
                Utils.alert(requireActivity(),
                        R.drawable.disconnect,
                        getString(R.string.alert_title),
                        getString(R.string.server_disconnected),
                        () -> {
                    requireActivity().finish();
                        });
                break;
            case "SERVER:WELCOME":
                // do nothing
                break;
            case "SERVER:DENY":
                Utils.warning(requireActivity(),
                        getString(R.string.login_deny));
                break;
            case "SERVER:LOGIN":
                userSet.add(resp[1]);
                Utils.warning(requireContext(), "login: " + resp[1]);
                break;
            case "SERVER:LOGOUT":
                userSet.remove(resp[1]);
                Utils.warning(requireContext(), "logout: " + resp[1]);
                break;
            case "SERVER:REG_DENY":
                Utils.alert(requireContext(),
                        R.drawable.error,
                        getString(R.string.alert_title),
                        getString(R.string.register_deny),
                        null);
                break;
            case "SERVER:REG_SUCCESS":
                Utils.warning(requireContext(), getString(R.string.register_success));
                requireActivity().getSupportFragmentManager().popBackStack();
                break;
            case "SERVER:MSG":
                if (messageBox.size() > 0){
                    int pre = messageBox.size() - 1;
                    if (!messageBox.get(pre).equals(resp[1])){
                        messageBox.add(resp[1]);
                    }
                }else {
                    messageBox.add(resp[1]);
                }
                break;
            case "SERVER:BRO":
                if (broadcastBox.size() > 0){
                    int pre = broadcastBox.size() - 1;
                    if (!broadcastBox.get(pre).equals(resp[1])){
                        broadcastBox.add(resp[1]);
                    }
                }else {
                    broadcastBox.add(resp[1]);
                }
                break;
            default:
                isHandle = false;

        }
        return isHandle;
    }

    private void bindData(){
        if (viewModel.hasSocket()){
            messageBox = viewModel.getHistoryMessage();
            broadcastBox = viewModel.getHistoryBroadcast();
            userSet= viewModel.getUserSet();
            viewModel.getMessageLiveData()
                    .observe(getViewLifecycleOwner(), msg -> {
                        this.msgHandle(msg);
                    });
        }
    }
}
