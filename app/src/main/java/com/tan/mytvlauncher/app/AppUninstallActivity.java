package com.tan.mytvlauncher.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tan.mytvlauncher.R;
import com.tan.mytvlauncher.adapter.AppUninstallAdapter;
import com.tan.mytvlauncher.util.Tools;

import java.util.ArrayList;
import java.util.List;

public class AppUninstallActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "AppUninstallActivity";
    private Context mContext;
    private ListView listView;
    private List<AppModel> appModels;
    private Receiver receiver;
    private AppUninstallAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_uninstall);
        mContext = this;
        init();
    }

    private void init() {
        appModels = new ArrayList<>();
        listView = (ListView) findViewById(R.id.app_uninstall_lv);
        AppDataManager appDataManager = new AppDataManager(mContext);
        for (AppModel item : appDataManager.getLauncherAppList()
                ) {
            if (!item.isSysApp()) {
                appModels.add(item);
            }
        }
        adapter = new AppUninstallAdapter(mContext, appModels);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri packageUri = Uri.parse("package:" + appModels.get(position).getPackageName());
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                mContext.startActivity(uninstallIntent);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    @Override
    public void onClick(View v) {

    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //接收安装广播
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {

                String packageName = intent.getDataString();
                packageName = packageName.split(":")[1];
                List<ResolveInfo> list = Tools.findActivitiesForPackage(context, packageName);
                ResolveInfo info = list.get(0);
                PackageManager localPackageManager = context.getPackageManager();
                AppModel localAppBean = new AppModel();
                localAppBean.setIcon(info.activityInfo.loadIcon(localPackageManager));
                localAppBean.setName(info.activityInfo.loadLabel(localPackageManager).toString());
                localAppBean.setPackageName(info.activityInfo.packageName);
                localAppBean.setDataDir(info.activityInfo.applicationInfo.publicSourceDir);

                appModels.add(localAppBean);
            }
            //接收卸载广播
            if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                String receiverName = intent.getDataString();
                receiverName = receiverName.substring(8);
                AppModel appBean;
                for (int i = 0; i < appModels.size(); i++) {
                    appBean = appModels.get(i);
                    String packageName = appBean.getPackageName();
                    if (packageName.equals(receiverName)) {
                        appModels.remove(i);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }
}
