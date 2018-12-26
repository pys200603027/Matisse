package com.zhihu.matisse.ui;

import android.content.Intent;

import com.zhihu.matisse.listener.OnActivityResultListenter;

/**
 * Activity Result bridge
 */
public class ActivityResultHelper {

    private static ActivityResultHelper in;

    private ActivityResultHelper() {
    }

    public static ActivityResultHelper getInstance() {
        if (in == null) {
            synchronized (ActivityResultHelper.class) {
                if (in == null) {
                    in = new ActivityResultHelper();
                }
            }
        }
        return in;
    }

    /**
     * be careful :don't forget to remove
     */
    private OnActivityResultListenter activityResultListenter;


    public void setActivityResultListenter(OnActivityResultListenter activityResultListenter) {
        this.activityResultListenter = activityResultListenter;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (activityResultListenter != null) {
            activityResultListenter.onActivityResult(requestCode, resultCode, data);
        }
    }
}
