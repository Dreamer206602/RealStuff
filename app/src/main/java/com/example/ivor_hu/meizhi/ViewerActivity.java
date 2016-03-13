package com.example.ivor_hu.meizhi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.example.ivor_hu.meizhi.db.DBManager;
import com.example.ivor_hu.meizhi.db.Image;
import com.example.ivor_hu.meizhi.utils.CommonUtil;
import com.example.ivor_hu.meizhi.utils.PicUtil;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Ivor on 2016/2/15.
 */
public class ViewerActivity extends AppCompatActivity {
    public static final String TAG = "ViewerActivity";

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 111;
    private final Handler msgHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case PicUtil.SAVE_DONE_TOAST:
                    String filepath = msg.getData().getString(PicUtil.FILEPATH);
                    CommonUtil.makeSnackBar(mViewPager, "已保存至" + filepath, Snackbar.LENGTH_LONG);
                    break;
                default:
                    break;
            }
        }
    };
    private ViewPager mViewPager;
    private List<Image> mImages;
    private int mPos;
    private Toolbar mToolbar;
    private FragmentStatePagerAdapter mAdapter;
    private boolean mIsHidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        setContentView(R.layout.viewer_pager_layout);

        initToolbar();

        mPos = getIntent().getIntExtra(GirlsFragment.POSTION, 0);
        mImages = DBManager.getIns(this).queryAllImages();
        mViewPager = (ViewPager) findViewById(R.id.viewer_pager);
        mViewPager.setAdapter(mAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return ViewerFragment.newInstance(
                        mImages.get(position).getUrl(),
                        position == mPos);
            }

            @Override
            public int getCount() {
                return mImages.size();
            }
        });
        mViewPager.setCurrentItem(mPos);

        // 避免图片在进行 Shared Element Transition 时盖过 Toolbar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setSharedElementsUseOverlay(false);
        }

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
//                Log.d(TAG, "onMapSharedElements: " + mViewPager.getCurrentItem());
                Image image = mImages.get(mViewPager.getCurrentItem());
                sharedElements.clear();
                sharedElements.put(image.getUrl(), ((ViewerFragment) mAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem())).getSharedElement());
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.img_save:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                } else {
                    final String url = mImages.get(mViewPager.getCurrentItem()).getUrl();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                PicUtil.saveBitmapFromUrl(ViewerActivity.this, url, msgHandler);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                return true;
            case R.id.img_share:
                final String url = mImages.get(mViewPager.getCurrentItem()).getUrl();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        shareMsg(getString(R.string.str_share_msg), null, url);
                    }
                }).start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void supportFinishAfterTransition() {
//        Log.d(TAG, "supportFinishAfterTransition: finish");
        Intent data = new Intent();
        data.putExtra("index", mViewPager.getCurrentItem());
        setResult(RESULT_OK, data);

        super.supportFinishAfterTransition();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.viewer_toolbar);
        mToolbar.setTitle(R.string.nav_girls);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(mToolbar);
    }


    public void shareMsg(String msgTitle, String msgText, String url) {
        String imgPath = PicUtil.getImgPathFromUrl(url);

        Intent intent = new Intent(Intent.ACTION_SEND);
        if (imgPath == null || imgPath.equals("")) {
            intent.setType("text/plain");
        } else {
            File file = new File(imgPath);
            if (!file.exists()) {
                try {
                    PicUtil.saveBitmapFromUrl(ViewerActivity.this, url, msgHandler);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (file != null && file.exists() && file.isFile()) {
                intent.setType("image/jpg");
                Uri uri = Uri.fromFile(file);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void toggleToolbar() {
        if (mIsHidden)
            showToolbar();
        else
            hideToolbar();
    }

    public void hideToolbar() {
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        mIsHidden = true;
    }

    public void showToolbar() {
        if (mToolbar.getVisibility() == View.GONE)
            mToolbar.setVisibility(View.VISIBLE);
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        mIsHidden = false;
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
//            PicUtil.saveBitmapFromUrl(ScalableImageActivity.this, mUrls.get(mViewPager.getCurrentItem()));
//    }
}
