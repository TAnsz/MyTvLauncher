package com.tan.mytvlauncher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tan.mytvlauncher.R;
import com.tan.mytvlauncher.app.AppModel;

import java.util.List;

/**
 * Created by t1569 on 2017/9/23.
 */

public class AppUninstallAdapter extends BaseAdapter {
    private final List<AppModel> appModels;
    private final Context context;

    public AppUninstallAdapter(Context context, List<AppModel> appModels) {
        this.context = context;
        this.appModels = appModels;
    }

    @Override
    public int getCount() {
        return appModels.size();
    }

    @Override
    public Object getItem(int position) {
        return appModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView==null){
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_app_uninstall,null
            );
            holder.name = (TextView) convertView.findViewById(R.id.item_app_uninstall_name);
            holder.icon = (ImageView)convertView.findViewById(R.id.item_app_uninstall_iv);
            convertView.setTag(holder);
        }else{
            holder = (Holder)convertView.getTag();
        }
        AppModel appModel = appModels.get(position);
        holder.icon.setImageDrawable(appModel.getIcon());
        holder.name.setText(appModel.getName());
        return convertView;
    }
    private class Holder {
        private TextView name;
        private ImageView icon;
    }
}
