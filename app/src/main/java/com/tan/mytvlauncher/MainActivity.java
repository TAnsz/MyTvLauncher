package com.tan.mytvlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends Activity {
    protected BrowseFragment mBrowseFragment;
    private ArrayObjectAdapter rowsAdapter;
    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;
    private Context mContext;
    private String backImgUrl = null;
    private static int CARD_L_WIDTH = 480;
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
        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
//        addCardRow();
        addUsedRow();
        addAppRow();
        addFunctionRow();

        Log.d("main", "buildRowsAdapter: ");
        mBrowseFragment.setAdapter(rowsAdapter);
        mBrowseFragment.setOnItemViewClickedListener(new OnItemViewClickedListener() {
                                                         @Override
                                                         public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                                                             if (item instanceof AppModel) {
                                                                 AppModel appModel = (AppModel) item;
                                                                 appModel.setOpenCount(mContext, appModel.getOpenCount() + 1);
                                                                 Log.d("main", "onItemClicked: " + appModel.getOpenCount());
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

    private void addUsedRow() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new AppCardPresenter(CARD_L_WIDTH, CARD_L_HEIGHT));
        ArrayList<AppModel> appModels = new AppDataManager(mContext).getLauncherAppList();
        Collections.sort(appModels);
        for (int i = 0; i < 4; i++) {
            listRowAdapter.add(appModels.get(appModels.size() - 1 - i));
        }
        ListRow listRow = new ListRow(new HeaderItem(0, getString(R.string.title_used)), listRowAdapter);
        rowsAdapter.add(listRow);
    }

    private void addFunctionRow() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new FunctionCardPresenter());
        List<FunctionModel> functionModels = FunctionModel.getFunctionList(mContext);
        for (FunctionModel item : functionModels
                ) {
            listRowAdapter.add(item);
        }
        rowsAdapter.add(new ListRow(new HeaderItem(0, getString(R.string.title_function)), listRowAdapter));
    }

    private void addAppRow() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new AppCardPresenter());
        ArrayObjectAdapter listSysRowAdapter = new ArrayObjectAdapter(new AppCardPresenter());
        ArrayList<AppModel> appModels = new AppDataManager(mContext).getLauncherAppList();
        for (AppModel appModel : appModels
                )
            if (appModel.isSysApp()) listSysRowAdapter.add(appModel);
            else listRowAdapter.add(appModel);
        rowsAdapter.add(new ListRow(new HeaderItem(0, getString(R.string.title_app)), listRowAdapter));
        rowsAdapter.add(new ListRow(new HeaderItem(0, getString(R.string.title_sysapp)), listSysRowAdapter));
    }

    private void addCardRow() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());

        for (CardModel carModel : CardModel.getCardModels()
                ) {
            listRowAdapter.add(carModel);
        }

        HeaderItem header = new HeaderItem(0, getString(R.string.title_used));
        rowsAdapter.add(new ListRow(header, listRowAdapter));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (backImgUrl == null) setBingImg();
        else setBackgroundImage();
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
}
