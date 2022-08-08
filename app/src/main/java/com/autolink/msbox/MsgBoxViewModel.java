package com.autolink.msbox;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

public class MsgBoxViewModel extends ViewModel {
    private static final String TAG = MsgBoxViewModel.class.getSimpleName();
    private static final DataRepository repository = DataRepository.getInstance();
    private LiveData<String> messageLiveData;
    private ArrayList<String> HistoryMessage = new ArrayList<>();
    private ArrayList<String> HistoryBroadcast = new ArrayList<>();
    private HashSet<String> userSet = new HashSet<>();
    private Socket socket = null;

    public void setSocket(Socket socket) {
        this.socket = socket;
        messageLiveData = repository.msgReader(this.getIn());
    }

    public InputStream getIn() {
        InputStream inputStream = null;
        try {
            inputStream = socket.getInputStream();
        } catch (IOException ioException) {
            Log.d(TAG, "获取输入流失败");
        }
        return inputStream;
    }

    public OutputStream getOut() {
        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException ioException) {
            Log.d(TAG, "获取输出流失败");
        }
        return outputStream;
    }

    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public HashSet<String> getUserSet() {
        return userSet;
    }

    public ArrayList<String> getHistoryMessage() {
        return HistoryMessage;
    }

    public boolean hasSocket(){
        return this.socket != null;
    }

    public ArrayList<String> getHistoryBroadcast() {
        return HistoryBroadcast;
    }

    public void close() {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
