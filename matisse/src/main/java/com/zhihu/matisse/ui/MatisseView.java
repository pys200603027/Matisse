package com.zhihu.matisse.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.Album;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.entity.SelectionSpec;
import com.zhihu.matisse.internal.model.AlbumCollection;
import com.zhihu.matisse.internal.model.SelectedItemCollection;
import com.zhihu.matisse.internal.ui.AlbumPreviewActivity;
import com.zhihu.matisse.internal.ui.BasePreviewActivity;
import com.zhihu.matisse.internal.ui.MediaSelectionFragment;
import com.zhihu.matisse.internal.ui.MediaSelectionLazyFragment;
import com.zhihu.matisse.internal.ui.adapter.AlbumMediaAdapter;
import com.zhihu.matisse.internal.ui.widget.IncapableDialog;
import com.zhihu.matisse.internal.utils.MediaStoreCompat;
import com.zhihu.matisse.internal.utils.PathUtils;
import com.zhihu.matisse.listener.OnResultListener;

import java.util.ArrayList;

/**
 * become a view
 */
public class MatisseView extends FrameLayout implements
        AlbumCollection.AlbumCallbacks,
        MediaSelectionLazyFragment.SelectionProvider,
        AlbumMediaAdapter.CheckStateListener,
        AlbumMediaAdapter.OnMediaClickListener,
        SelectionConfirmView.OnViewClickListener {

    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private SelectedItemCollection mSelectedCollection = new SelectedItemCollection(getContext());
    private SelectionSpec mSpec;
    /**
     * for capture
     */
    private MediaStoreCompat mMediaStoreCompat;

    /**
     * about View
     */
    private SelectionConfirmView selectionConfirmView;
    private View mEmptyView;
    private View mContainer;

    public static final String EXTRA_RESULT_SELECTION = "extra_result_selection";
    public static final String EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path";
    public static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";
    private static final int REQUEST_CODE_PREVIEW = 23;


    OnResultListener onResultListener;

    public MatisseView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public MatisseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.view_matisse, this);

        selectionConfirmView = findViewById(R.id.sc);
        mEmptyView = findViewById(R.id.empty_view);
        mContainer = findViewById(R.id.fl_container);
        selectionConfirmView.setOnViewClickListener(this);

        initAlbum();
    }

    public void initAlbum() {
        if (!(getContext() instanceof FragmentActivity)) {
            throw new RuntimeException("Context must be FragmentActivity.");
        }
        mSpec = SelectionSpec.getInstance();
        mSelectedCollection.onCreate(null);
        mAlbumCollection.onCreate((FragmentActivity) getContext(), this);
        mAlbumCollection.onRestoreInstanceState(null);
        mAlbumCollection.loadAlbums();
        updateBottomToolbar();
    }

    @Override
    public void onAlbumLoad(final Cursor cursor) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                cursor.moveToPosition(mAlbumCollection.getCurrentSelection());
                Album album = Album.valueOf(cursor);
                if (album.isAll() && SelectionSpec.getInstance().capture) {
                    album.addCaptureCount();
                }
                onAlbumSelected(album);
            }
        });
    }

    @Override
    public void onAlbumReset() {

    }

    private void onAlbumSelected(Album album) {
        if (album.isAll() && album.isEmpty()) {
            mContainer.setVisibility(View.GONE);
            selectionConfirmView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mContainer.setVisibility(View.VISIBLE);
            selectionConfirmView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);

            final MediaSelectionLazyFragment fragment = MediaSelectionLazyFragment.newInstance(album);
            ((FragmentActivity) getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_container, fragment, MediaSelectionLazyFragment.class.getSimpleName())
                    .commitAllowingStateLoss();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragment.attach(MatisseView.this);
                    fragment.onLazyLoad();
                }
            }, 150);
        }
    }

    @Override
    public SelectedItemCollection provideSelectedItemCollection() {
        return mSelectedCollection;
    }


    @Override
    public void onUpdate() {
        updateBottomToolbar();

        if (mSpec.onSelectedListener != null) {
            mSpec.onSelectedListener.onSelected(
                    mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString());
        }
    }

    private void updateBottomToolbar() {
        selectionConfirmView.update(mSelectedCollection.asListOfUri());

        int selectedCount = mSelectedCollection.count();
        if (selectedCount == 0) {
            selectionConfirmView.setSendBtnEnable(false);
        } else {
            selectionConfirmView.setSendBtnEnable(true);
        }

        if (mSpec.originalable) {
            selectionConfirmView.setOriginalItemVisible(View.VISIBLE);
            selectionConfirmView.updateOriginalState(((FragmentActivity) getContext()).getSupportFragmentManager(), mSelectedCollection.asList(), mSpec.originalMaxSize);
        } else {
            selectionConfirmView.setOriginalItemVisible(View.INVISIBLE);
        }
    }

    /**
     * original option click
     *
     * @return
     */
    @Override
    public boolean onOriginalClick() {
        int count = selectionConfirmView.countOverMaxSize(mSelectedCollection.asList(), mSpec.originalMaxSize);
        if (count > 0) {
            IncapableDialog incapableDialog = IncapableDialog.newInstance("", getResources().getString(R.string.error_over_original_count, count, mSpec.originalMaxSize));
            incapableDialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(), IncapableDialog.class.getName());
            return false;
        }
        if (mSpec.onCheckedListener != null) {
            mSpec.onCheckedListener.onCheck(selectionConfirmView.isOriginalEnable());
        }
        return true;
    }

    @Override
    public void onSendClick() {
        Intent result = new Intent();
        ArrayList<Uri> selectedUris = (ArrayList<Uri>) mSelectedCollection.asListOfUri();
        result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris);
        ArrayList<String> selectedPaths = (ArrayList<String>) mSelectedCollection.asListOfString();
        result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths);
        result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, selectionConfirmView.isOriginalEnable());

        if (onResultListener != null) {
            onResultListener.onResult(Activity.RESULT_OK, result);
        }
    }


    /**
     * PreviewActivity
     *
     * @param album
     * @param item
     * @param adapterPosition
     */
    @Override
    public void onMediaClick(Album album, Item item, int adapterPosition) {
        Intent intent = new Intent(getContext(), AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, selectionConfirmView.isOriginalEnable());
        ((FragmentActivity) getContext()).startActivityForResult(intent, REQUEST_CODE_PREVIEW);
    }

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_PREVIEW) {
            Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
            ArrayList<Item> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
            selectionConfirmView.setOriginalEnable(data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false));
            int collectionType = resultBundle.getInt(SelectedItemCollection.STATE_COLLECTION_TYPE,
                    SelectedItemCollection.COLLECTION_UNDEFINED);
            /**
             * apply & send
             */
            if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                Intent result = new Intent();
                ArrayList<Uri> selectedUris = new ArrayList<>();
                ArrayList<String> selectedPaths = new ArrayList<>();
                if (selected != null) {
                    for (Item item : selected) {
                        selectedUris.add(item.getContentUri());
                        selectedPaths.add(PathUtils.getPath(getContext(), item.getContentUri()));
                    }
                }
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris);
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths);
                result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, selectionConfirmView.isOriginalEnable());
                if (onResultListener != null) {
                    onResultListener.onResult(Activity.RESULT_OK, result);
                }
            } else {
                /**
                 * just return
                 */
                mSelectedCollection.overwrite(selected, collectionType);
                Fragment mediaSelectionFragment = ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentByTag(
                        MediaSelectionFragment.class.getSimpleName());
                if (mediaSelectionFragment instanceof MediaSelectionFragment) {
                    ((MediaSelectionFragment) mediaSelectionFragment).refreshMediaGrid();
                }
                updateBottomToolbar();
            }
        }
    }
}
