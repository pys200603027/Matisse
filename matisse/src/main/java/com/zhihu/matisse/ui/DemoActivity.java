package com.zhihu.matisse.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.Album;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.entity.SelectionSpec;
import com.zhihu.matisse.internal.model.AlbumCollection;
import com.zhihu.matisse.internal.model.SelectedItemCollection;
import com.zhihu.matisse.internal.ui.AlbumPreviewActivity;
import com.zhihu.matisse.internal.ui.BasePreviewActivity;
import com.zhihu.matisse.internal.ui.MediaSelectionFragment;
import com.zhihu.matisse.internal.ui.adapter.AlbumMediaAdapter;
import com.zhihu.matisse.internal.ui.widget.IncapableDialog;
import com.zhihu.matisse.internal.utils.MediaStoreCompat;
import com.zhihu.matisse.internal.utils.PathUtils;

import java.util.ArrayList;

/**
 * 模仿了一遍
 */
public class DemoActivity extends AppCompatActivity implements
        AlbumCollection.AlbumCallbacks,
        MediaSelectionFragment.SelectionProvider,
        AlbumMediaAdapter.CheckStateListener,
        AlbumMediaAdapter.OnMediaClickListener, SelectionConfirmView.OnViewClickListener {

    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
    private SelectionSpec mSpec;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSpec = SelectionSpec.getInstance();
        setTheme(mSpec.themeId);
        if (!mSpec.hasInited) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        if (mSpec.needOrientationRestriction()) {
            setRequestedOrientation(mSpec.orientation);
        }

        if (mSpec.capture) {
            mMediaStoreCompat = new MediaStoreCompat(this);
            if (mSpec.captureStrategy == null) {
                throw new RuntimeException("Don't forget to set CaptureStrategy.");
            }
            mMediaStoreCompat.setCaptureStrategy(mSpec.captureStrategy);
        }

        mSelectedCollection.onCreate(savedInstanceState);
        mAlbumCollection.onCreate(this, this);
        mAlbumCollection.onRestoreInstanceState(savedInstanceState);
        mAlbumCollection.loadAlbums();


        selectionConfirmView = findViewById(R.id.sc);

        mEmptyView = findViewById(R.id.empty_view);

        mContainer = findViewById(R.id.fl_container);
        selectionConfirmView.setOnViewClickListener(this);
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

            Fragment fragment = MediaSelectionFragment.newInstance(album);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_container, fragment, MediaSelectionFragment.class.getSimpleName())
                    .commitAllowingStateLoss();
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
            selectionConfirmView.updateOriginalState(getSupportFragmentManager(), mSelectedCollection.asList(), mSpec.originalMaxSize);
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
            IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                    getString(R.string.error_over_original_count, count, mSpec.originalMaxSize));
            incapableDialog.show(getSupportFragmentManager(),
                    IncapableDialog.class.getName());
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
        setResult(RESULT_OK, result);
        finish();
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
        Intent intent = new Intent(this, AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, selectionConfirmView.isOriginalEnable());
        startActivityForResult(intent, REQUEST_CODE_PREVIEW);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
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
                        selectedPaths.add(PathUtils.getPath(this, item.getContentUri()));
                    }
                }
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris);
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths);
                result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, selectionConfirmView.isOriginalEnable());
                setResult(RESULT_OK, result);
                finish();
            } else {
                /**
                 * just return
                 */
                mSelectedCollection.overwrite(selected, collectionType);
                Fragment mediaSelectionFragment = getSupportFragmentManager().findFragmentByTag(
                        MediaSelectionFragment.class.getSimpleName());
                if (mediaSelectionFragment instanceof MediaSelectionFragment) {
                    ((MediaSelectionFragment) mediaSelectionFragment).refreshMediaGrid();
                }
                updateBottomToolbar();
            }
        }
    }


}
