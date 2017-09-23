package com.tan.mytvlauncher.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by t1569 on 2017/9/23.
 */

public class AppDataManager {
    private final Context mContext;

    public AppDataManager(Context context) {
        mContext = context;
    }

    public ArrayList<AppModel> getLauncherAppList() {
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
        ArrayList<AppModel> appModels = new ArrayList<>();
        Iterator<ResolveInfo> resolveInfoIterators = null;
        if (list.size() != 0) {
            resolveInfoIterators = list.iterator();
        }
        while (true) {
            if (!resolveInfoIterators.hasNext())
                break;
            ResolveInfo resolveInfo = resolveInfoIterators.next();
            AppModel appModel = new AppModel();
            appModel.setIcon(resolveInfo.activityInfo.loadIcon(packageManager));
            appModel.setName(resolveInfo.activityInfo.loadLabel(packageManager).toString());
            appModel.setPackageName(resolveInfo.activityInfo.packageName);
            appModel.setDataDir(resolveInfo.activityInfo.applicationInfo.publicSourceDir);
            appModel.setLauncherName(resolveInfo.activityInfo.name);
            appModel.initOpenCount(mContext);
            String pkgName = resolveInfo.activityInfo.packageName;
            PackageInfo mPackageInfo;
            try {
                mPackageInfo = mContext.getPackageManager().getPackageInfo(pkgName, 0);
                if ((mPackageInfo.applicationInfo.flags & mPackageInfo.applicationInfo.FLAG_SYSTEM) > 0) {
                    appModel.setSysApp(true);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (!appModel.getPackageName().equals("com.tan.mytvlauncher")) appModels.add(appModel);
        }
        return appModels;
    }
}
