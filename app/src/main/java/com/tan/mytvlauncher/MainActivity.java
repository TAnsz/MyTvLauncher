package com.tan.mytvlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.tan.mytvlauncher.app.AppCardPresenter;
import com.tan.mytvlauncher.app.AppDataManager;
import com.tan.mytvlauncher.app.AppModel;
import com.tan.mytvlauncher.card.CardModel;
import com.tan.mytvlauncher.card.CardPresenter;
import com.tan.mytvlauncher.function.FunctionCardPresenter;
import com.tan.mytvlauncher.function.FunctionModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    protected BrowseFragment mBrowseFragment;
    private ArrayObjectAdapter rowsAdapter;
    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;
    private Context mContext;

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
        addCardRow();
        addAppRow();
        addFunctionRow();

        Log.d("main", "buildRowsAdapter: ");
        mBrowseFragment.setAdapter(rowsAdapter);
        mBrowseFragment.setOnItemViewClickedListener(new OnItemViewClickedListener() {
                                                         @Override
                                                         public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                                                             if (item instanceof AppModel) {
                                                                 AppModel appModel = (AppModel) item;
                                                                 Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(appModel.getPackageName());
                                                                 if (null != intent) mContext.startActivity(intent);
                                                             }else if (item instanceof FunctionModel){
                                                                 FunctionModel functionModel = (FunctionModel)item;
                                                                 Intent intent = functionModel.getIntent();
                                                                 if (null != intent)
                                                                     startActivity(intent);
                                                             }
                                                         }
                                                     }

        );
    }

    private void addFunctionRow() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new FunctionCardPresenter());
        List<FunctionModel> functionModels = FunctionModel.getFunctionList(mContext);
        for (FunctionModel item:functionModels
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

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(this);
        mBackgroundManager.attach(this.getWindow());
        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        //设置背景图
        mBackgroundManager.setDrawable(mContext.getResources().getDrawable(R.drawable.pic_default));
    }
}
