package com.autolink.msbox;

import android.content.Context;
import android.util.Log;
import android.widget.TableRow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataRepository {
    private Context context;
    private static final String TAG = DataRepository.class.getSimpleName();
    private Socket socket;
//    private static final String SERVER_IP = "10.68.28.119";
    private static final String SERVER_IP = "10.68.7.148";
//    private static final String SERVER_IP = "192.168.129.221";
    private static final Integer SERVER_PORT = 3333;
    private static DataRepository INSTANCE = null;
    private static boolean sign = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DataRepository(Context context){
        this.context = context;
    }
    public static void initialize(Context context){
        INSTANCE = new DataRepository(context);
    }

    public static DataRepository getInstance(){
        if (INSTANCE == null) {throw new ExceptionInInitializerError("the repository need to be initialize.");}
        return INSTANCE;
    }

    public LiveData<Socket> connect(){
        MutableLiveData<Socket> socketMutableLiveData = new MutableLiveData<>();
        executor.execute(() ->{
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                 socketMutableLiveData.postValue(socket);
            } catch (IOException exception) {
                socketMutableLiveData.postValue(null);
            }
        });
        return socketMutableLiveData;
    }

    public LiveData<String> msgReader(InputStream inputStream){
        this.sign = true;
        MutableLiveData<String> msgLiveData = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            while (sign){
                try {
                    byte[] buffer = new byte[1024];
                    int b = inputStream.read(buffer);
                    if (b != -1){
                        String msg = new String(buffer, "utf8").split("#")[0];
                        msgLiveData.postValue(msg);
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        return msgLiveData;
    }

    public void quit(){
        this.sign = false;
    }

    public void write(OutputStream out, String msg) {
        executor.execute(() -> {
            try {
                out.write((msg+"#").trim().getBytes());
                out.flush();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }
}
