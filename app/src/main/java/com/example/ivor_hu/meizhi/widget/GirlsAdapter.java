package com.example.ivor_hu.meizhi.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ivor_hu.meizhi.R;
import com.example.ivor_hu.meizhi.db.DBManager;
import com.example.ivor_hu.meizhi.db.Image;

import java.util.List;

/**
 * Created by Ivor on 2016/2/6.
 */
public class GirlsAdapter extends RecyclerView.Adapter<GirlsAdapter.MyViewHolder> {
    private static final String TAG = "GirlsAdapter";

    private Context mContext;
    private List<Image> mImages;
    private OnItemClickListener mOnItemClickListener;
    private int lastImagesNum;

    public GirlsAdapter(Context mContext) {
        this.mContext = mContext;
        mImages = DBManager.getIns(mContext).queryAllImages();
        lastImagesNum = mImages.size();
        setHasStableIds(true);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

//    public void updateInsertedData(int numImages, boolean isMore) {
//        if (isMore) {
//            notifyItemRangeInserted(lastImagesNum - 1, numImages);
//            Log.d(TAG, "updateInsertedData: from " + (lastImagesNum - 1) + " by " + numImages);
//        } else {
//            notifyItemRangeInserted(0, numImages);
//            Log.d(TAG, "updateInsertedData: from 0 to " + numImages);
//        }
//        lastImagesNum += numImages;
//    }

    public void updateInsertedDataFirstTime(int numImages) {
        if (lastImagesNum > 0 )
            return;
        notifyItemRangeInserted(0, numImages);
        lastImagesNum = mImages.size();
        Log.d(TAG, "updateInsertedData: from 0 to " + numImages);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).
                inflate(R.layout.girls_item, parent, false));
    }

    @Override
    public long getItemId(int position) {
        return mImages.get(position).getId().hashCode();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Image image = mImages.get(position);

        holder.imageView.setOriginalSize(image.getWidth(), image.getHeight());
        Glide.with(mContext)
                .load(image.getUrl())
//                .placeholder(R.color.cardview_light_background)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);
        ViewCompat.setTransitionName(holder.imageView, image.getUrl());

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(v, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(v, pos);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    public String getUrlAt(int pos) {
        return mImages.get(pos).getUrl();
    }

    public interface OnItemClickListener {

        void onItemClick(View view, int pos);

        void onItemLongClick(View view, int pos);

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        RatioImageView imageView;
        CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardview);
            imageView = (RatioImageView) itemView.findViewById(R.id.network_imageview);
        }

    }

}
