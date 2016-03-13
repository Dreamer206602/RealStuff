package com.example.ivor_hu.meizhi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.example.ivor_hu.meizhi.db.Stuff;
import com.example.ivor_hu.meizhi.services.StuffFetchService;
import com.example.ivor_hu.meizhi.utils.CommonUtil;
import com.example.ivor_hu.meizhi.utils.Constants;
import com.example.ivor_hu.meizhi.utils.VolleyUtil;
import com.example.ivor_hu.meizhi.widget.StuffAdapter;

/**
 * Created by Ivor on 2016/3/3.
 */
public class StuffFragment extends Fragment {
    private static final String TAG = "StuffFragment";
    public static final String SERVICE_TYPE = "service_type";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private StuffAdapter mAdapter;
    private LocalBroadcastManager mLocalBroadcastManager;
    private UpdateResultReceiver updateResultReceiver;
    private boolean mIsLoadingMore = false;
    private boolean mIsRefreshing = false;
    private String mType;
    private boolean mIsNoMore = false;

    public static StuffFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString("type", type);

        StuffFragment fragment = new StuffFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        mType = getArguments().getString("type");
        if (!mType.equals(Constants.TYPE_COLLECTIONS))
            updateResultReceiver = new UpdateResultReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stuff_fragment, container, false);

        mRefreshLayout = $(view, R.id.stuff_refresh_layout);

        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView = $(view, R.id.stuff_recyclerview);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter = new StuffAdapter(getActivity(), mType));
        if (!mType.equals(Constants.TYPE_COLLECTIONS)) {
            mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!mIsLoadingMore && dy > 0) {
                        int lastVisiblePos = mLayoutManager.findLastVisibleItemPosition();
                        if (!mIsNoMore && lastVisiblePos + 1 == mAdapter.getItemCount()) {
                            loadingMore();
                            CommonUtil.makeSnackBar(mRefreshLayout, getString(R.string.str_load_more), Snackbar.LENGTH_SHORT);
                        }
                    }
                }
            });
        }

        mAdapter.setOnItemClickListener(new StuffAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                if (mIsLoadingMore || mIsRefreshing) {
                    return;
                }
                CommonUtil.openUrl(getActivity(), mAdapter.getStuffAt(pos).getUrl());
            }

            @Override
            public void onItemLongClick(final View view, final int pos) {
                Log.d(TAG, "onItemLongClick: " + pos);
                view.setActivated(true);
                getActivity().startActionMode(new AbsListView.MultiChoiceModeListener() {
                    @Override
                    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                    }

                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        mode.getMenuInflater().inflate(R.menu.context_menu, menu);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.context_menu_share:
                                Stuff stuff = mAdapter.getStuffAt(pos);
                                String textShared = stuff.getTitle() + "    " + stuff.getUrl()+" -- "+getString(R.string.str_share_msg);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.str_share_msg));
                                intent.putExtra(Intent.EXTRA_TEXT, textShared);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getActivity().startActivity(intent);
                                mode.finish();
                                return true;
                            default:
                                return false;
                        }
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        view.setActivated(false);
                    }
                });
            }
        });

        if (!mType.equals(Constants.TYPE_COLLECTIONS))
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        Log.d(TAG, "onCreateView: ");
        return view;
    }

    private void loadingMore() {
        if (mIsLoadingMore)
            return;

        Intent intent = new Intent(getActivity(), StuffFetchService.class);
        intent.setAction(StuffFetchService.ACTION_FETCH_MORE).putExtra(SERVICE_TYPE, mType);
        getActivity().startService(intent);

        mIsLoadingMore = true;
        setRefreshLayout(true);
    }

    private void refreshStuff() {
        if (mIsRefreshing) {
            return;
        }

        if (mType.equals(Constants.TYPE_COLLECTIONS)) {
            setRefreshLayout(false);
            updateData();
            return;
        }

        Intent intent = new Intent(getActivity(), StuffFetchService.class);
        intent.setAction(StuffFetchService.ACTION_FETCH_REFRESH).putExtra(SERVICE_TYPE, mType);
        getActivity().startService(intent);

        mIsRefreshing = true;
        setRefreshLayout(true);
    }

    private void setRefreshLayout(final boolean state) {
        if (null == mRefreshLayout)
            return;

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(state);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshStuff();
            }
        };
        mRefreshLayout.setOnRefreshListener(listener);
        listener.onRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (!mType.equals(Constants.TYPE_COLLECTIONS))
            mLocalBroadcastManager.registerReceiver(updateResultReceiver, new IntentFilter(StuffFetchService.ACTION_UPDATE_RESULT));
        else
            updateData();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        if (mType.equals(Constants.TYPE_COLLECTIONS))
            return;
        VolleyUtil.getInstance(getActivity()).getRequestQueue().cancelAll(mType);
        mLocalBroadcastManager.unregisterReceiver(updateResultReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    private <T extends View> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    public void smoothScrollToTop() {
        if (null != mLayoutManager) {
            mLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
        }
    }

    public void updateData() {
        if (null == mAdapter)
            return;

        mAdapter.notifyDataSetChanged();
    }

    private class UpdateResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int fetched = intent.getIntExtra(StuffFetchService.EXTRA_FETCHED, 0);
            String trigger = intent.getStringExtra(StuffFetchService.EXTRA_TRIGGER);
            String type = intent.getStringExtra(StuffFetchService.EXTRA_TYPE);

            if (!type.equals(mType)) {
                return;
            }

            Log.d(TAG, "fetched " + fetched + ", triggered by " + trigger);
            if (fetched == 0 && trigger.equals(StuffFetchService.ACTION_FETCH_MORE)) {
                CommonUtil.makeSnackBar(mRefreshLayout, getString(R.string.str_no_more), Snackbar.LENGTH_SHORT);
                mIsNoMore = true;
            }

            setRefreshLayout(false);
            if (mIsRefreshing) {
                mIsRefreshing = false;
                CommonUtil.makeSnackBar(mRefreshLayout, getString(R.string.str_refreshed), Snackbar.LENGTH_SHORT);
                mRecyclerView.smoothScrollToPosition(0);
            }
            if (mIsLoadingMore)
                mIsLoadingMore = false;

            if (null == mAdapter || fetched == 0)
                return;
            mAdapter.updateInsertedData(fetched, trigger.equals(StuffFetchService.ACTION_FETCH_MORE) ? true : false);
        }
    }
}
