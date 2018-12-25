package com.zhihu.matisse.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.SelectionSpec;
import com.zhihu.matisse.listener.OnResultListener;

public class DoubleMatisseActivity extends AppCompatActivity implements OnResultListener {

    MatisseView matisseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SelectionSpec mSpec = SelectionSpec.getInstance();
        setTheme(mSpec.themeId);

        if (!mSpec.hasInited) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_matisse);
        matisseView = findViewById(R.id.mv);
        matisseView.setOnResultListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        matisseView.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResult(int requestCode, Intent data) {
        setResult(requestCode, data);
        finish();
    }
}
