package com.tan.mytvlauncher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.os.Bundle;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.tan.mytvlauncher.app.AppCardPresenter;
import com.tan.mytvlauncher.app.AppDataManager;
import com.tan.mytvlauncher.app.AppModel;
import com.tan.mytvlauncher.app.BingImage;
import com.tan.mytvlauncher.card.CardModel;
import com.tan.mytvlauncher.card.CardPresenter;
import com.tan.mytvlauncher.function.FunctionCardPresenter;
import com.tan.mytvlauncher.function.FunctionModel;
import com.tan.mytvlauncher.loader.AppItemLoader;
import com.tan.mytvlauncher.util.SpinnerFragment;
import com.tan.mytvlauncher.util.Tools;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends Activity {
    protected BrowseFragment mBrowseFragment;
    private BackgroundManager mBackgroundManager;
    private ArrayObjectAdapter mUsedListRowAdapter;
    private DisplayMetrics mMetrics;
    private Context mContext;
    private ArrayList<AppModel> mAppModels;
    private Receiver receiver;
    private String backImgUrl = null;
    private static int CARD_L_WIDTH = 435;
    private static int CARD_L_HEIGHT = 300;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int ITEM_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mBrowseFragment = (BrowseFragment) getFragmentManager().findFragmentById(R.id.browse_fragment);
        mBrowseFragment.setHeadersState(BrowseFragment.HEADERS_DISABLED);

        getLoaderManager().initLoader(ITEM_LOADER_ID, null, new MainFragmentLoaderCallbacks());

        mBrowseFragment.setTitle(getString(R.string.app_name));
        prepareBackgroundManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        receiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (backImgUrl == null) setBingImg();
        else setBackgroundImage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(this);
        mBackgroundManager.attach(this.getWindow());
        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        //设置背景图
        setBingImg();
    }

    private void setBackgroundImage() {
        Glide.with(mContext)
                .load(backImgUrl)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
    }

    private void setBingImg() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        if (statusCode == 200) {
                            Gson gson = new Gson();
                            BingImage bingImage = gson.fromJson(response.toString(), BingImage.class);
                            List<BingImage.ImagesBean> img = bingImage.getImages();
                            if (img != null && img.size() > 0) {
                                backImgUrl = "https://cn.bing.com" + img.get(0).getUrl();
                                setBackgroundImage();
                            } else Log.d("main", "onSuccess: 没有获取到类型");

                        }
                    }
                }
        );
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //接收安装广播
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                try {
                    String packageName = intent.getDataString();
                    packageName = packageName.split(":")[1];
                    List<ResolveInfo> list = Tools.findActivitiesForPackage(context, packageName);
                    if (list.size() > 0) {
                        ResolveInfo info = list.get(0);
                        PackageManager localPackageManager = context.getPackageManager();
                        AppModel localAppModel = new AppModel();
                        localAppModel.setIcon(info.activityInfo.loadIcon(localPackageManager));
                        localAppModel.setName(info.activityInfo.loadLabel(localPackageManager).toString());
                        localAppModel.setPackageName(info.activityInfo.packageName);
                        localAppModel.setDataDir(info.activityInfo.applicationInfo.publicSourceDir);
                        mAppModels.add(localAppModel);
                        getLoaderManager().restartLoader(ITEM_LOADER_ID, null, new MainFragmentLoaderCallbacks());
                    } else {
                        Log.d(TAG, "onReceive: 找不到安装的app");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            //接收卸载广播
            if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                String receiverName = intent.getDataString();
                Log.d(TAG, "onReceive: " + receiverName);
                receiverName = receiverName.substring(8);
                for (int i = 0; i < mAppModels.size(); i++
                        ) {
                    if (mAppModels.get(i).getPackageName().equals(receiverName)) {
                        mAppModels.get(i).setOpenCount(mContext, 0);
                        mAppModels.remove(i);
                        getLoaderManager().restartLoader(ITEM_LOADER_ID, null, new MainFragmentLoaderCallbacks());
                    }
                }
            }
        }
    }

    private class MainFragmentLoaderCallbacks implements android.app.LoaderManager.LoaderCallbacks<List<ListRow>> {
        @Override
        public Loader<List<ListRow>> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader: AppItemLoader");
            mAppModels = new AppDataManager(mContext).getLauncherAppList();
            return new AppItemLoader(mContext, mAppModels);
        }

        @Override
        public void onLoadFinished(Loader<List<ListRow>> loader, List<ListRow> data) {
            Log.d(TAG, "onLoadFinished: " + data.size());
            switch (loader.getId()) {
                case ITEM_LOADER_ID:
                    Log.d(TAG, "onLoadFinished: UI Update");
                    ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                    rowsAdapter.addAll(0, data);
                    mBrowseFragment.setAdapter(rowsAdapter);
                    mBrowseFragment.setOnItemViewClickedListener(new OnItemViewClickedListener() {
                                                                     @Override
                                                                     public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                                                                         if (item instanceof AppModel) {
                                                                             AppModel appModel = (AppModel) item;
                                                                             appModel.setOpenCount(mContext, appModel.getOpenCount() + 1);
                                                                             Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(appModel.getPackageName());
                                                                             if (null != intent) mContext.startActivity(intent);
                                                                         } else if (item instanceof FunctionModel) {
                                                                             FunctionModel functionModel = (FunctionModel) item;
                                                                             Intent intent = functionModel.getIntent();
                                                                             if (null != intent)
                                                                                 startActivity(intent);
                                                                         }
                                                                     }
                                                                 }

                    );
            }
        }

        @Override
        public void onLoaderReset(Loader<List<ListRow>> loader) {
            mBrowseFragment.setAdapter(null);
        }
    }
}
