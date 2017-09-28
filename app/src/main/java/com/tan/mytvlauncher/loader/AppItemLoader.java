package com.tan.mytvlauncher.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.util.Log;

import com.tan.mytvlauncher.MainActivity;
import com.tan.mytvlauncher.R;
import com.tan.mytvlauncher.app.AppCardPresenter;
import com.tan.mytvlauncher.app.AppDataManager;
import com.tan.mytvlauncher.app.AppModel;
import com.tan.mytvlauncher.card.CardModel;
import com.tan.mytvlauncher.card.CardPresenter;
import com.tan.mytvlauncher.function.FunctionCardPresenter;
import com.tan.mytvlauncher.function.FunctionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by t1569 on 2017/9/24.
 */

public class AppItemLoader extends AsyncTaskLoader<List<ListRow>> {
    private static final String TAG = AppItemLoader.class.getSimpleName();
    private static int CARD_L_WIDTH = 435;
    private static int CARD_L_HEIGHT = 300;
    private ArrayList<AppModel> mAppModels;

    public AppItemLoader(Context context, ArrayList<AppModel> appModels) {

        super(context);
        this.mAppModels = appModels;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<ListRow> loadInBackground() {
        List<ListRow> listRows = new ArrayList<>();
        Log.d(TAG, "loadInBackground: " + mAppModels.size());
        listRows.add(getUsedRow());

        listRows.addAll(getAppRow());
        listRows.add(getFunctionRow());
        return listRows;
    }

    private ListRow getUsedRow() {
        ArrayObjectAdapter usedListRowAdapter = new ArrayObjectAdapter(new AppCardPresenter(CARD_L_WIDTH, CARD_L_HEIGHT));
        ArrayList<AppModel> appModels = (ArrayList<AppModel>) mAppModels.clone();
        Collections.sort(appModels, new Comparator<AppModel>() {
            public int compare(AppModel appModel1, AppModel appModel2) {
                return appModel2.getOpenCount() - appModel1.getOpenCount();
            }
        });
        for (int i = 0; i < 4; i++) {
            usedListRowAdapter.add(appModels.get(i));
        }
        ListRow listRow = new ListRow(new HeaderItem(0, getContext().getString(R.string.title_used)), usedListRowAdapter);
        return listRow;
    }

    private ListRow getFunctionRow() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new FunctionCardPresenter());
        List<FunctionModel> functionModels = FunctionModel.getFunctionList(getContext());
        for (FunctionModel item : functionModels
                ) {
            listRowAdapter.add(item);
        }
        return new ListRow(new HeaderItem(0, getContext().getString(R.string.title_function)), listRowAdapter);
    }

    private List<ListRow> getAppRow() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new AppCardPresenter());
        ArrayObjectAdapter listSysRowAdapter = new ArrayObjectAdapter(new AppCardPresenter());
        for (AppModel appModel : mAppModels
                )
            if (appModel.isSysApp()) listSysRowAdapter.add(appModel);
            else listRowAdapter.add(appModel);
        List<ListRow> listRows = new ArrayList<>();
        listRows.add(new ListRow(new HeaderItem(0, getContext().getString(R.string.title_app)), listRowAdapter));
        listRows.add(new ListRow(new HeaderItem(0, getContext().getString(R.string.title_sysapp)), listSysRowAdapter));
        return listRows;
    }

    private ListRow[] getCardRow() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());

        for (CardModel carModel : CardModel.getCardModels()
                ) {
            listRowAdapter.add(carModel);
        }

        HeaderItem header = new HeaderItem(0, getContext().getString(R.string.title_used));
        return new ListRow[]{new ListRow(header, listRowAdapter)};
    }
}
