package com.tan.mytvlauncher.function;

import android.content.Context;
import android.content.Intent;

import com.tan.mytvlauncher.R;
import com.tan.mytvlauncher.app.AppUninstallActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by t1569 on 2017/9/23.
 */

public class FunctionModel {
    private int icon;
    private String id;
    private String name;
    private Intent mIntent;

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public void setIntent(Intent Intent) {
        this.mIntent = Intent;
    }

    public static List<FunctionModel> getFunctionList(Context context) {
        List<FunctionModel> functionModels = new ArrayList<>();
        FunctionModel appUninstall = new FunctionModel();
        appUninstall.setName(context.getString(R.string.appUninstall));
        appUninstall.setIcon(R.drawable.ic_app_uninstall);
        appUninstall.setIntent(new Intent(context, AppUninstallActivity.class));

        functionModels.add(appUninstall);
        return functionModels;
    }
}
