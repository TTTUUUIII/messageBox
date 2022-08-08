package com.autolink.msbox.util;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.autolink.msbox.R;

public class Utils {
    public static void alert(Context context, int drawable, String title, String msg, Call_ call_){
        new  AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(drawable)
                .setMessage(msg)
                .setPositiveButton(R.string.allow_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (call_ != null){
                            call_.op();
                        }
                    }
                }).show();
    }
    public static void warning(Context context , String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public interface Call_{
        void op();
    }
}
