package com.zhihu.matisse.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.R;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.SelectionSpec;
import com.zhihu.matisse.listener.OnResultListener;

public class DoubleMatisseActivity extends AppCompatActivity implements OnResultListener {

    MatisseView matisseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Matisse.from(this)
                .choose(MimeType.ofImage())
                .theme(R.style.Matisse_Dracula)
                .countable(false)
                .maxSelectable(9)
                .originalEnable(false)
                .maxOriginalSize(10)
                .imageEngine(new PicassoEngine())
                .forCallback(new OnResultListener() {
                    @Override
                    public void onResult(int requestCode, Intent data) {
                        Log.d("123", "resustCode:" + requestCode);
                    }
                });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_matisse);
        matisseView = findViewById(R.id.mv);
        matisseView.initAlbum();
    }

    /**
     * for preview callback
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultHelper.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResult(int requestCode, Intent data) {
        setResult(requestCode, data);
        finish();
    }

}
