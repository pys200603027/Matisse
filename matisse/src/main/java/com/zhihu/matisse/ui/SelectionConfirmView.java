package com.zhihu.matisse.ui;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.ui.widget.CheckRadioView;
import com.zhihu.matisse.internal.ui.widget.IncapableDialog;
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils;

import java.util.List;


public class SelectionConfirmView extends LinearLayout implements View.OnClickListener {

    /**
     * about original
     */
    private LinearLayout mOriginalLayout;
    private CheckRadioView mOriginal;
    private boolean mOriginalEnable;

    /**
     * preview list
     */
    private RecyclerView recyclerView;
    private SelectionConfirmPreviewAdapter adapter;

    /**
     * confirm & send
     */
    View sendView;

    public SelectionConfirmView(Context context) {
        super(context);
        initView(context);
    }

    public SelectionConfirmView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.photo_selection_confirm_item, this);
        mOriginalLayout = findViewById(R.id.originalLayout);
        mOriginal = findViewById(R.id.original);
        mOriginalLayout.setOnClickListener(this);

        recyclerView = findViewById(R.id.preview_list);
        sendView = findViewById(R.id.tv_send);
        sendView.setOnClickListener(this);

        initRecyclerView();
    }

    private void initRecyclerView() {
        adapter = new SelectionConfirmPreviewAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addItemDecoration(new OffsetItemDecoration(getContext(), 0, 12, 0, 0));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    public void update(List<Uri> list) {
        adapter.setContentUriList(list);
    }

    public void setSendBtnEnable(boolean enable) {
        sendView.setEnabled(enable);
    }

    public void setOriginalItemVisible(int visible) {
        mOriginalLayout.setVisibility(visible);
    }

    public int countOverMaxSize(List<Item> items, int originalMaxSize) {
        int count = 0;
        int selectedCount = items.size();
        for (int i = 0; i < selectedCount; i++) {
            Item item = items.get(i);
            if (item.isImage()) {
                float size = PhotoMetadataUtils.getSizeInMB(item.size);
                if (size > originalMaxSize) {
                    count++;
                }
            }
        }
        return count;
    }

    public SelectionConfirmView updateOriginalState(FragmentManager fragmentManager, List<Item> items, int originalMaxSize) {
        mOriginal.setChecked(mOriginalEnable);
        if (countOverMaxSize(items, originalMaxSize) > 0) {
            if (mOriginalEnable) {
                IncapableDialog incapableDialog = IncapableDialog.newInstance("", getResources().getString(R.string.error_over_original_size, originalMaxSize));
                incapableDialog.show(fragmentManager, IncapableDialog.class.getName());
                mOriginal.setChecked(false);
                mOriginalEnable = false;
            }
        }
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.originalLayout) {
            if (onViewClickListener != null) {
                boolean b = onViewClickListener.onOriginalClick();
                if (b) {
                    mOriginalEnable = !mOriginalEnable;
                    mOriginal.setChecked(mOriginalEnable);
                }
            }
        } else if (v.getId() == R.id.tv_send) {
            if (onViewClickListener != null) {
                onViewClickListener.onSendClick();
            }
        }
    }

    public boolean isOriginalEnable() {
        return mOriginalEnable;
    }

    public SelectionConfirmView setOriginalEnable(boolean mOriginalEnable) {
        this.mOriginalEnable = mOriginalEnable;
        return this;
    }

    OnViewClickListener onViewClickListener;

    public SelectionConfirmView setOnViewClickListener(OnViewClickListener onViewClickListener) {
        this.onViewClickListener = onViewClickListener;
        return this;
    }

    public interface OnViewClickListener {
        boolean onOriginalClick();

        void onSendClick();
    }
}
