package com.autolink.msbox;

import androidx.fragment.app.Fragment;

/****
 * fragmentManager Callback
 */
public interface FMCallback {
    void replace(Fragment fragment, boolean isCanBack);
    void remove(Fragment fragment, boolean isCanBack);
    void add(Fragment fragment, boolean isCanBack);

    void tryConnect(boolean flag);
}
