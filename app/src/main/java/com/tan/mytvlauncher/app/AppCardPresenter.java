package com.tan.mytvlauncher.app;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tan.mytvlauncher.R;

/**
 * Created by t1569 on 2017/9/22.
 */

public class AppCardPresenter extends Presenter {
    private static final String TAG = "AppCardPresenter";

    private static int CARD_WIDTH = 313;
    private static int CARD_HEIGHT = 176;
    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;
    private Drawable mDefaultCardImage;

    private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
        // Both background colors should be set because the view's background is temporarily visible
        // during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");

        sDefaultBackgroundColor = parent.getResources().getColor(R.color.default_background);
        sSelectedBackgroundColor = parent.getResources().getColor(R.color.selected_background);

        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        AppModel appModel = (AppModel) item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        Log.d(TAG, "onBindViewHolder");
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        cardView.setTitleText(appModel.getName());
        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER_INSIDE);
        cardView.getMainImageView().setImageDrawable(appModel.getIcon());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}
