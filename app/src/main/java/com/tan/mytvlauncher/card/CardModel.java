package com.tan.mytvlauncher.card;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by t1569 on 2017/9/22.
 */

public class CardModel {
    private long id;
    private String title;
    private String desc;
    private String imgUrl;

    public CardModel(final long id, final String title, final String desc, final String imgUrl) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.imgUrl = imgUrl;
    }

    public CardModel() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public static List<CardModel> getCardModels() {
        List<CardModel> cardModels = new ArrayList<>();
        String titles[] = {
                "视频",
                "音乐",
                "直播"
        };
        for (int i = 0; i < titles.length; i++) {
            CardModel cardModel = new CardModel(i, titles[i], "", "");
            cardModels.add(cardModel);
        }
        return cardModels;
    }
}
