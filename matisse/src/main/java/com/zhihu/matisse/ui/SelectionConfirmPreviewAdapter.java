package com.zhihu.matisse.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.SelectionSpec;

import java.util.ArrayList;
import java.util.List;

public class SelectionConfirmPreviewAdapter extends RecyclerView.Adapter {

    private List<Uri> contentUriList = new ArrayList<>();

    public SelectionConfirmPreviewAdapter() {

    }

    public void setContentUriList(List<Uri> list) {
        contentUriList.clear();
        contentUriList.addAll(list);
        notifyDataSetChanged();
    }


    public void addContentUri(String url) {
        contentUriList.add(Uri.parse(url));
        notifyItemInserted(contentUriList.size() - 1);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_selection_preview_item, parent, false);
        return new SelectionPreviewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SelectionPreviewHolder) {
            Uri uri = contentUriList.get(position);
            ((SelectionPreviewHolder) holder).bindMedia(uri);
        }
    }

    @Override
    public int getItemCount() {
        return contentUriList.size();
    }

    class SelectionPreviewHolder extends RecyclerView.ViewHolder {
        ImageView mThumbnail;

        public SelectionPreviewHolder(View itemView) {
            super(itemView);
            this.mThumbnail = (ImageView) itemView;
        }

        private Context getContext() {
            return mThumbnail.getContext();
        }

        private int getImageResize(Context context) {
            float dimension = context.getResources().getDimension(R.dimen.selection_item_size);
            return (int) dimension;
        }

        private Drawable getPlaceHolder() {
            return new ColorDrawable(0xff37474F);
        }

        public void bindMedia(Uri uri) {
            SelectionSpec.getInstance().imageEngine.loadThumbnail(
                    getContext(),
                    getImageResize(getContext()),
                    getPlaceHolder(),
                    mThumbnail,
                    uri
            );
        }
    }
}
