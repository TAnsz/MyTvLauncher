package com.tan.mytvlauncher.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by t1569 on 2017/9/23.
 */

public class AppModel implements Comparable<AppModel> {
    private static final String PREFS_NAME = "MyAppFile";
    private String dataDir;
    private Drawable icon;
    private String id;
    private String name;
    private String launcherName;
    private String packageName;
    private int pageIndex;
    private int position;
    private boolean sysApp;
    private int openCount;

    public String getDataDir() {
        return this.dataDir;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public int getPageIndex() {
        return this.pageIndex;
    }

    public int getPosition() {
        return this.position;
    }

    public void setDataDir(String paramString) {
        this.dataDir = paramString;
    }

    public void setIcon(Drawable paramDrawable) {
        this.icon = paramDrawable;
    }

    public void setId(String paramString) {
        this.id = paramString;
    }

    public void setName(String paramString) {
        this.name = paramString;
    }

    public void setPackageName(String paramString) {
        this.packageName = paramString;
    }

    public void setPageIndex(int paramInt) {
        this.pageIndex = paramInt;
    }

    public void setPosition(int paramInt) {
        this.position = paramInt;
    }

    public String toString() {
        return "AppBean [packageName=" + this.packageName + ", name=" + this.name + ", dataDir=" + this.dataDir + "]";
    }

    public boolean isSysApp() {
        return sysApp;
    }

    public void setSysApp(boolean sysApp) {
        this.sysApp = sysApp;
    }

    public String getLauncherName() {
        return launcherName;
    }

    public void setLauncherName(String launcherName) {
        this.launcherName = launcherName;
    }

    public int getOpenCount() {
        return openCount;
    }

    public void initOpenCount(Context context) {
        if (openCount <= 0) {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            openCount = settings.getInt(this.getPackageName(), 0);
        }
    }

    public void setOpenCount(Context context, int openCount) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putInt(this.getPackageName(), openCount);
        editor.commit();
        this.openCount = openCount;
    }

    @Override
    public int compareTo(AppModel o) {
        return this.getOpenCount() - o.getOpenCount();
    }
}
