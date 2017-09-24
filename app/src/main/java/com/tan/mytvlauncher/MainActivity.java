package com.tan.mytvlauncher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.tan.mytvlauncher.app.AppUninstallActivity;
import com.tan.mytvlauncher.app.BingImage;
import com.tan.mytvlauncher.card.CardModel;
import com.tan.mytvlauncher.card.CardPresenter;
import com.tan.mytvlauncher.function.FunctionCardPresenter;
import com.tan.mytvlauncher.function.FunctionModel;
import com.tan.mytvlauncher.util.Tools;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends Activity {
    protected BrowseFragment mBrowseFragment;
    private ArrayObjectAdapter rowsAdapter;
    private BackgroundManager mBackgroundManager;
    private ArrayObjectAdapter mUsedListRowAdapter;
    private DisplayMetrics mMetrics;
    private Context mContext;
    private ArrayList<AppModel> mAppModels;
    private Receiver receiver;
    private String backImgUrl = null;
    private static int CARD_L_WIDTH = 435;
    private static int CARD_L_HEIGHT = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mBrowseFragment = (BrowseFragment) getFragmentManager().findFragmentById(R.id.browse_fragment);
        mBrowseFragment.setHeadersState(BrowseFragment.HEADERS_DISABLED);

        mBrowseFragment.setTitle(getString(R.string.app_name));
        prepareBackgroundManager();
        buildRowsAdapter();
    }

    private void buildRowsAdapter() {
        mAppModels = new AppDataManager(mContext).getLauncherAppList();
        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        new AsyncTask<String, Void, List<ListRow>>() {
            @Override
            protected List<ListRow> doInBackground(String... params) {
                List<ListRow> listRows = new ArrayList<ListRow>();
                listRows.add(getUsedRow());

                listRows.addAll(getAppRow());
                listRows.add(getFunctionRow());
                return listRows;
            }

            @Override
            protected void onPostExecute(List<ListRow> listRows) {
                rowsAdapter.addAll(0, listRows);
                mBrowseFragment.setAdapter(rowsAdapter);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        Log.d("main", "buildRowsAdapter: ");
        mBrowseFragment.setOnItemViewClickedListener(new OnItemViewClickedListener() {
                                                         @Override
                                                         public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                                                             if (item instanceof AppModel) {
                                                                 AppModel appModel = (AppModel) item;
                                                                 int index = mAppModels.indexOf(appModel);
                                                                 mAppModels.get(index).setOpenCount(mContext, appModel.getOpenCount() + 1);
                                                                 refreshUsedApp();
                                                                 rowsAdapter.notifyArrayItemRangeChanged(0, 1);
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

    private ListRow getUsedRow() {
        mUsedListRowAdapter = new ArrayObjectAdapter(new AppCardPresenter(CARD_L_WIDTH, CARD_L_HEIGHT));
        refreshUsedApp();
        ListRow listRow = new ListRow(new HeaderItem(0, getString(R.string.title_used)), mUsedListRowAdapter);
        return listRow;
    }

    private void refreshUsedApp() {
        ArrayList<AppModel> appModels = new AppDataManager(mContext).getLauncherAppList();
        Collections.sort(appModels, new Comparator<AppModel>() {
            public int compare(AppModel appModel1, AppModel appModel2) {
                return appModel2.getOpenCount() - appModel1.getOpenCount();
            }
        });
        mUsedListRowAdapter.clear();
        for (int i = 0; i < 4; i++) {
            mUsedListRowAdapter.add(appModels.get(i));
        }
    }

    private ListRow getFunctionRow() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new FunctionCardPresenter());
        List<FunctionModel> functionModels = FunctionModel.getFunctionList(mContext);
        for (FunctionModel item : functionModels
                ) {
            listRowAdapter.add(item);
        }
        return new ListRow(new HeaderItem(0, getString(R.string.title_function)), listRowAdapter);
    }

    private List<ListRow> getAppRow() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new AppCardPresenter());
        ArrayObjectAdapter listSysRowAdapter = new ArrayObjectAdapter(new AppCardPresenter());
        for (AppModel appModel : mAppModels
                )
            if (appModel.isSysApp()) listSysRowAdapter.add(appModel);
            else listRowAdapter.add(appModel);
        List<ListRow> listRows = new ArrayList<>();
        listRows.add(new ListRow(new HeaderItem(0, getString(R.string.title_app)), listRowAdapter));
        listRows.add(new ListRow(new HeaderItem(0, getString(R.string.title_sysapp)), listSysRowAdapter));
        return listRows;
    }

    private ListRow[] getCardRow() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());

        for (CardModel carModel : CardModel.getCardModels()
                ) {
            listRowAdapter.add(carModel);
        }

        HeaderItem header = new HeaderItem(0, getString(R.string.title_used));
        return new ListRow[]{new ListRow(header, listRowAdapter)};
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

                String packageName = intent.getDataString();
                List<ResolveInfo> list = Tools.findActivitiesForPackage(context, packageName);
                ResolveInfo info = list.get(0);
                PackageManager localPackageManager = context.getPackageManager();
                AppModel localAppBean = new AppModel();
                localAppBean.setIcon(info.activityInfo.loadIcon(localPackageManager));
                localAppBean.setName(info.activityInfo.loadLabel(localPackageManager).toString());
                localAppBean.setPackageName(info.activityInfo.packageName);
                localAppBean.setDataDir(info.activityInfo.applicationInfo.publicSourceDir);

                mAppModels.add(localAppBean);
            }
            //接收卸载广播
            if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                String receiverName = intent.getDataString();
                receiverName = receiverName.substring(8);
                AppModel appBean;
                for (int i = 0; i < mAppModels.size(); i++) {
                    appBean = mAppModels.get(i);
                    String packageName = appBean.getPackageName();
                    if (packageName.equals(receiverName)) {
                        mAppModels.remove(i);
                        rowsAdapter.notifyArrayItemRangeChanged(1, 2);
                    }
                }
            }
        }
    }
}
