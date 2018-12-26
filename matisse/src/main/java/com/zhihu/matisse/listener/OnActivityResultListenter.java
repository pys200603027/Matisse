package com.zhihu.matisse.listener;

import android.content.Intent;

public interface OnActivityResultListenter {
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
