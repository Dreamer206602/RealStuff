package com.example.ivor_hu.meizhi.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ivor_hu.meizhi.R;
import com.example.ivor_hu.meizhi.db.DBManager;
import com.example.ivor_hu.meizhi.db.Stuff;
import com.example.ivor_hu.meizhi.utils.Constants;
import com.example.ivor_hu.meizhi.utils.DateUtil;

import java.util.List;

/**
 * Created by Ivor on 2016/2/28.
 */
public class StuffAdapter extends RecyclerView.Adapter<StuffAdapter.Viewholder> {
    private static final String TAG = "StuffAdapter";
    private Context mContext;
    private List<Stuff> mStuffs;
    private OnItemClickListener mOnItemClickListener;
    private int lastStuffsNum;
    private String mType;

    public void updateInsertedData(int numImages, boolean isMore) {
        if (isMore) {
//            notifyItemRangeInserted(lastStuffsNum - 1, numImages);
            Log.d(TAG, "updateInsertedData: from " + (lastStuffsNum - 1) + " by " + numImages);
        } else {
//            notifyItemRangeInserted(0, numImages);
            Log.d(TAG, "updateInsertedData: from 0 to " + numImages);
        }
        lastStuffsNum += numImages;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int pos);

        void onItemLongClick(View view, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public StuffAdapter(Context mContext, String type) {
        this.mContext = mContext;
        this.mType = type;
        if (mType.equals(Constants.TYPE_COLLECTIONS)) {
//            mStuffs = realm.where(Stuff.class)
//                    .equalTo("isLiked", true)
//                    .findAllSorted("lastChanged", Sort.DESCENDING);
        } else {
            mStuffs = DBManager.getIns(mContext).queryAllStuffs(mType);
        }
        lastStuffsNum = mStuffs.size();
        setHasStableIds(true);
    }

    @Override
    public StuffAdapter.Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(mContext).inflate(R.layout.stuff_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, final int position) {
        final Stuff stuff = mStuffs.get(position);
        holder.author.setText(stuff.getAuthor());
        holder.title.setText(stuff.getTitle());
        holder.date.setText(DateUtil.format(stuff.getPublishedAt()));
        if (null != mOnItemClickListener) {
            holder.stuff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            });

            holder.stuff.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(v, position);
                    return true;
                }
            });
        }
        holder.likeBtn.setTag(position);
        if (mType.equals(Constants.TYPE_COLLECTIONS)) {
            holder.likeBtn.setImageResource(R.drawable.like);
            holder.likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteItem(position);
                }
            });
        }else{
            holder.likeBtn.setImageResource(mStuffs.get(position).isLiked() ? R.drawable.like : R.drawable.unlike);
            holder.likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    toggleLikeBtn((ImageButton) v, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mStuffs.size();
    }

    @Override
    public long getItemId(int position) {
        return mStuffs.get(position).getId().hashCode();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView title, author, date;
        LinearLayout stuff;
        ImageButton likeBtn;

        public Viewholder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.stuff_title);
            author = (TextView) itemView.findViewById(R.id.stuff_author);
            date = (TextView) itemView.findViewById(R.id.stuff_date);
            stuff = (LinearLayout) itemView.findViewById(R.id.stuff);
            likeBtn = (ImageButton) itemView.findViewById(R.id.like_btn);
        }

    }

    private void deleteItem(int position) {
//        realm.beginTransaction();
//        mStuffs.where()
//                .equalTo("id", mStuffs.get(position).getId())
//                .findFirst()
//                .setLiked(false);
//        realm.commitTransaction();
//        notifyDataSetChanged();
    }

    private void toggleLikeBtn(ImageButton likeBtn, int pos) {

        if (mStuffs.get(pos).isLiked()) {
            likeBtn.setImageResource(R.drawable.unlike);
            changeLiked(pos, false);
        } else {
            likeBtn.setImageResource(R.drawable.like);
            changeLiked(pos, true);
        }
    }

    private void changeLiked(int pos, boolean isLiked) {
//        realm.beginTransaction();
//        Stuff stuff = mStuffs
//                .where()
//                .equalTo("id", mStuffs.get(pos).getId())
//                .findFirst();
//
//        stuff.setLiked(isLiked);
//        stuff.setLastChanged(new Date());
//        realm.commitTransaction();
//        notifyItemChanged(pos);
        DBManager.getIns(mContext).updateStuffLiked(mStuffs.get(pos).getId(), isLiked);
        notifyDataSetChanged();
    }

    public Stuff getStuffAt(int pos) {
        return mStuffs.get(pos);
    }



}
