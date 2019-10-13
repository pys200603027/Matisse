package com.zhihu.matisse.internal.loader;

import android.content.Context;
import android.provider.MediaStore;

import androidx.loader.content.CursorLoader;

import com.zhihu.matisse.internal.entity.Album;

public class AlbumMediaLoaderDelegate extends AlbumMediaLoader {

    public AlbumMediaLoaderDelegate(Context context, String selection, String[] selectionArgs, boolean capture) {
        super(context, selection, selectionArgs, capture);
    }

    public static CursorLoader newVideoInstance(Context context, Album album, boolean capture) {
        String selection;
        String[] selectionArgs;
        boolean enableCapture;

        if (album.isAll()) {
            selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE;
            selectionArgs = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
            enableCapture = capture;
        } else {
            selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE;
            selectionArgs = getSelectionAlbumArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                    album.getId());
            enableCapture = false;
        }
        return new AlbumMediaLoader(context, selection, selectionArgs, enableCapture);
    }

    public static CursorLoader newImageInstance(Context context, Album album, boolean capture) {
        String selection;
        String[] selectionArgs;
        boolean enableCapture;

        if (album.isAll()) {
            selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE;
            selectionArgs = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
            enableCapture = capture;
        } else {
            selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE;
            selectionArgs = getSelectionAlbumArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                    album.getId());
            enableCapture = false;
        }
        return new AlbumMediaLoader(context, selection, selectionArgs, enableCapture);
    }
}
